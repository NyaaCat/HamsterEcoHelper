package cat.nyaa.HamsterEcoHelper.signshop;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Sign;
import cat.nyaa.HamsterEcoHelper.database.SignShop;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.meowj.langutils.lang.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchCommands extends CommandReceiver {
    private Cache<UUID, List<Map.Entry<SignShop, ShopItem>>> searchResult =
            CacheBuilder.newBuilder()
                        .concurrencyLevel(2)
                        .maximumSize(10000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build();
    private HashMap<UUID, Long> cooldown = new HashMap<>();
    private HamsterEcoHelper plugin;

    public SearchCommands(Object plugin, ILocalizer i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    private static double distance(Location loc, Sign sign) {
        return Math.sqrt(
                NumberConversions.square(loc.getX() - sign.x) +
                        NumberConversions.square(loc.getY() - sign.y) +
                        NumberConversions.square(loc.getZ() - sign.z));
    }

    @Override
    public String getHelpPrefix() {
        return "search";
    }

    @SubCommand(value = "searchpage", permission = "heh.signshop.search")
    public void page(CommandSender sender, Arguments args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        UUID searcher = player == null ? new UUID(2333, 2333) : player.getUniqueId();
        List<Map.Entry<SignShop, ShopItem>> result = searchResult.getIfPresent(searcher);
        if (result == null) {
            msg(sender, "user.signshop.search.no_recent_result");
            return;
        }
        int page = args.nextInt() - 1;
        int start = page * 9;
        if (start > result.size()) {
            msg(sender, "user.signshop.search.page_out_of_bound");
            return;
        }
        msg(sender, "user.signshop.search.page", page + 1, (int) Math.ceil(result.size() / 9.0d));
        result.stream().skip(start).limit(9).forEach(pair ->
                                                             new Message("")
                                                                     .append(I18n.format("user.signshop.search.result",
                                                                             Bukkit.getOfflinePlayer(pair.getKey().owner)
                                                                                   .getName(),
                                                                             pair.getValue().unitPrice
                                                                     ), pair.getValue().itemStack)
                                                                     .send(sender));
    }

    @SuppressWarnings("deprecation")
    @SubCommand(permission = "heh.signshop.search", isDefaultCommand = true)
    public void search(CommandSender sender, Arguments args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        UUID searcher = player == null ? new UUID(2333, 2333) : player.getUniqueId();
        if (!sender.hasPermission("heh.admin") && (System.currentTimeMillis() < cooldown.getOrDefault(searcher, 0L))) {
            msg(sender, "user.info.cooldown", (cooldown.get(searcher) - System.currentTimeMillis()) / 1000);
            return;
        }
        Location curLoc = player == null ? null : player.getLocation();
        List<String> keywords = new LinkedList<>();
        String ownerLimit = args.argString("p", args.argString("player", null));
        String itemLimit = args.argString("i", args.argString("item", null));
        String advanced = args.argString("a", args.argString("advanced", ""));
        Set<String> advancedOption = ImmutableSet.copyOf(advanced.split(Pattern.quote("|")));
        boolean matchLoreOnly = advancedOption.contains("loreonly");
        boolean matchLore = matchLoreOnly || advancedOption.contains("lore");
        boolean matchEnchOnly = advancedOption.contains("enchonly");
        boolean matchEnch = matchEnchOnly || advancedOption.contains("ench");
        final int rangeLimit = player == null ? -1 : args.argInt("r", args.argInt("range", -1));
        final Material materialLimit;
        if (itemLimit != null) {
            materialLimit = MiscUtils.getMaterial(itemLimit, sender);
            if (materialLimit == null || materialLimit == Material.AIR || !materialLimit.isItem()) {
                msg(sender, "user.error.unknown_item", itemLimit);
                return;
            }
        } else {
            materialLimit = null;
        }
        while (args.top() != null) {
            keywords.add(args.next().toLowerCase());
        }
        if (keywords.isEmpty()) {
            msg(sender, "manual.search.usage");
            return;
        }
        long cd = System.currentTimeMillis() + plugin.config.search_cooldown_tick * 50;
        if (matchLore) cd += plugin.config.search_lore_cooldown_tick * 50;
        if (matchEnch) cd += plugin.config.search_ench_cooldown_tick * 50;
        cooldown.put(searcher, cd);

        UUID limitUuid = ownerLimit == null ? null : Bukkit.getOfflinePlayer(ownerLimit).getUniqueId();

        List<SignShop> signShops = plugin.database.getSignShops();

        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final LinkedListMultimap<SignShop, ShopItem> match = LinkedListMultimap.create();
            for (SignShop shop : signShops) {
                UUID owner = shop.owner;
                Set<Sign> signOwned = plugin.signShopManager.signLocations
                                              .stream()
                                              .filter(sign -> sign.owner.equals(owner))
                                              .filter(sign -> sign.shopMode == ShopMode.SELL)
                                              .collect(Collectors.toSet());
                if (signOwned.isEmpty()) continue;
                if (ownerLimit != null && !limitUuid.equals(owner))
                    continue;
                if (rangeLimit != -1 &&
                            signOwned.stream().noneMatch(sign ->
                                                                 curLoc.getWorld().getName().equals(sign.world)
                                                                         && distance(curLoc, sign) < rangeLimit)) {
                    continue;
                }
                List<ShopItem> items = shop.getItems(ShopMode.SELL);
                items.stream().filter(
                        shopItem -> {
                            boolean loreMatch;
                            boolean enchMatch;
                            ItemStack stack = shopItem.getItemStack(shopItem.amount);
                            ItemMeta meta = stack.getItemMeta();
                            if (materialLimit != null && !materialLimit.getKey().equals(stack.getType().getKey())) {
                                return false;
                            }
                            if (matchLore && meta.hasLore()) {
                                loreMatch = meta.getLore().stream()
                                                .map(ChatColor::stripColor)
                                                .map(String::toLowerCase)
                                                .anyMatch(lore -> keywords.stream().anyMatch(lore::contains) || "*".equals(keywords.get(0)));
                                if (loreMatch) return true;
                            }
                            if (matchLoreOnly) return false;
                            if (matchEnch) {
                                Map<Enchantment, Integer> enchants = meta.getEnchants();
                                if (meta instanceof EnchantmentStorageMeta) {
                                    enchants = ((EnchantmentStorageMeta) meta).getStoredEnchants();
                                }
                                enchMatch = enchants.entrySet()
                                                    .stream().flatMap(enchEntry ->
                                                                              Stream.of(
                                                                                      LanguageHelper.getEnchantmentDisplayName(enchEntry, "zh_cn"),
                                                                                      LanguageHelper.getEnchantmentDisplayName(enchEntry, "en_us")
                                                                              )
                                        )
                                                    .map(ChatColor::stripColor)
                                                    .map(String::toLowerCase)
                                                    .anyMatch(ench -> keywords.stream().anyMatch(ench::contains) || "*".equals(keywords.get(0)));
                                if (enchMatch) return true;
                            }
                            if (matchEnchOnly) return false;
                            String zhName = ChatColor.stripColor(LanguageHelper.getItemDisplayName(stack, "zh_cn")).toLowerCase();
                            String enName = ChatColor.stripColor(LanguageHelper.getItemDisplayName(stack, "en_us")).toLowerCase();
                            return "*".equals(keywords.get(0)) || keywords.stream().anyMatch(zhName::contains) || keywords.stream().anyMatch(enName::contains);
                        }
                ).forEach(shopItem -> match.put(shop, shopItem));
            }
            if (match.isEmpty()) {
                Bukkit.getServer().getScheduler().runTask(plugin, () -> msg(sender, "user.signshop.search.no_result"));
                return;
            }
            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                match.keySet().forEach(ss -> {
                    Stream<Sign> sis = plugin.signShopManager.signLocations.stream().filter(sign -> sign.owner.equals(ss.owner)).filter(sign -> sign.shopMode == ShopMode.SELL);
                    if (curLoc != null) {
                        sis = sis.filter(sign -> {
                            Location location = sign.getLocation();
                            if (location == null)return false;
                            return curLoc.getWorld().equals(location.getWorld());
                        })
                                 .sorted(Comparator.comparingDouble(a -> a.getLocation().distance(curLoc)));
                        if (rangeLimit != -1)
                            sis = sis.filter(sign -> curLoc.distance(sign.getLocation()) < rangeLimit);
                    }
                    sis.findFirst().ifPresent(si -> msg(sender, "user.signshop.search.sign_of", ss.getPlayer().getName(), si.x, si.y, si.z));
                });
                List<Map.Entry<SignShop, ShopItem>> result = match.entries().stream().sorted(Comparator.comparingDouble(a -> a.getValue().unitPrice)).collect(Collectors.toList());
                searchResult.put(searcher, result);
                msg(sender, "user.signshop.search.page", 1, (int) Math.ceil(result.size() / 9.0d));
                if (player != null) {
                    result.stream().limit(9).forEach(pair ->
                                                             new Message("")
                                                                     .append(I18n.format("user.signshop.search.result",
                                                                             Bukkit.getOfflinePlayer(pair.getKey().owner)
                                                                                   .getName(),
                                                                             pair.getValue().unitPrice
                                                                     ), pair.getValue().itemStack)
                                                                     .send(player));
                } else {
                    result.stream().limit(9).forEach(pair ->
                                                             sender.sendMessage(new Message("")
                                                                                        .append(I18n.format("user.signshop.search.result",
                                                                                                Bukkit.getOfflinePlayer(pair.getKey().owner)
                                                                                                      .getName(),
                                                                                                pair.getValue().unitPrice
                                                                                        ), pair.getValue().itemStack)
                                                                                        .inner.toLegacyText()));
                }
            });
        });
    }
}
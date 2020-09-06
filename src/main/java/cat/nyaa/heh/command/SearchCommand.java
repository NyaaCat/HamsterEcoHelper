package cat.nyaa.heh.command;

import cat.nyaa.heh.Configuration;
import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.meowj.langutils.lang.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class SearchCommand extends CommandReceiver {

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public SearchCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "search";
    }

    private static final String PERMISSION_SEARCH = "heh.search";
    private HashMap<UUID, Long> cooldown = new HashMap<>();
    Cache<UUID, List<ShopItem>> searchResult =
            CacheBuilder.newBuilder()
                    .concurrencyLevel(2)
                    .maximumSize(10000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_SEARCH)
    public void onSearch(CommandSender sender, Arguments arguments){
        Player player = sender instanceof Player ? (Player) sender : null;
        UUID searcher = player == null ? new UUID(2333, 2333) : player.getUniqueId();
        if (!sender.hasPermission("heh.admin") && (System.currentTimeMillis() < cooldown.getOrDefault(searcher, 0L))) {
            msg(sender, "user.info.cooldown", (cooldown.get(searcher) - System.currentTimeMillis()) / 1000);
            return;
        }
        List<String> keywords = new LinkedList<>();
        String ownerLimit = arguments.argString("p", arguments.argString("player", null));
        String itemLimit = arguments.argString("i", arguments.argString("item", null));
        String advanced = arguments.argString("a", arguments.argString("advanced", ""));
        Set<String> advancedOption = ImmutableSet.copyOf(advanced.split(Pattern.quote("|")));
        boolean matchLoreOnly = advancedOption.contains("loreonly");
        boolean matchLore = matchLoreOnly || advancedOption.contains("lore");
        boolean matchEnchOnly = advancedOption.contains("enchonly");
        boolean matchEnch = matchEnchOnly || advancedOption.contains("ench");
        final Material materialLimit;
        if (itemLimit != null) {
            materialLimit = getMaterial(itemLimit, sender);
            if (materialLimit == null || materialLimit == Material.AIR || !materialLimit.isItem()) {
                msg(sender, "user.error.unknown_item", itemLimit);
                return;
            }
        } else {
            materialLimit = null;
        }
        while (arguments.top() != null) {
            keywords.add(arguments.next().toLowerCase());
        }
        if (keywords.isEmpty()) {
            msg(sender, "manual.search.usage");
            return;
        }
        Configuration config = HamsterEcoHelper.plugin.config;
        long cd = System.currentTimeMillis() + config.searchCooldownTick * 50;
        if (matchLore) cd += config.searchLoreCooldownTick * 50;
        if (matchEnch) cd += config.searchEnchCooldownTick * 50;
        cooldown.put(searcher, cd);

        UUID limitUuid = ownerLimit == null ? null : Bukkit.getOfflinePlayer(ownerLimit).getUniqueId();

        ShopItemManager instance = ShopItemManager.getInstance();
        int shopItemCount = instance.getShopItemCount();
        class SearchTask extends BukkitRunnable{
            private int count;
            private int current;
            private int batchSize;
            private List<ShopItem> result;

            SearchTask(int count, int current, int batchSize, List<ShopItem> result){
                this.count = count;
                this.current = current;
                this.batchSize = batchSize;
                this.result = result;
            }

            @Override
            public void run() {
                List<ShopItem> shopItems = instance.getShopItems(current, batchSize);
                current += batchSize;
                shopItems.stream().forEach(shopItem -> {
                    if (match(shopItem, materialLimit, keywords, matchLoreOnly, matchLore, matchEnchOnly, matchEnch)){
                        result.add(shopItem);
                    }
                });

                if (current < count){
                    new SearchTask(count, current, batchSize, result).runTaskLaterAsynchronously(HamsterEcoHelper.plugin, 1);
                }else {
                    result.sort(Comparator.comparingDouble(ShopItem::getUnitPrice));
                    onSearchComplete(player, result);
                }
            }
        }
        new SearchTask(shopItemCount, 0, 100, new ArrayList<>()).runTaskAsynchronously(HamsterEcoHelper.plugin);
    }

    private void onSearchComplete(Player player, List<ShopItem> result) {
        searchResult.put(player.getUniqueId(), result);
        new Message(I18n.format("command.search.complete")).send(player);
        sendPageInfo(player, result, 0);
    }

    public static boolean match(ShopItem shopItem, Material materialLimit, List<String> keywords, boolean matchLoreOnly, boolean matchLore, boolean matchEnchOnly, boolean matchEnch){
        boolean loreMatch;
        boolean enchMatch;
        ItemStack stack = shopItem.getItemStack();
        stack.setAmount(shopItem.getAmount() - shopItem.getSoldAmount());
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

    public static Material getMaterial(String name) {
        return Material.matchMaterial(name);
    }

    public static Material getMaterial(String name, CommandSender sender) {
        Material m = Material.matchMaterial(name, false);
        if (m == null) {
            m = Material.matchMaterial(name, true);
            if (m != null) {
                //noinspection deprecation
                m = Bukkit.getUnsafe().fromLegacy(m);
                sender.sendMessage(I18n.format("user.warn.legacy_name", name, m.toString()));
            }
        }
        return m;
    }


    @SubCommand(value = "page", permission = PERMISSION_SEARCH)
    public void onPage(CommandSender sender, Arguments arguments){
        Player player = sender instanceof Player ? (Player) sender : null;
        UUID searcher = player == null ? new UUID(2333, 2333) : player.getUniqueId();
        List<ShopItem> result = searchResult.getIfPresent(searcher);
        int page = arguments.nextInt() - 1;
        sendPageInfo(sender, result, page);
    }

    private void sendPageInfo(CommandSender sender, List<ShopItem> result, int page) {
        int start = page * 9;
        if (result == null) {
            msg(sender, "user.signshop.search.no_recent_result");
            return;
        }

        if (start > result.size()) {
            msg(sender, "user.signshop.search.page_out_of_bound");
            return;
        }
        msg(sender, "user.signshop.search.page", page + 1, (int) Math.ceil(result.size() / 9.0d));
        result.stream().skip(start).limit(9).forEach(item ->
                new Message("")
                        .append(I18n.format("user.signshop.search.result",
                                Bukkit.getOfflinePlayer(item.getOwner())
                                        .getName(),
                                item.getUnitPrice()
                        ), item.getItemStack())
                        .send(sender));
    }


    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }
}

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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
        String keyword = arguments.nextString();
        Configuration config = HamsterEcoHelper.plugin.config;
        long cd = System.currentTimeMillis() + config.searchCooldownTick * 50;
        cooldown.put(searcher, cd);


        ShopItemManager instance = ShopItemManager.getInstance();
        int shopItemCount = instance.getShopItemCount();
        new BukkitRunnable(){

            @Override
            public void run() {
                List<ShopItem> result = ShopItemManager.getInstance().searchShopItems(keyword);
                result.sort(Comparator.comparingDouble(ShopItem::getUnitPrice));
                onSearchComplete(player, result);
            }
        }.runTaskAsynchronously(HamsterEcoHelper.plugin);
    }

    private void onSearchComplete(Player player, List<ShopItem> result) {
        searchResult.put(player.getUniqueId(), result);
        new Message(I18n.format("command.search.complete")).send(player);
        sendPageInfo(player, result, 0);
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

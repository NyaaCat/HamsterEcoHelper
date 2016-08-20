package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionManager extends BukkitRunnable {
    private final HamsterEcoHelper plugin;
    private AuctionInstance currentAuction = null;

    public AuctionManager(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        int aucInterval = plugin.config.auctionIntervalTicks;
        this.runTaskTimer(plugin, aucInterval, aucInterval);
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() < plugin.config.auctionMinimalPlayer) {
            plugin.logger.info(I18n.get("internal.info.auc_not_enough_player", Bukkit.getOnlinePlayers().size(), plugin.config.auctionMinimalPlayer));
            return;
        }
        int delay = Utils.inclusiveRandomInt(0, plugin.config.auctionMaxDelayTicks);
        (new BukkitRunnable() {
            @Override
            public void run() {
                newAuction();
            }
        }).runTaskLater(plugin, delay);
        plugin.logger.info(I18n.get("internal.info.auc_scheduled", delay));
    }

    public boolean newAuction(AuctionItemTemplate item) {
        if (plugin.auctionManager != this) return false;
        if (currentAuction != null) return false;
        if (item == null) return false;
        currentAuction = new AuctionInstance(item.getItemStack(),
                item.baseAuctionPrice,
                item.bidStepPrice,
                item.waitTimeTicks,
                item.hideName,
                plugin,
                ()->this.currentAuction = null);
        return true;
    }

    public boolean newAuction() {
        if (currentAuction != null) return false;
        if (plugin.config.itemsForAuction.size() == 0) return false;
        AuctionItemTemplate bidItem = Utils.randomWithWeight(plugin.config.itemsForAuction,
                (AuctionItemTemplate temp) -> temp.randomWeight);
        if (bidItem == null) return false; // wtf?

        return newAuction(bidItem);
    }

    public void halt() {
        if (currentAuction != null) {
            currentAuction.halt();
            currentAuction = null;
            new Message(I18n.get("user.auc.halted")).broadcast("heh.bid");
        }
    }

    public AuctionInstance getCurrentAuction() {
        return currentAuction;
    }
}

package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
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
        if (Bukkit.getOnlinePlayers().size() < plugin.config.auctionMinimalPlayer)
            return;
        (new BukkitRunnable() {
            @Override
            public void run() {
                newAuction();
            }
        }).runTaskLater(plugin, Utils.inclusiveRandomInt(0, plugin.config.auctionMaxDelayTicks));
    }

    public boolean newAuction(AuctionItemTemplate item) {
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

        currentAuction = new AuctionInstance(bidItem.getItemStack(), bidItem.baseAuctionPrice, bidItem.bidStepPrice,
                bidItem.waitTimeTicks, bidItem.hideName, plugin, ()->this.currentAuction = null);
        return true;
    }

    public void halt() {
        if (currentAuction != null)
            currentAuction.halt();
        currentAuction = null;

    }

    public AuctionInstance getCurrentAuction() {
        return currentAuction;
    }
}

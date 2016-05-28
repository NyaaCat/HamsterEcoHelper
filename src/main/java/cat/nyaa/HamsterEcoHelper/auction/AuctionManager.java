package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
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
        newAuction();
    }

    public boolean newAuction() {
        if (currentAuction != null) return false;
        if (plugin.config.itemsForAuction.size() == 0) return false;
        AuctionItemTemplate bidItem = Utils.randomWithWeight(plugin.config.itemsForAuction,
                (AuctionItemTemplate temp) -> temp.randomWeight);
        if (bidItem == null) return false; // wtf?

        currentAuction = new AuctionInstance(bidItem.getItemStack(), bidItem.baseAuctionPrice, bidItem.bidStepPrice,
                plugin.config.bidTimeoutTicks, plugin, ()->this.currentAuction = null);
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

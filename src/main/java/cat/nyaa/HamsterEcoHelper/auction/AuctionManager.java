package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.data.AuctionItemTemplate;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionManager extends BukkitRunnable {
    private final HamsterEcoHelper plugin;
    private AuctionInstance currentAuction = null;

    public AuctionManager(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        int aucIntval = plugin.config.auctionIntervalTicks;
        this.runTaskTimer(plugin, aucIntval, aucIntval);
    }

    @Override
    public void run() {
        newAuction();
    }

    public boolean newAuction() {
        if (currentAuction != null) return false;
        if (plugin.config.itemsForAuction.size() == 0) return false;

        Double[] weight_prefix = new Double[plugin.config.itemsForAuction.size()];
        weight_prefix[0] = plugin.config.itemsForAuction.get(0).randomWeight;
        for (int i = 1; i < plugin.config.itemsForAuction.size(); i++) {
            weight_prefix[i] = weight_prefix[i - 1] + plugin.config.itemsForAuction.get(i).randomWeight;
        }
        double rnd = Math.random() * weight_prefix[weight_prefix.length - 1];
        AuctionItemTemplate bidItem = null;
        for (int i = 0; i < weight_prefix.length; i++) {
            if (weight_prefix[i] > rnd) {
                bidItem = plugin.config.itemsForAuction.get(i);
            }
        }
        if (bidItem == null) return false; // wtf?
        currentAuction = new AuctionInstance(bidItem.getItemStack(), bidItem.baseAuctionPrice, bidItem.bidStepPrice,
                plugin.config.bidTimeoutTicks, plugin, new Runnable() {
            @Override
            public void run() {
                AuctionManager.this.finish();
            }
        });
        return true;
    }

    public void finish() {
        currentAuction = null;
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

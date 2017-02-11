package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class AuctionManager extends BukkitRunnable {
    private final HamsterEcoHelper plugin;
    public HashMap<UUID, Long> cooldown = new HashMap<>();
    private AuctionInstance currentAuction = null;

    public AuctionManager(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        int aucInterval = plugin.config.auctionIntervalTicks;
        this.runTaskTimer(plugin, aucInterval, aucInterval);
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() < plugin.config.auctionMinimalPlayer) {
            plugin.logger.info(I18n._("log.info.auc_not_enough_player", Bukkit.getOnlinePlayers().size(), plugin.config.auctionMinimalPlayer));
            return;
        }
        if (plugin.config.enable_balance && plugin.config.current_balance > 0) {
            return;
        }
        int delay = Utils.inclusiveRandomInt(0, plugin.config.auctionMaxDelayTicks);
        (new BukkitRunnable() {
            @Override
            public void run() {
                newAuction();
            }
        }).runTaskLater(plugin, delay);
        plugin.logger.info(I18n._("log.info.auc_scheduled", delay));
    }

    public boolean newAuction(AuctionItemTemplate item) {
        if (plugin.auctionManager != this) return false;
        if (currentAuction != null) return false;
        if (item == null) return false;
        currentAuction = new AuctionInstance(null, item.getItemStack(),
                item.baseAuctionPrice,
                item.bidStepPrice, 
                0,
                item.waitTimeTicks,
                item.hideName,
                plugin,
                () -> this.currentAuction = null);
        return true;
    }

    public boolean newAuction() {
        if (currentAuction != null) return false;
        if (plugin.config.auctionConfig.itemsForAuction.size() == 0) return false;
        AuctionItemTemplate bidItem = Utils.randomWithWeight(plugin.config.auctionConfig.itemsForAuction,
                (AuctionItemTemplate temp) -> temp.randomWeight);
        if (bidItem == null) return false; // wtf?

        return newAuction(bidItem);
    }

    public boolean newPlayerAuction(Player player, ItemStack item, int basePrice, int stepPrice, int reservePrice) {
        if (currentAuction != null) return false;
        if (!(basePrice > 0.9)) {
            return false;
        }
        if (this.cooldown.containsKey(player.getUniqueId()) && this.cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage(I18n._("user.info.cooldown", (this.cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000));
            return false;
        }
        this.cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerAuctionCooldownTicks / 20 * 1000));

        currentAuction = new AuctionInstance(player, item,
                basePrice,
                stepPrice,
                reservePrice,
                plugin.config.playerAuctionTimeoutTicks,
                false,
                plugin,
                () -> this.currentAuction = null);
        return true;
    }

    public void halt() {
        if (currentAuction != null) {
            currentAuction.halt();
            currentAuction = null;
            new Message(I18n._("user.auc.halted")).broadcast("heh.bid");
        }
    }

    public AuctionInstance getCurrentAuction() {
        return currentAuction;
    }
}

package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class RequisitionManager extends BukkitRunnable {
    private final HamsterEcoHelper plugin;
    public HashMap<UUID, Long> cooldown = new HashMap<>();
    private RequisitionInstance currentReq = null;

    public RequisitionManager(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        int interval = plugin.config.requisitionIntervalTicks;
        runTaskTimer(plugin, interval, interval);
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() < plugin.config.requisitionMinimalPlayer) {
            plugin.logger.info(I18n.format("log.info.req_not_enough_player", Bukkit.getOnlinePlayers().size(), plugin.config.auctionMinimalPlayer));
            return;
        }
        if (plugin.systemBalance.isEnabled() && plugin.systemBalance.getBalance() <= 0D) {
            return;
        }
        int delay = MiscUtils.inclusiveRandomInt(0, plugin.config.requisitionMaxDelayTicks);
        (new BukkitRunnable() {
            @Override
            public void run() {
                newRequisition();
            }
        }).runTaskLater(plugin, delay);
        plugin.logger.info(I18n.format("log.info.req_scheduled", delay));
    }

    public boolean newRequisition() {
        if (currentReq != null) return false;
        if (plugin.config.requisitionConfig.itemsForReq.isEmpty()) return false;
        RequisitionSpecification item = MiscUtils.randomWithWeight(
                plugin.config.requisitionConfig.itemsForReq,
                (RequisitionSpecification i) -> i.randomWeight
        );
        if (item == null) return false;
        return newRequisition(item);
    }

    public boolean newRequisition(RequisitionSpecification item) {
        if (plugin.reqManager != this) return false;
        if (currentReq != null) return false;
        if (item == null) return false;

        int unitPrice = MiscUtils.inclusiveRandomInt(item.minPurchasePrice, item.maxPurchasePrice);
        int amount = item.maxAmount < 0 ? -1 : MiscUtils.inclusiveRandomInt(item.minAmount, item.maxAmount);
        currentReq = new RequisitionInstance(item, unitPrice, amount, plugin, () -> this.currentReq = null);
        if (plugin.config.requisitionHintInterval > 0) {
            currentReq.new RequisitionHintTimer(this, plugin.config.requisitionHintInterval, plugin);
        }
        return true;
    }

    public boolean newPlayerRequisition(Player player, ItemStack item, double unitPrice, int amount, boolean isStrict) {
        if (currentReq != null) return false;
        if (item == null) return false;
        currentReq = new RequisitionInstance(player, item, unitPrice, amount, isStrict, plugin, () -> this.currentReq = null);
        if (plugin.config.requisitionHintInterval > 0) {
            currentReq.new RequisitionHintTimer(this, plugin.config.requisitionHintInterval, plugin);
        }
        return true;
    }

    public void halt() {
        if (currentReq != null) {
            currentReq.halt();
            currentReq = null;
            new Message(I18n.format("user.req.halted")).broadcast(new Permission("heh.sell"));
        }
    }

    public RequisitionInstance getCurrentRequisition() {
        return currentReq;
    }
}

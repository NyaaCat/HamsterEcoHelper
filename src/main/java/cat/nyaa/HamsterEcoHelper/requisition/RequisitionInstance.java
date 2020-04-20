package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

import static cat.nyaa.HamsterEcoHelper.utils.MiscUtils.uid;

public class RequisitionInstance {
    private final Runnable finishCallback;
    private final RequisitionSpecification templateItem;
    private final double unitPrice;
    private final Logger logger;
    public OfflinePlayer owner = null;
    private BukkitRunnable timeoutListener;
    private int soldAmount;
    private int amountRemains;
    private long endTime;
    private HamsterEcoHelper plugin;

    public RequisitionInstance(
            RequisitionSpecification templateItem,
            int unitPrice, int reqAmount,
            HamsterEcoHelper plugin, Runnable finishCallback) {
        this.plugin = plugin;
        this.finishCallback = finishCallback;
        this.unitPrice = unitPrice;
        this.templateItem = templateItem;
        this.amountRemains = reqAmount;
        this.soldAmount = 0;
        this.endTime = System.currentTimeMillis() + templateItem.timeoutTicks * 50;
        timeoutListener = new TimeoutListener();
        timeoutListener.runTaskLater(plugin, templateItem.timeoutTicks);
        ItemStack tmp = templateItem.itemTemplate;
        new Message(I18n.format("user.req.new_req_0")).append("{itemName}", tmp)
                                                      .appendFormat(plugin.i18n, "user.req.new_req_1", reqAmount, (double) unitPrice, (double) templateItem.timeoutTicks / 20D)
                                                      .broadcast();
        String name = MiscUtils.getItemName(templateItem.itemTemplate);
        logger = plugin.getLogger();
        plugin.getLogger().info(I18n.format("log.info.req_start", name, reqAmount, (double) unitPrice, templateItem.timeoutTicks, uid(this)));
    }

    public RequisitionInstance(Player player,
                               ItemStack item,
                               double unitPrice, int reqAmount,
                               boolean isStrict, HamsterEcoHelper plugin, Runnable finishCallback) {
        this.owner = player;
        this.plugin = plugin;
        this.finishCallback = finishCallback;
        this.unitPrice = unitPrice;
        this.templateItem = new RequisitionSpecification();
        this.templateItem.itemTemplate = item;
        this.templateItem.timeoutTicks = plugin.config.playerRequisitionTimeoutTicks;
        this.templateItem.matchRule.requireExact = isStrict;
        this.templateItem.matchRule.enchantMatch = RequisitionSpecification.MatchingMode.EXACT;
        this.templateItem.matchRule.nameMatch = RequisitionSpecification.MatchingMode.EXACT;
        this.templateItem.matchRule.loreMatch = RequisitionSpecification.MatchingMode.EXACT;
        this.templateItem.matchRule.maxDamageValue = -2;
        this.templateItem.matchRule.minDamageValue = -2;
        this.templateItem.matchRule.repairCostMatch = RequisitionSpecification.MatchingMode.EXACT;
        this.amountRemains = reqAmount;
        this.soldAmount = 0;
        this.endTime = System.currentTimeMillis() + (plugin.config.playerRequisitionTimeoutTicks / 20 * 1000);
        timeoutListener = new TimeoutListener();
        timeoutListener.runTaskLater(plugin, plugin.config.playerRequisitionTimeoutTicks);
        new Message(I18n.format("user.req.player_req_0", player.getName())).append("{itemName}", item)
                                                                           .appendFormat(plugin.i18n, "user.req.player_req_1", reqAmount, unitPrice, (double) templateItem.timeoutTicks / 20D)
                                                                           .broadcast();
        String itemName = MiscUtils.getItemName(item);
        logger = plugin.getLogger();
        plugin.getLogger().info(I18n.format("log.info.player_req_start", player.getName(), itemName,
                reqAmount, unitPrice, templateItem.timeoutTicks, uid(this)));

    }

    public boolean canSellAmount(int amount) {
        return amountRemains <= -1 || amountRemains >= amount;
    }

    public int getAmountRemains() {
        return amountRemains;
    }

    void halt() {
        timeoutListener.cancel();
        if (owner != null && amountRemains > 0) {
            plugin.eco.deposit(owner, amountRemains * unitPrice);
            amountRemains = 0;
            plugin.reqManager.cooldown.put(owner.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerRequisitionCooldownTicks * 50));
        }
        logger.info(I18n.format("log.info.req_finish", uid(this), soldAmount, "HALTED"));
        if (owner == null && plugin.systemBalance.isEnabled()) {
            plugin.logger.info(I18n.format("log.info.current_balance", plugin.systemBalance.getBalance()));
            plugin.config.save();
        }
    }

    /**
     * @return zero or positive: give that much money to player
     * -1: not enough item in hand
     * -2: item not match
     */
    public double purchase(Player p, int amount) {
        ItemStack itemHand = p.getInventory().getItemInMainHand();
        if (itemHand.getAmount() < amount) return -1;
        if (!templateItem.matchRule.matches(itemHand)) return -2;
        if (amountRemains < amount && amountRemains >= 0) amount = amountRemains;
        int new_amount = itemHand.getAmount() - amount;

        if (owner != null) {
            ItemStack tmp = itemHand.clone();
            tmp.setAmount(amount);
            MiscUtils.giveItem(owner, tmp);
        }

        if (new_amount == 0) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            itemHand.setAmount(new_amount);
        }

        if (amountRemains >= 0) amountRemains -= amount;
        soldAmount += amount;
        new Message(I18n.format("user.req.sold_amount_0", p.getName(), amount))
                .append("{itemName}", templateItem.itemTemplate)
                .appendFormat(plugin.i18n, "user.req.sold_amount_1", amountRemains)
                .broadcast();
        logger.info(I18n.format("log.info.req_sell", uid(this), amount, amountRemains, p.getName()));
        if (amountRemains == 0) {
            new Message(I18n.format("user.req.sold_out")).broadcast(new Permission("heh.bid"));
            halt();
            logger.info(I18n.format("log.info.req_finish", uid(this), soldAmount, "SOLD_OUT"));
            finishCallback.run();
        }
        return unitPrice * amount;
    }

    private class TimeoutListener extends BukkitRunnable {
        @Override
        public void run() {
            finishCallback.run();
            if (owner != null && amountRemains > 0) {
                plugin.eco.deposit(owner, amountRemains * unitPrice);
                amountRemains = 0;
                plugin.reqManager.cooldown.put(owner.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerRequisitionCooldownTicks * 50));
            }
            new Message(I18n.format("user.req.finish")).broadcast(new Permission("heh.bid"));
            logger.info(I18n.format("log.info.req_finish", uid(RequisitionInstance.this), soldAmount, "TIMEOUT"));
            if (owner == null && plugin.systemBalance.isEnabled()) {
                plugin.logger.info(I18n.format("log.info.current_balance", plugin.systemBalance.getBalance()));
                plugin.config.save();
            }
        }
    }

    public class RequisitionHintTimer extends BukkitRunnable {
        private final RequisitionManager manager;

        public RequisitionHintTimer(RequisitionManager manager, int interval, JavaPlugin plugin) {
            super();
            this.manager = manager;
            runTaskTimer(plugin, interval, interval);
        }

        @Override
        public void run() {
            if (RequisitionInstance.this != manager.getCurrentRequisition()
                        || amountRemains <= 0
                        || endTime - System.currentTimeMillis() < 100) {
                cancel();
                if (owner != null && amountRemains > 0) {
                    plugin.eco.deposit(owner, amountRemains * unitPrice);
                    amountRemains = 0;
                    plugin.reqManager.cooldown.put(owner.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerRequisitionCooldownTicks * 50));
                }
            } else {
                new Message(I18n.format("user.req.hint_req_0")).append("{itemName}", templateItem.itemTemplate)
                                                               .appendFormat(plugin.i18n, "user.req.hint_req_1", amountRemains, unitPrice, ((double) (endTime - System.currentTimeMillis())) / 1000D)
                                                               .broadcast();
            }

        }
    }
}

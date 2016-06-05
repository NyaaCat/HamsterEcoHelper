package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

import static cat.nyaa.HamsterEcoHelper.utils.Utils.uid;

public class RequisitionInstance {
    private final Runnable finishCallback;
    private BukkitRunnable timeoutListener;

    private final RequisitionSpecification templateItem;
    private final int unitPrice;
    private final Logger logger;
    private int soldAmount;
    private int amountRemains;
    private long endTime;

    public RequisitionInstance(
            RequisitionSpecification templateItem,
            int unitPrice, int reqAmount,
            JavaPlugin plugin, Runnable finishCallback)
    {
        this.finishCallback = finishCallback;
        this.unitPrice = unitPrice;
        this.templateItem = templateItem;
        this.amountRemains = reqAmount;
        this.soldAmount = 0;
        this.endTime = System.currentTimeMillis() + templateItem.timeoutTicks * 50;
        timeoutListener = new TimeoutListener();
        timeoutListener.runTaskLater(plugin, templateItem.timeoutTicks);
        ItemStack tmp = templateItem.itemTemplate;
        new Message(I18n.get("user.req.new_req_0")).append(tmp,"{itemName}")
                .appendFormat("user.req.new_req_1", reqAmount, unitPrice, (double)templateItem.timeoutTicks / 20D)
                .broadcast();
        String name = templateItem.itemTemplate.hasItemMeta() && templateItem.itemTemplate.getItemMeta().hasDisplayName()?
                templateItem.itemTemplate.getItemMeta().getDisplayName():
                templateItem.itemTemplate.getType().name() + ":" + templateItem.itemTemplate.getDurability();
        logger = plugin.getLogger();
        plugin.getLogger().info(I18n.get("internal.info.req_start", name, reqAmount, unitPrice, templateItem.timeoutTicks, uid(this)));
    }

    public boolean canSellAmount(int amount) {
        return amountRemains <= -1 || amountRemains >= amount;
    }

    public int getAmountRemains() {
        return amountRemains;
    }

    void halt() {
        timeoutListener.cancel();
        logger.info(I18n.get("internal.info.req_finish", uid(this), soldAmount, "HALTED"));
    }

    /**
     * @return zero or positive: give that much money to player
     *         -1: not enough item in hand
     *         -2: item not match
     */
    public int purchase(Player p, int amount) {
        ItemStack itemHand = p.getInventory().getItemInMainHand();
        if (itemHand.getAmount() < amount) return -1;
        if (!templateItem.matchRule.matches(itemHand)) return -2;
        if (amountRemains < amount && amountRemains >= 0) amount = amountRemains;
        int new_amount = itemHand.getAmount() - amount;
        if (new_amount == 0) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            itemHand.setAmount(new_amount);
        }
        if (amountRemains >= 0) amountRemains -= amount;
        soldAmount += amount;
        new Message(I18n.get("user.req.sold_amount_0", p.getName(), amount))
                .append(templateItem.itemTemplate, "{itemName}")
                .appendFormat("user.req.sold_amount_1", amountRemains)
                .broadcast();
        logger.info(I18n.get("internal.info.req_sell", uid(this), amount, amountRemains, p.getName()));
        if (amountRemains == 0) {
            new Message(I18n.get("user.req.sold_out")).broadcast("heh.bid");
            halt();
            logger.info(I18n.get("internal.info.req_finish", uid(this), soldAmount, "SOLD_OUT"));
            finishCallback.run();
        }
        return unitPrice * amount;
    }

    private class TimeoutListener extends BukkitRunnable {
        @Override
        public void run() {
            finishCallback.run();
            new Message(I18n.get("user.req.finish")).broadcast("heh.bid");
            logger.info(I18n.get("internal.info.req_finish", uid(RequisitionInstance.this), soldAmount, "TIMEOUT"));
        }
    }

    public class RequisitionHintTimer extends BukkitRunnable{
        private final RequisitionManager manager;
        public RequisitionHintTimer(RequisitionManager manager, int interval, JavaPlugin plugin) {
            super();
            this.manager = manager;
            runTaskTimer(plugin, interval, interval);
        }

        @Override
        public void run() {
            if (RequisitionInstance.this != manager.getCurrentRequisition()) {
                cancel();
            } else {
                new Message(I18n.get("user.req.hint_req_0")).append(templateItem.itemTemplate,"{itemName}")
                        .appendFormat("user.req.hint_req_1", amountRemains, unitPrice, ((double)(endTime - System.currentTimeMillis())) / 1000D)
                        .broadcast();
            }

        }
    }
}

package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RequisitionInstance {
    private final Runnable finishCallback;
    private BukkitRunnable timeoutListener;

    private final RequisitionSpecification templateItem;
    private final int unitPrice;
    private int amountRemains;

    public RequisitionInstance(
            RequisitionSpecification templateItem,
            int unitPrice, int reqAmount,
            JavaPlugin plugin, Runnable finishCallback)
    {
        this.finishCallback = finishCallback;
        this.unitPrice = unitPrice;
        this.templateItem = templateItem;
        this.amountRemains = reqAmount;
        timeoutListener = new TimeoutListener();
        timeoutListener.runTaskLater(plugin, templateItem.timeoutTicks);
        ItemStack tmp = templateItem.itemTemplate;
        new Message(I18n.get("user.req.new_req_0")).append(tmp,"{itemName}")
                .appendFormat("user.req.new_req_1", reqAmount, unitPrice, (double)templateItem.timeoutTicks / 20D)
                .broadcast();
    }

    public boolean canSellAmount(int amount) {
        return amountRemains == -1 || amountRemains >= amount;
    }

    public int getAmountRemains() {
        return amountRemains;
    }

    void halt() {
        timeoutListener.cancel();
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
        if (amountRemains < amount) amount = amountRemains;
        int new_amount = itemHand.getAmount() - amount;
        if (new_amount == 0) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            itemHand.setAmount(new_amount);
        }
        amountRemains -= amount;
        if (amountRemains == 0) {
            Bukkit.broadcast(I18n.get("user.req.sold_out"), "heh.bid");
            halt();
            finishCallback.run();
        }
        return unitPrice * amount;
    }

    private class TimeoutListener extends BukkitRunnable {
        @Override
        public void run() {
            finishCallback.run();
            Bukkit.broadcast(I18n.get("user.req.finish"), "heh.bid");
        }
    }
}

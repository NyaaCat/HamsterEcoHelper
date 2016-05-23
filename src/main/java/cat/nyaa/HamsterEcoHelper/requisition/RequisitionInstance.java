package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.data.RequisitionSpecification;
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

    public RequisitionInstance(
            RequisitionSpecification templateItem,
            int unitPrice, int reqAmount,
            JavaPlugin plugin, Runnable finishCallback)
    {
        this.finishCallback = finishCallback;
        this.unitPrice = unitPrice;
        this.templateItem = templateItem;
        timeoutListener = new TimeoutListener();
        timeoutListener.runTaskLater(plugin, templateItem.timeoutTicks);
        ItemStack tmp = templateItem.itemTemplate;
        String name = tmp.hasItemMeta() ? tmp.getItemMeta().getDisplayName() : tmp.getType().name();
        Bukkit.broadcast(I18n.get("user.req.new_req", name, reqAmount, unitPrice, (double)templateItem.timeoutTicks / 20D), "heh.sell");
    }

    void halt() {
        timeoutListener.cancel();
    }

    public int purchase(Player p, int amount) {
        //TODO
        if (p.getInventory().getItemInMainHand().equals(templateItem.itemTemplate)) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return unitPrice;
        } else {
            return 0;
        }
    }

    private class TimeoutListener extends BukkitRunnable {
        @Override
        public void run() {
            finishCallback.run();
            Bukkit.broadcast(I18n.get("user.auc.finish"), "heh.bid");
        }
    }
}

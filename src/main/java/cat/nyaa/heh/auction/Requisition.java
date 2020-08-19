package cat.nyaa.heh.auction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ListIterator;

public class Requisition {
    public static Requisition currentRequisition;
    private static Permission permission = new Permission("heh.req");
    private boolean hasRequisition = false;

    public boolean isHasRequisition() {
        return hasRequisition;
    }

    private ShopItem item;
    private int duration;

    private int state = 0;
    private RequisitionTask requisitionTask;

    public Requisition(ShopItem shopItem){
        ItemStack is = shopItem.getItemStack();
        checkShulkerBox(is);
        this.item = shopItem;
        this.duration = HamsterEcoHelper.plugin.config.requisitionDuration;
    }

    private void checkShulkerBox(ItemStack is) {
        ItemMeta itemMeta = is.getItemMeta();
        if (itemMeta instanceof BlockStateMeta &&
                ((BlockStateMeta) itemMeta).getBlockState() instanceof ShulkerBox) {
            ShulkerBox blockState = (ShulkerBox) ((BlockStateMeta) itemMeta).getBlockState();
            ListIterator<ItemStack> iterator = blockState.getInventory().iterator();
            while (iterator.hasNext()){
                ItemStack next = iterator.next();
                if (next!=null && !next.getType().isAir()){
                    throw new IllegalArgumentException("shulker box with content is not supported.");
                }
            }
        }
    }

    public void start(){
        if (currentRequisition != null){
            throw new IllegalStateException("there's already a requisition running.");
        }
        currentRequisition = this;
        hasRequisition = true;
        state = 0;
        requisitionTask = new RequisitionTask(this);
        requisitionTask.runTaskLater(HamsterEcoHelper.plugin, duration/4);
        String name = Bukkit.getOfflinePlayer(item.getOwner()).getName();
        if (item.isOwnedBySystem()){
            name = SystemAccountUtils.getSystemName();
        }
        broadcast(new Message("").append(I18n.format("requisition.start.player", name, duration/20), getItem()));
    }

    private ItemStack getItem() {
        ItemStack itemStack = item.getItemStack();
        int amount = itemStack.getAmount();
        itemStack.setAmount(amount);
        return itemStack;
    }

    private boolean giveTo(Inventory inventory, ItemStack itemStack) {
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (InventoryUtils.addItem(inventory, itemStack)){
                return true;
            }
        }
        return false;
    }

    public boolean onSell(Player seller, ItemStack sellItem){
        if (!isValidItem(sellItem)) {
            throw new IllegalArgumentException("not similar item");
        }

        boolean result = TransactionController.getInstance().makeTransaction(item.getOwner(), seller.getUniqueId(), item, sellItem.getAmount());
        if (result){
            broadcast(new Message("").append(I18n.format("requisition.sell", seller.getName(), item.getAmount() - item.getSoldAmount()), sellItem));
        }
        return result;
    }

    public boolean isValidItem(ItemStack sellItem) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.itemTemplate = getItem();
        return itemMatcher.matches(sellItem);
    }

    public static void broadcast(Message message){
        message.broadcast(permission);
    }

    private class RequisitionTask extends BukkitRunnable {
        private Requisition requisition;

        public RequisitionTask(Requisition requisition) {
            this.requisition = requisition;
        }

        @Override
        public void run() {
            requisition.state++;
            if (requisition.state < 4){
                int remain = item.getAmount() - item.getSoldAmount();
                String name = Bukkit.getOfflinePlayer(item.getOwner()).getName();
                if (item.isOwnedBySystem()){
                    name = SystemAccountUtils.getSystemName();
                }
                broadcast(new Message("").append(I18n.format("requisition.info.player", name, remain, (duration - state*(duration/4))/20), getItem()));

                requisitionTask = new RequisitionTask(requisition);
                requisitionTask.runTaskLater(HamsterEcoHelper.plugin, duration/4);
            }else {
                requisition.onStop();
            }
       }
    }

    private void onStop() {
        requisitionTask.cancel();
        hasRequisition = false;
        currentRequisition = null;
        broadcast(new Message(I18n.format("requisition.stop")));
    }
}

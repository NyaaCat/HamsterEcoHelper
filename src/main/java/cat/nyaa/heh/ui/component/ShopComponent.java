package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.utils.ClickUtils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ShopComponent extends BasePagedComponent<ShopItem>{
    static UiTaskHandler uiTaskHandler = new UiTaskHandler();

    public ShopComponent(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        UUID uniqueId = event.getWhoClicked().getUniqueId();
        if (checkCD(uniqueId)) {
            return;
        }
        clickChecker.click(uniqueId, getClickCD());
        ShopItem shopItem = getContent(event);
        if (shopItem == null){
            return;
        }

        UUID buyer = uniqueId;
        if (buyer.equals(shopItem.getOwner())){
            addOnCursor(event, shopItem, 1);
        }else{
            BukkitRunnable buyTask = new BukkitRunnable() {
                @Override
                public void run() {
                    makeTransaction(event, buyer, shopItem);
                    onPostTransaction();
                }
            };
            submitUiTask(uniqueId, buyTask);
        }
    }

    protected abstract void submitUiTask(UUID uniqueId, BukkitRunnable buyTask);

    private int getClickCD() {
        return 10;
    }

    ClickUtils clickChecker = new ClickUtils();

    private boolean checkCD(UUID clicker) {
        return clickChecker.isMultiClick(clicker);
    }

    protected void makeTransaction(InventoryClickEvent event, UUID buyer, ShopItem shopItem) {
        double fee = getFee();
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1, fee, getReason());
    }

    protected abstract String getReason();

    protected abstract double getFee();

    protected void addOnCursor(InventoryClickEvent event, ShopItem shopItem, int amount) {
        ItemStack itemStack = shopItem.getItemStack();
        itemStack.setAmount(amount);
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()){
            shopItem.setAmount(shopItem.getAmount() - amount);
            event.getView().setCursor(itemStack);
        }else if (cursor.isSimilar(itemStack) && cursor.getAmount() < cursor.getMaxStackSize()){
            ItemStack clone = cursor.clone();
            int amountBefore = clone.getAmount();
            int amountAfter = Math.min(clone.getMaxStackSize(), clone.getAmount() + amount);
            clone.setAmount(amountAfter);
            event.getView().setCursor(clone);
            shopItem.setAmount(shopItem.getAmount() - (amountAfter - amountBefore));
        }
        ShopItemManager.getInstance().updateShopItem(shopItem);
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        ShopItem shopItem = getContent(event);
        if (shopItem == null){
            return;
        }
        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            addOnCursor(event, shopItem, 1);
        }else {
            double fee = getFee();
            TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1, fee, getReason());
        }
        onPostTransaction();
    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {
        ShopItem shopItem = getContent(event);
        if (shopItem == null){
            return;
        }
        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            ItemStack itemStack = shopItem.getItemStack();
            int amount = shopItem.getAmount() - shopItem.getSoldAmount();
            itemStack.setAmount(amount);
            shopItem.setAmount(shopItem.getAmount() - amount);
            giveToPlayer(event.getWhoClicked(), itemStack);
            DatabaseManager.getInstance().updateShopItem(shopItem);
        }else {
            double fee = getFee();
            TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, shopItem.getAmount() - shopItem.getSoldAmount(), fee, getReason());
        }
        onPostTransaction();
    }

    private void giveToPlayer(HumanEntity player, ItemStack itemStack) {
        if (!giveTo(player.getInventory(), itemStack)){
            if (!giveTo(player.getEnderChest(), itemStack)) {
                StorageConnection.getInstance().getPlayerStorage(player.getUniqueId()).addItem(itemStack, 0);
                new Message(I18n.format("item.give.temp_storage")).send(player);
                return;
            }
            new Message(I18n.format("item.give.ender_chest")).send(player);
            return;
        }
    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player){
            Player player = (Player) event.getWhoClicked();
            if(!player.isOp()){
                return;
            }
        }
        ShopItem shopItem = getContent(event);
        ItemStack clone = shopItem.getItemStack().clone();
        clone.setAmount(clone.getMaxStackSize());
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().equals(Material.AIR)){
            event.getView().setCursor(clone);
        }else if (cursor.isSimilar(clone)){
            cursor.setAmount(cursor.getMaxStackSize());
            event.getView().setCursor(cursor);
        }
    }

    private boolean giveTo(Inventory inventory, ItemStack itemStack) {
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (InventoryUtils.addItem(inventory, itemStack)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDrag(InventoryClickEvent event) {

    }

    protected abstract void onPostTransaction();

    static class UiTaskHandler extends BukkitRunnable{
        Map<UUID, List<BukkitRunnable>> uiEvts = new HashMap<>();

        @Override
        public void run() {
            if (uiEvts.isEmpty()){
                return;
            }
            uiEvts.forEach((k,v) -> {
                if (v.size() > 1) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
                    if (offlinePlayer.isOnline()){
                        offlinePlayer.getPlayer().sendMessage(I18n.format("ui.message.click_too_fast"));
                    }
                    return;
                }
                if (v.size() == 1) {
                    BukkitRunnable bukkitRunnable = v.get(0);
                    bukkitRunnable.run();
                }
            });
            uiEvts.clear();
        }
    }
}

package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class ShopComponent extends BasePagedComponent<ShopItem>{
    public ShopComponent(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        ShopItem shopItem = getShopItem(event);
        if (shopItem == null){
            return;
        }

        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            addOnCursor(event, shopItem, shopItem.getAmount() - shopItem.getSoldAmount());
        }else{
            makeTransaction(event, buyer, shopItem);
        }
        loadData();
        refreshUi();
    }

    protected void makeTransaction(InventoryClickEvent event, UUID buyer, ShopItem shopItem) {
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1);
    }

    protected void addOnCursor(InventoryClickEvent event, ShopItem shopItem, int amount) {
        ItemStack itemStack = shopItem.getItemStack();
        itemStack.setAmount(amount);
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()){
            shopItem.setAmount(shopItem.getAmount() - amount);
            event.getView().setCursor(itemStack);
        }else if (cursor.isSimilar(itemStack) && cursor.getAmount() < cursor.getMaxStackSize()){
            shopItem.setAmount(shopItem.getAmount() - amount);
            itemStack.setAmount(Math.min(cursor.getAmount() + amount, cursor.getMaxStackSize()));
            event.getView().setCursor(itemStack);
        }
        ShopItemManager.getInstance().updateShopItem(shopItem);
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        //todo
        ShopItem shopItem = getShopItem(event);
        if (shopItem == null){
            return;
        }
        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            addOnCursor(event, shopItem, 1);
        }else {
            TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1);
        }
        loadData();
        refreshUi();
    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {
        //todo
        ShopItem shopItem = getShopItem(event);
        if (shopItem == null){
            return;
        }
        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            ItemStack itemStack = shopItem.getItemStack();
            int amount = shopItem.getAmount() - shopItem.getSoldAmount();
            itemStack.setAmount(amount);
            shopItem.setAmount(shopItem.getAmount() - amount);
            giveTo(event.getWhoClicked().getInventory(), itemStack);
            DatabaseManager.getInstance().updateShopItem(shopItem);
        }else {
            TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, shopItem.getAmount() - shopItem.getSoldAmount());
        }
        loadData();
        refreshUi();
    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player){
            Player player = (Player) event.getWhoClicked();
            if(!player.isOp()){
                return;
            }
        }
        ItemStack clone = event.getCurrentItem().clone();
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
}

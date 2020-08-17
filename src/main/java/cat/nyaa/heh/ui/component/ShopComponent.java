package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class ShopComponent extends BasePagedComponent{
    public ShopComponent(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        //todo
        ShopItem shopItem = getShopItem(event);
        if (shopItem == null){
            return;
        }

        UUID buyer = event.getWhoClicked().getUniqueId();
        if (buyer.equals(shopItem.getOwner())){
            addOnCursor(event, shopItem);
            return;
        }
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1);
        loadData();
        refreshUi();
    }

    private void addOnCursor(InventoryClickEvent event, ShopItem shopItem) {
        ItemStack itemStack = shopItem.getItemStack();
        int amount = shopItem.getAmount();
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()){
            shopItem.setAmount(amount - 1);
            event.getView().setCursor(itemStack);
        }else if (cursor.isSimilar(itemStack) && cursor.getAmount() < cursor.getMaxStackSize()){
            shopItem.setAmount(amount - 1);
            itemStack.setAmount(cursor.getAmount() + 1);
            event.getView().setCursor(itemStack);
        }
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
            addOnCursor(event, shopItem);
            return;
        }
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1);
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
            addOnCursor(event, shopItem);
            return;
        }
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, shopItem.getAmount() - shopItem.getSoldAmount());
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

    @Override
    public void onDrag(InventoryClickEvent event) {

    }
}

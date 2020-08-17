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
        TransactionController.getInstance().makeTransaction(buyer, shopItem.getOwner(), shopItem, 1);
        loadData();
        refreshUi();
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        //todo
        ShopItem shopItem = getShopItem(event);
        if (shopItem == null){
            return;
        }
        UUID buyer = event.getWhoClicked().getUniqueId();
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
        }
    }

    @Override
    public void onDrag(InventoryClickEvent event) {

    }
}

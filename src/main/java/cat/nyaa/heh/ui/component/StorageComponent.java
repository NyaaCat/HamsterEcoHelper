package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.StorageConnection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class StorageComponent extends BasePagedComponent<StorageItem> {
    private UUID owner;

    public StorageComponent(UUID owner, Inventory inventory) {
        super(inventory);
        this.owner = owner;
    }

    @Override
    public void loadData() {
        List<StorageItem> storage = StorageConnection.getInstance().getStorage(owner);
        this.items = storage;
    }

    @Override
    public void loadData(List<StorageItem> data) {
        this.items = data;
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {

    }

    @Override
    public void onRightClick(InventoryClickEvent event) {

    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {

    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {

    }

    @Override
    public void onDrag(InventoryClickEvent event) {

    }
}

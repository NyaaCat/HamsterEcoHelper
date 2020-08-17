package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.item.ShopItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePagedComponent extends BaseComponent<ShopItem> implements IPagedUiAccess {
    public BasePagedComponent(Inventory inventory) {
        super(0, 0, 5, 9);
        int initialCapacity = rows() * columns();
        this.uiInventory = inventory;
    }

    @Override
    public void setPage(int page) {
        currentPage = page;
        updateAsynchronously();
    }

    @Override
    public void preUpdate() {
        int pageSize = getPageSize();
        ItemStack itemStack = new ItemStack(Material.AIR);
        for (int i = 0; i < pageSize; i++) {
            setItemAt(i, itemStack);
        }
    }

    protected int currentPage = 0;

    protected ShopItem getShopItem(InventoryClickEvent event) {
        int i = indexOf(event.getSlot());
        if (i == -1){
            return null;
        }
        int index = getCurrentPage() * getPageSize() + i;
        return items.stream().skip(index).findFirst().orElse(null);
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getPageSize() {
        return columns() * rows();
    }

    @Override
    public int getSize() {
        return items.size();
    }
}

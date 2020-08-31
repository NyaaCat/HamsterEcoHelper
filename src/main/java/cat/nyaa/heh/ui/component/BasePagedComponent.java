package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.business.item.ModelableItem;
import cat.nyaa.heh.business.item.ShopItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BasePagedComponent<E extends ModelableItem> extends BaseComponent<E> implements IPagedUiAccess {
    public BasePagedComponent(Inventory inventory) {
        super(0, 0, 5, 9);
        this.uiInventory = inventory;
    }

    @Override
    public void setPage(int page) {
        currentPage = page;
    }

    @Override
    public void preUpdate() {
//        int pageSize = getPageSize();
//        ItemStack itemStack = new ItemStack(Material.AIR);
//        for (int i = 0; i < pageSize; i++) {
//            setItemAt(i, itemStack);
//        }
    }

    protected int currentPage = 0;

    protected E getShopItem(InventoryClickEvent event) {
        int i = indexOf(event.getSlot());
        if (i == -1){
            return null;
        }
        int index = getCurrentPage() * getPageSize() + i;
        return items.stream().skip(index).findFirst().orElse(null);
    }

    @Override
    public void refreshUi() {
        List<ItemStack> collect = items.stream().skip(getPageSize() * getCurrentPage())
                .limit(getPageSize())
                .map(itemstack -> itemstack.getModel())
                .collect(Collectors.toList());
        int size = collect.size();
        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i < getPageSize(); i++) {
            if (i >= size) {
                setItemAt(i, air);
                continue;
            }
            setItemAt(i, collect.get(i));
        }
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

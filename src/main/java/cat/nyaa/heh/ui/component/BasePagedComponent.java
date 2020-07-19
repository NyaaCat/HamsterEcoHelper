package cat.nyaa.heh.ui.component;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePagedComponent extends MatrixComponent implements IPagedUiAccess, RefreshableUi {

    public BasePagedComponent(Inventory inventory) {
        super(0, 0, 5, 9);
        this.loadData();
        this.uiInventory = inventory;
    }

    protected Inventory uiInventory;

    protected List<ItemStack> items = new ArrayList<>();

    protected int currentPage = 0;

    protected void setItemAt(int index, ItemStack itemStack){
        int row = index / columns();
        int col = index % columns();
        setItemAt(row, col, itemStack);
    }

    protected void setItemAt(int row, int col, ItemStack itemStack){
        uiInventory.setItem(access(row, col), itemStack);
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

    @Override
    public Inventory getInventory() {
        return uiInventory;
    }

    @Override
    public Map<String, String> getInfo() {
        HashMap<String, String> info = new HashMap<>();
        return info;
    }
}

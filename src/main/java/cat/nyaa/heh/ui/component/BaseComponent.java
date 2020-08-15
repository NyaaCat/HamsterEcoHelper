package cat.nyaa.heh.ui.component;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseComponent<T> extends MatrixComponent implements InventoryHolder, InfoHolder, RefreshableUi<T>, ClickEventHandler{
    protected Inventory uiInventory;

    public BaseComponent(int startRow, int startCol, int rows, int columns) {
        super(startRow, startCol, rows, columns);
        this.loadData();
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

    protected void setItemAt(int index, ItemStack itemStack){
        int row = index / columns();
        int col = index % columns();
        setItemAt(row, col, itemStack);
    }

    protected void setItemAt(int row, int col, ItemStack itemStack){
        uiInventory.setItem(access(row, col), itemStack);
    }

    protected List<ItemStack> items = new ArrayList<>();
}

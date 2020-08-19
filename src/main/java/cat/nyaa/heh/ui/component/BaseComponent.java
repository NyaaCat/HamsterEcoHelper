package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.Tax;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseComponent<T> extends MatrixComponent implements InventoryHolder, InfoHolder, RefreshableUi<T>, ClickEventHandler{
    protected Inventory uiInventory;

    protected List<ShopItem> items = new ArrayList<>();

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
}

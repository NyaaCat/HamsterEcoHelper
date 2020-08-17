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
    private static final String KEY_MODEL = "heh_model";
    private static final NamespacedKey NAMESPACED_KEY_MODEL = new NamespacedKey(HamsterEcoHelper.plugin, KEY_MODEL);
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

    public static ItemStack getFakeItem(ShopItem shopItem) {
        ItemStack itemStack = shopItem.getItemStack();
        itemStack.setAmount(shopItem.getAmount() - shopItem.getSoldAmount());

        ItemStack fakeItem = new ItemStack(itemStack.getType());
        ItemMeta itemMeta = itemStack.getItemMeta();
        ItemMeta fakeMeta = fakeItem.getItemMeta();
        if (itemMeta == null){
            fakeItem = new ItemStack(Material.AIR);
            return fakeItem;
        }
        fakeItem.setAmount(itemStack.getAmount());
        List<String> lore = itemMeta.getLore();
        if (lore == null){
            lore = new ArrayList<>();
        }
        String owner = shopItem.isOwnedBySystem() ? I18n.format("system.name") : Bukkit.getOfflinePlayer(shopItem.getOwner()).getName();
        lore.add(I18n.format("fake_item_lore", owner, shopItem.getUnitPrice(), Tax.calcTax(shopItem, BigDecimal.valueOf(shopItem.getUnitPrice()))));
        if (fakeMeta == null) return null;
        String displayName = itemMeta.getDisplayName();
        if (!displayName.equals("")){
            fakeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+ displayName));
        }
        fakeMeta.setLore(lore);
        if (itemMeta.hasCustomModelData()){
            fakeMeta.setCustomModelData(itemMeta.getCustomModelData());
        }
        Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
        if (!enchants.isEmpty()) {
            enchants.forEach((enchantment, integer) -> {
                fakeMeta.addEnchant(enchantment, integer, true);
            });
        }
        if (itemMeta instanceof LeatherArmorMeta && fakeMeta instanceof LeatherArmorMeta){
            ((LeatherArmorMeta) fakeMeta).setColor(((LeatherArmorMeta) itemMeta).getColor());
        }
        markSample(fakeMeta);
        fakeItem.setItemMeta(fakeMeta);
        return fakeItem;
    }

    public static void markSample(ItemMeta fakeMeta) {
        fakeMeta.getPersistentDataContainer().set(NAMESPACED_KEY_MODEL, PersistentDataType.INTEGER, 1);
    }
}

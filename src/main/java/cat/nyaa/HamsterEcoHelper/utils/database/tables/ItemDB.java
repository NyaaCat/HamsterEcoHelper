package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@DataTable("nbt_data")
public class ItemDB {
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("item")
    private ItemStack item;

    public ItemStack getItemStack() {
        return item.clone();
    }

    public void setItemStack(ItemStack item) {
        this.item = item.clone();
        this.item.setAmount(1);
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = getItemStack();
        item.setAmount(amount);
        return item;
    }
}

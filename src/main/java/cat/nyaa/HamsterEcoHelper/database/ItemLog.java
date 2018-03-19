package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@DataTable("itemlog")
public class ItemLog {
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("owner")
    public UUID owner;
    @DataColumn("item")
    public String item;
    public int amount;
    @DataColumn("price")
    public Double price;

    public ItemStack getItemStack() {
        return getItemStack(amount);
    }

    public void setItemStack(ItemStack item) {
        this.item = ItemStackUtils.itemToBase64(item);
        amount = item.getAmount();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = ItemStackUtils.itemFromBase64(this.item);
        item.setAmount(amount);
        return item;
    }

    @DataColumn("amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }
}
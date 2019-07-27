package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Table("itemlog")
public class ItemLog {
    @Column(primary = true)
    public Long id;
    @Column
    public UUID owner;
    @Column
    public ItemStack item;
    public int amount;
    @Column
    public Double price;

    public ItemStack getItemStack() {
        ItemStack item = this.item.clone();
        item.setAmount(amount);
        return item;
    }

    @Column(name = "amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }
}
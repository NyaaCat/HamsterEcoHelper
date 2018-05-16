package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "itemlog")
@Access(AccessType.FIELD)
public class ItemLog {
    @Column(name = "id")
    @Id
    public Long id;

    public UUID owner;
    @Column(name = "item", columnDefinition = "LONGTEXT")
    public String item;
    public int amount;
    @Column(name = "price")
    public Double price;

    @Access(AccessType.PROPERTY)
    @Column(name = "owner")
    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(String owner) {
        this.owner = UUID.fromString(owner);
    }

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

    @Access(AccessType.PROPERTY)
    @Column(name = "amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }
}
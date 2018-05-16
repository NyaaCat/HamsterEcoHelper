package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
@Table(name= "market")
public class MarketItem {
    @Column(name= "id")
    @Id
    public Long id;
    public UUID playerId;
    @Column(name= "item", columnDefinition = "LONGTEXT")
    public String item;
    public int amount;
    @Column(name= "unit_price")
    public Double unitPrice;


    @Access(AccessType.PROPERTY)
    @Column(name= "player_id")
    @Id
    public String getPlayerId() {
        return playerId.toString();
    }

    public void setPlayerId(String owner) {
        this.playerId = UUID.fromString(owner);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    public ItemStack getItemStack() {
        return getItemStack(amount);
    }

    public void setItemStack(ItemStack item) {
        this.item = ItemStackUtils.itemToBase64(item);
        this.amount = item.getAmount();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = ItemStackUtils.itemFromBase64(this.item);
        item.setAmount(amount);
        return item;
    }

    @Access(AccessType.PROPERTY)
    @Column(name= "amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }
}
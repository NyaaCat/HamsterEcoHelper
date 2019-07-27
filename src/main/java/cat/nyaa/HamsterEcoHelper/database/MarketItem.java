package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Table("market")
public class MarketItem {
    @Column(primary = true)
    public Long id;
    @Column(name = "player_id")
    public UUID playerId;
    @Column
    public ItemStack item;
    public int amount;
    @Column(name = "unit_price")
    public Double unitPrice;

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    public ItemStack getItemStack() {
        return getItemStack(amount);
    }

    public ItemStack getItemStack(int amount) {
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
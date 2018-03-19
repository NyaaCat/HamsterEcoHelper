package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@DataTable("market")
public class MarketItem {
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("player_id")
    public UUID playerId;
    @DataColumn("item")
    public String item;
    public int amount;
    @DataColumn("unit_price")
    public Double unitPrice;

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

    @DataColumn("amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }
}
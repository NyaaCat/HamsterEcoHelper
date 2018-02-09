package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
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
    private Double unitPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID uuid) {
        this.playerId = uuid;
    }

    public OfflinePlayer getPlayer(){
        return Bukkit.getOfflinePlayer(getPlayerId());
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
    
    @DataColumn("amount")
    public Long getAmount() {
        return (long) amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount.intValue();
    }

    @DataColumn("unit_price")
    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unit_price) {
        this.unitPrice = unit_price;
    }

}
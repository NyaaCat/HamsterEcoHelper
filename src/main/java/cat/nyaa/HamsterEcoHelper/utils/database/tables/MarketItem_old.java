package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.UUID;
@Deprecated
@DataTable("market")
public class MarketItem_old {
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("player_id")
    public String playerId;
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
        return UUID.fromString(playerId);
    }

    public void setPlayerId(UUID uuid) {
        this.playerId = uuid.toString();
    }

    public OfflinePlayer getPlayer(){
        return Bukkit.getOfflinePlayer(getPlayerId());
    }
    
    public ItemStack getItemStack() {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(new String(Base64.getDecoder().decode(item)));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ItemStack itemStack = yaml.getItemStack("item");
        itemStack.setAmount((int) this.amount);
        return itemStack;
    }

    public void setItemStack(ItemStack item) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("item", item);
        this.item = Base64.getEncoder().encodeToString(yaml.saveToString().getBytes());
        amount = item.getAmount();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = getItemStack();
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
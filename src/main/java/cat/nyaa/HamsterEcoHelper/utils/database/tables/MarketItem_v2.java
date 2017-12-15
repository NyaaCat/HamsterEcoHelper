package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@DataTable("market_v2")
public class MarketItem_v2 {
    @DataColumn("item_id")
    public Long itemID;
    @DataColumn("id")
    @PrimaryKey
    private Long id;
    @DataColumn("player_id")
    private String playerId;
    @DataColumn("amount")
    private Long amount;
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

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(getPlayerId());
    }


    public int getAmount() {
        return amount.intValue();
    }

    public void setAmount(int amount) {
        this.amount = (long) amount;
    }

    @DataColumn("unit_price")
    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unit_price) {
        this.unitPrice = unit_price;
    }

    public ItemStack getItem() {
        ItemStack item = HamsterEcoHelper.instance.database.getItemByID(itemID);
        item.setAmount(getAmount());
        return item;
    }

    public ItemStack getItem(int amount) {
        ItemStack item = HamsterEcoHelper.instance.database.getItemByID(itemID);
        item.setAmount(amount);
        return item;
    }
}
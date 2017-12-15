package cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@DataTable("signshopitem")
public class SignShopItem {
    @DataColumn("item_id")
    public Long itemID;
    @DataColumn("amount")
    public Long amount;
    @DataColumn("id")
    @PrimaryKey
    private Long id;
    @DataColumn("player_id")
    private String playerId;
    @DataColumn("unit_price")
    private Double unitPrice;
    @DataColumn("type")
    private ShopMode type;

    public ShopMode getType() {
        return type;
    }

    public void setType(ShopMode type) {
        this.type = type;
    }

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

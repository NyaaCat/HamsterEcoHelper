package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@DataTable("marketitem")
public class MarketItem {
    @DataColumn("item_id")
    public Long itemID;
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("player_id")
    public UUID playerId;
    @DataColumn("amount")
    private Long amount;
    @DataColumn("unit_price")
    public Double unitPrice;

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    public int getAmount() {
        return amount.intValue();
    }

    public void setAmount(int amount) {
        this.amount = (long) amount;
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
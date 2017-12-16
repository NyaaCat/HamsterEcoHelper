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
    @DataColumn("unit_price")
    public Double unitPrice;
    @DataColumn("id")
    @PrimaryKey
    public Long id;
    @DataColumn("player_id")
    public UUID playerId;
    @DataColumn("type")
    public ShopMode type;

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

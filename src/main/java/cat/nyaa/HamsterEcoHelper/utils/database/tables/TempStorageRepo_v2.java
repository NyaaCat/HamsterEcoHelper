package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@DataTable("temporary_storage_v2")
public class TempStorageRepo_v2 {
    @DataColumn("player_id")
    @PrimaryKey
    private String playerId;
    @DataColumn("items")
    private String items = "";

    public UUID getPlayerId() {
        return UUID.fromString(playerId);
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId.toString();
    }

    public List<ItemStack> getItems() {
        return ItemStackUtils.itemsFromBase64(items);
    }

    public void setItems(List<ItemStack> items) {
        this.items = ItemStackUtils.itemsToBase64(items);
    }
}
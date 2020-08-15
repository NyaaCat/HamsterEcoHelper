package cat.nyaa.heh.item;

import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.model.ShopItemDbModel;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class ShopItemManager {
    public static Map<Long, ShopItem> shopItemMap = new WeakHashMap<>();

    public static ShopItem getShopItem(long itemID) {
        MarketConnection instance = MarketConnection.getInstance();
        return shopItemMap.computeIfAbsent(itemID, (id) -> instance.loadItemFromDb(itemID));
    }
}

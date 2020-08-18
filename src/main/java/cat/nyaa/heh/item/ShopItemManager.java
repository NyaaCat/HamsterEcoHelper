package cat.nyaa.heh.item;

import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.model.ShopItemDbModel;
import cat.nyaa.heh.utils.UidUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class ShopItemManager {
    private static ShopItemManager INSTANCE;
    private static final String TABLE_NAME = "items";
    private UidUtils uidManager = UidUtils.create(TABLE_NAME);

    private ShopItemManager() {
        uidManager.loadUid();
    }

    public static ShopItemManager getInstance(){
        if (INSTANCE == null){
            synchronized (ShopItemManager.class){
                if (INSTANCE == null) {
                    INSTANCE = new ShopItemManager();
                }
            }
        }
        return INSTANCE;
    }

    public Map<Long, ShopItem> shopItemMap = new WeakHashMap<>();

    public ShopItem getShopItem(long itemID) {
        MarketConnection instance = MarketConnection.getInstance();
        return shopItemMap.computeIfAbsent(itemID, (id) -> instance.loadItemFromDb(itemID));
    }

    public UidUtils getUidManager() {
        return uidManager;
    }

    public long getNextUid() {
        return this.uidManager.getNextUid();
    }

    public void updateShopItem(ShopItem shopItem) {
        DatabaseManager.getInstance().updateShopItem(shopItem);
    }
}

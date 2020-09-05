package cat.nyaa.heh.db;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.ItemFrameShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopSell;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SignShopConnection {
    private static SignShopConnection INSTANCE;

    private SignShopConnection() {
    }

    public static SignShopConnection getInstance() {
        if (INSTANCE == null) {
            synchronized (SignShopConnection.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SignShopConnection();
                }
            }
        }
        return INSTANCE;
    }

    public static boolean isShopFrame(UUID uniqueId) {
        return DatabaseManager.getInstance().getShopFrame(uniqueId) != null;
    }

    public List<ShopItem> getSellShopItems(UUID owner) {
        return DatabaseManager.getInstance().getSellShopItems(owner);
    }

    public List<ShopItem> getBuyShopItems(UUID owner) {
        return DatabaseManager.getInstance().getBuyShopItems(owner);
    }


    public List<BaseSignShop> getShops() {
        return DatabaseManager.getInstance().getShops();
    }

    public Map<UUID, List<SignShopBuy>> getBuyShops() {
        return DatabaseManager.getInstance().getBuyShops();
    }

    public Map<UUID, List<SignShopSell>> getSellShops() {
        return DatabaseManager.getInstance().getSellShops();
    }

    public void addSignShop(BaseSignShop signShopSell) {
        signShopSell.setUid(LocationConnection.getInstance().getNextUid());
        DatabaseManager.getInstance().insertLocation(signShopSell.toDbModel());
    }

    public long addItem(BaseSignShop baseSignShop, ShopItem shopItem) {
        long uid = ShopItemManager.getInstance().getNextUid();
        shopItem.setUid(uid);
        DatabaseManager.getInstance().addShopItem(shopItem);
        return uid;
    }

    public void updateItem(ShopItem item) {
        DatabaseManager.getInstance().updateShopItem(item);
    }

    public void removeShop(BaseSignShop shopAt) {
        DatabaseManager.getInstance().removeShop(shopAt);
    }

    public ItemFrameShop getShopFrame(UUID uniqueId) {
        return new ItemFrameShop(DatabaseManager.getInstance().getShopFrame(uniqueId));
    }

    public Inventory getLottoItems(UUID owner) {
        return DatabaseManager.getInstance().getLottoItems(owner);
    }
}

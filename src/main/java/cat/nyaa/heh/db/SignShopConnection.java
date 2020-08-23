package cat.nyaa.heh.db;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopSell;
import cat.nyaa.heh.utils.UidUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SignShopConnection {
    private static SignShopConnection INSTANCE;
    private static final String TABLE_NAME_SIGN_SHOP = "shop";
    private UidUtils signUidManager = UidUtils.create(TABLE_NAME_SIGN_SHOP);

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

    public SignShopBuy getBuyShop(UUID owner){
        return DatabaseManager.getInstance().getBuyShop(owner);
    }

    public boolean hasBuyShop(UUID owner){
        return DatabaseManager.getInstance().hasBuyShop(owner);
    }

    public SignShopSell getSellShop(UUID owner){
        return DatabaseManager.getInstance().getSellShop(owner);
    }

    public boolean hasSellShop(UUID owner){
        return DatabaseManager.getInstance().hasSellShop(owner);
    }

    public List<ShopItem> getSellShopItems(UUID owner) {
        return DatabaseManager.getInstance().getSellShopItems(owner);
    }

    public List<ShopItem> getBuyShopItems(UUID owner) {
        return DatabaseManager.getInstance().getBuyShopItems(owner);
    }

    public Map<UUID, List<SignShopBuy>> getBuyShops() {
        return DatabaseManager.getInstance().getBuyShops();
    }

    public Map<UUID, List<SignShopSell>> getSellShops() {
        return DatabaseManager.getInstance().getSellShops();
    }

    public void addSignShop(BaseSignShop signShopSell) {
        DatabaseManager.getInstance().addSignShop(signShopSell.toDbModel());
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
}

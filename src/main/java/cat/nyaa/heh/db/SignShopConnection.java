package cat.nyaa.heh.db;

import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.signshop.BaseSignShop;
import cat.nyaa.heh.signshop.SignShopBuy;
import cat.nyaa.heh.signshop.SignShopSell;

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
}

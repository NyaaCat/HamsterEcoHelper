package cat.nyaa.heh.db;

import cat.nyaa.heh.db.model.ShopItemDbModel;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;

import java.util.List;
import java.util.UUID;

public class MarketConnection {
    private static MarketConnection INSTANCE;

    private MarketConnection(){
    }

    public static MarketConnection getInstance(){
        if (INSTANCE == null){
            synchronized (MarketConnection.class){
                if (INSTANCE == null){
                    INSTANCE = new MarketConnection();
                }
            }
        }
        return INSTANCE;
    }

    public void updateItem(ShopItem shopItem){

    }

    public void addItem(ShopItem shopItem) {
        if (!checkShopItem(shopItem)) {
            throw new IllegalArgumentException();
        }
        ShopItemDbModel shopItemDbModel = ShopItemDbModel.fromShopItem(shopItem);
        if (shopItemDbModel.getUid() == -1){
            shopItemDbModel.setUid(ShopItemManager.getInstance().getNextUid());
        }
        DatabaseManager instance = DatabaseManager.getInstance();
        instance.insertShopItem(shopItemDbModel);
    }


    private boolean checkShopItem(ShopItem shopItem) {
        boolean valid;
        valid = shopItem.getAmount() > 0;
        valid = valid && (shopItem.getItemStack() != null && !shopItem.getItemStack().getType().isAir());
        valid = valid && (shopItem.getOwner() != null);
        valid = valid && (shopItem.getUnitPrice() >= 0);
        return valid;
    }

    public ShopItem loadItemFromDb(long itemID) {
        return DatabaseManager.getInstance().getItem(itemID);
    }


    public  ShopItem getItemAt(int slot){
        return null;
    }

    public List<ShopItem> getItems(){
        List<ShopItem> items = DatabaseManager.getInstance().getMarketItems();
        return items;
    }

    public int getItemCount(UUID uniqueId) {
        return DatabaseManager.getInstance().getMarketItemCount(uniqueId);
    }
}

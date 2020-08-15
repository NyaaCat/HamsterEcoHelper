package cat.nyaa.heh.db;

import cat.nyaa.heh.db.model.ShopItemDbModel;
import cat.nyaa.heh.item.ShopItem;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MarketConnection {
    private static MarketConnection INSTANCE;
    public long itemUid = -1;
    private static final String TABLE_NAME = "shop_item";

    private MarketConnection(){
        loadUid();
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
        if (itemUid == -1){
            updateUid();
        }
        if (!checkShopItem(shopItem)) {
            ShopItemDbModel shopItemDbModel = ShopItemDbModel.fromShopItem(shopItem);
            if (shopItemDbModel.getUid() == -1){
                shopItemDbModel.setUid(getNextUid());
            }
            DatabaseManager instance = DatabaseManager.getInstance();
            instance.insertShopItem(shopItemDbModel);
        }else throw new IllegalArgumentException();
    }


    private void updateUid() {
        try {
            itemUid = DatabaseManager.getInstance().getUidMax(TABLE_NAME);
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("failed to get max uid for table %s", TABLE_NAME));
            itemUid = 0;
        }
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


    private long getNextUid() {
        return ++itemUid;
    }

    public void loadUid() {
        updateUid();
    }

    public  ShopItem getItemAt(int slot){
        return null;
    }

    public List<ShopItem> getItems(){
        List<ShopItem> items = new ArrayList<>();
        return items;
    }
}

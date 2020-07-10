package cat.nyaa.hamsterecohelper.item;

import cat.nyaa.hamsterecohelper.db.DatabaseManager;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class ShopItemManager {
    private static ShopItemManager INSTANCE;
    public long itemUid = -1;
    public Map<Long, ShopItem> shopItemMap = new WeakHashMap<>();
    private static final String TABLE_NAME = "shop_item";

    private ShopItemManager(){

    }

    public static ShopItemManager getInstance() {
        if (INSTANCE == null){
            synchronized (ShopItemManager.class){
                if (INSTANCE == null) {
                    INSTANCE = new ShopItemManager();
                }
            }
        }
        return INSTANCE;
    }

    public ShopItem getShopItem(long itemID) {
        return shopItemMap.computeIfAbsent(itemID, (id) -> loadItemFromDb(itemID));
    }

    public void addShopItem(ShopItem shopItem){
        if (itemUid == -1){
            updateUid();
        }
        if (checkShopItem(shopItem)) {
            ShopItemDbModel shopItemDbModel = ShopItemDbModel.fromShopItem(shopItem);
            if (shopItemDbModel.uid == -1){
                shopItemDbModel.uid = getNextUid();
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

    private long getNextUid() {
        return ++itemUid;
    }

    private boolean checkShopItem(ShopItem shopItem) {
        boolean valid;
        valid = shopItem.getAmount() > 0;
        valid = valid && (shopItem.getItemStack() != null && !shopItem.getItemStack().getType().isAir());
        valid = valid && (shopItem.getOwner() != null);
        valid = valid && (shopItem.getUnitPrice() >= 0);
        return valid;
    }

    private ShopItem loadItemFromDb(long itemID) {
        return DatabaseManager.getInstance().getItem(itemID);
    }

    public void loadUid() {
        updateUid();
    }
}

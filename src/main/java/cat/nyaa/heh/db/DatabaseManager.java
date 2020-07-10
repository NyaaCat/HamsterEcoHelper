package cat.nyaa.heh.db;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.enums.ShopItemType;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.item.ShopItemDbModel;
import cat.nyaa.heh.transaction.Transaction;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager INSTANCE;

    private DatabaseConfig databaseConfig;
    IConnectedDatabase db;

    ITypedTable<ShopItemDbModel> shopItemTable;

    private DatabaseManager(){
        databaseConfig = new DatabaseConfig();
        databaseConfig.load();
        try{
            db = DatabaseUtils.connect(HamsterEcoHelper.plugin, databaseConfig.backendConfig);
            shopItemTable = db.getTable(ShopItemDbModel.class);
        }catch (Exception e){
            Bukkit.getPluginManager().disablePlugin(HamsterEcoHelper.plugin);
        }
    }

    public static DatabaseManager getInstance(){
        if (INSTANCE == null){
            synchronized (DatabaseManager.class){
                if (INSTANCE == null){
                    INSTANCE = new DatabaseManager();
                }
            }
        }
        return INSTANCE;
    }

    public ShopItem getItem(long itemID) {
        ShopItemDbModel shopItemDbModel = shopItemTable.selectUniqueUnchecked(WhereClause.EQ("uid", itemID));
        return ShopItemDbModel.toShopItem(shopItemDbModel);
    }

    public List<ShopItem> getPlayerItems(ShopItemType type, UUID owner){
        List<ShopItem> collect = shopItemTable.select(WhereClause.EQ("owner", owner.toString()).whereEq("type", type.name())).stream()
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public void addTransaction(Transaction transaction){

    }

    public int count(ShopItemType type, UUID owner){
        return shopItemTable.count(WhereClause.EQ("owner", owner.toString()).whereEq("type", type.name()));
    }

    public int getUidMax(String tableName) throws SQLException {
        ResultSet resultSet = db.getConnection().createStatement().executeQuery(String.format("select max(uid) from %s", tableName));
        if (resultSet.first()) {
            return resultSet.getInt(0);
        }return 0;
    }

    public void insertShopItem(ShopItemDbModel shopItemDbModel){
        shopItemTable.insert(shopItemDbModel);
    }
}

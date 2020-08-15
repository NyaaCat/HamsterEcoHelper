package cat.nyaa.heh.db;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.db.model.ShopItemDbModel;
import cat.nyaa.heh.db.model.SignShopDbModel;
import cat.nyaa.heh.enums.ShopItemType;
import cat.nyaa.heh.enums.SignShopType;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.signshop.SignShopBuy;
import cat.nyaa.heh.signshop.SignShopSell;
import cat.nyaa.heh.transaction.Tax;
import cat.nyaa.heh.transaction.Transaction;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager INSTANCE;

    private DatabaseConfig databaseConfig;
    IConnectedDatabase db;

    ITypedTable<ShopItemDbModel> shopItemTable;
    ITypedTable<Tax> taxTable;
    ITypedTable<Transaction> transactionTable;
    ITypedTable<SignShopDbModel> signShopTable;

    private DatabaseManager(){
        databaseConfig = new DatabaseConfig();
        databaseConfig.load();
        try{
            db = DatabaseUtils.connect(HamsterEcoHelper.plugin, databaseConfig.backendConfig);
            loadTables();

        }catch (Exception e){
            Bukkit.getPluginManager().disablePlugin(HamsterEcoHelper.plugin);
        }
    }

    private void loadTables() {
        shopItemTable = db.getTable(ShopItemDbModel.class);
        taxTable = db.getTable(Tax.class);
        transactionTable = db.getTable(Transaction.class);
        signShopTable = db.getTable(SignShopDbModel.class);
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
        List<ShopItem> collect = shopItemTable.select(WhereClause.EQ("type", type).whereEq("owner", owner.toString())).stream()
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public void updateShopItem(ShopItem item) {
        shopItemTable.update(ShopItemDbModel.fromShopItem(item), WhereClause.EQ("uid",item.getUid()));
    }

    public void insertTransaction(Transaction transaction) {
        transactionTable.insert(transaction);
    }

    public void insertTax(Tax taxRecord) {
        taxTable.insert(taxRecord);
    }

    public int count(ShopItemType type, UUID owner){
        return shopItemTable.count(WhereClause.EQ("type", type.name()).whereEq("owner", owner.toString()));
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

    public List<ShopItem> getMarketItems() {
        List<ShopItem> collect = shopItemTable.select(
                    WhereClause.EQ("type", ShopItemType.MARKET.name())
                            .where("amount", ">", "sold")
                            .whereEq("available", true)
                ).stream()
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public List<ShopItem> getMarketItems(UUID owner) {
        List<ShopItem> collect = shopItemTable.select(
                WhereClause.EQ("type", ShopItemType.MARKET.name())
                        .whereEq("owner", owner.toString())
                        .where("amount", ">", "sold")
                        .whereEq("available", true)
        ).stream()
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public boolean hasBuyShop(UUID owner) {
        return signShopTable.count(WhereClause.EQ("type", SignShopType.BUY).whereEq("owner", owner)) > 0;
    }

    public boolean hasSellShop(UUID owner) {
        return signShopTable.count(WhereClause.EQ("type", SignShopType.SELL).whereEq("owner", owner)) > 0;
    }

    public SignShopBuy getBuyShop(UUID owner) {
        return new SignShopBuy(signShopTable.selectUniqueUnchecked(WhereClause.EQ("type", SignShopType.BUY).whereEq("owner", owner)));
    }

    public SignShopSell getSellShop(UUID owner) {
        return new SignShopSell(signShopTable.selectUniqueUnchecked(WhereClause.EQ("type", SignShopType.SELL).whereEq("owner", owner)));
    }

    public List<ShopItem> getSellShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", SignShopType.SELL).whereEq("owner", owner)).stream()
                .filter(ShopItemDbModel::isAvailable)
                .filter(shopItemDbModel -> shopItemDbModel.getSold()>=shopItemDbModel.getAmount())
                .map(ShopItem::new)
                .collect(Collectors.toList());
    }

    public List<ShopItem> getBuyShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", SignShopType.BUY).whereEq("owner", owner)).stream()
                .filter(ShopItemDbModel::isAvailable)
                .filter(shopItemDbModel -> shopItemDbModel.getSold()>=shopItemDbModel.getAmount())
                .map(ShopItem::new)
                .collect(Collectors.toList());
    }

    public Map<UUID, List<SignShopBuy>> getBuyShops() {
        Map<UUID, List<SignShopBuy>> result = new HashMap<>();
        signShopTable.select(WhereClause.EQ("type", SignShopType.BUY)).stream()
                .forEach(signShopDbModel -> {
                    List<SignShopBuy> signShopSells = result.computeIfAbsent(signShopDbModel.getOwner(), uuid -> new ArrayList<>());
                    signShopSells.add(new SignShopBuy(signShopDbModel));
                });
        return result;
    }

    public Map<UUID, List<SignShopSell>> getSellShops() {
        Map<UUID, List<SignShopSell>> result = new HashMap<>();
        signShopTable.select(WhereClause.EQ("type", SignShopType.BUY)).stream()
                .forEach(signShopDbModel -> {
                    List<SignShopSell> signShopSells = result.computeIfAbsent(signShopDbModel.getOwner(), uuid -> new ArrayList<>());
                    signShopSells.add(new SignShopSell(signShopDbModel));
                });
        return result;
    }

    public void addSignShop(SignShopDbModel signShopSell) {
        signShopTable.insert(signShopSell);
    }
}

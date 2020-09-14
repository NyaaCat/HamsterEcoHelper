package heh8_0.db;

import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import heh8_0.db.model.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager INSTANCE;

    IConnectedDatabase db;

    ITypedTable<ShopItemDbModel> shopItemTable;
    ITypedTable<Tax> taxTable;
    ITypedTable<Transaction> transactionTable;
    ITypedTable<InvoiceDbModel> invoiceTable;
    ITypedTable<LocationDbModel> locationTable;
    ITypedTable<StorageDbModel> storageTable;
    ITypedTable<AccountDbModel> accountTable;

    private DatabaseManager(){
        try{
//            db = DatabaseUtils.connect(HamsterEcoHelper.plugin, databaseConfig.backendConfig);
            loadTables();
        }catch (Exception e){
        }
    }

    private void loadTables() {
        shopItemTable = db.getTable(ShopItemDbModel.class);
        taxTable = db.getTable(Tax.class);
        transactionTable = db.getTable(Transaction.class);
        invoiceTable = db.getTable(InvoiceDbModel.class);
        locationTable = db.getTable(LocationDbModel.class);
        storageTable = db.getTable(StorageDbModel.class);
        accountTable = db.getTable(AccountDbModel.class);
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

    public ShopItemDbModel getItem(long itemID) {
        ShopItemDbModel shopItemDbModel = shopItemTable.selectUniqueUnchecked(WhereClause.EQ("uid", itemID));
        return shopItemDbModel;
    }

    public List<ShopItemDbModel> getPlayerItems(ShopItemType type, UUID owner){
        List<ShopItemDbModel> collect = shopItemTable.select(WhereClause.EQ("type", type).whereEq("available", true).whereEq("owner", owner.toString()));
        return collect;
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

    public long getUidMax(String tableName) throws SQLException {
        ResultSet resultSet = db.getConnection().createStatement().executeQuery(String.format("select max(uid) from %s", tableName));
        return resultSet.getLong(1);
    }

    public void insertShopItem(ShopItemDbModel shopItemDbModel){
        shopItemTable.insert(shopItemDbModel);
    }

    public List<ShopItemDbModel> getMarketItems() {
        List<ShopItemDbModel> collect = shopItemTable.select(
                WhereClause.EQ("type", ShopItemType.MARKET)
                        .whereEq("available", true)
        ).stream()
                .filter(shopItemDbModel -> shopItemDbModel.getAmount() > shopItemDbModel.getSold())
                .collect(Collectors.toList());
        return collect;
    }

    public List<ShopItemDbModel> getMarketItems(UUID owner) {
        List<ShopItemDbModel> collect = shopItemTable.select(
                WhereClause.EQ("type", ShopItemType.MARKET)
                        .whereEq("available", true)
                        .whereEq("owner", owner.toString())
        ).stream()
                .filter(shopItemDbModel -> shopItemDbModel.getAmount() > shopItemDbModel.getSold())
                .collect(Collectors.toList());
        return collect;
    }

    public boolean hasBuyShop(UUID owner) {
        return locationTable.count(WhereClause.EQ("type", LocationType.SIGN_SHOP_BUY).whereEq("owner", owner)) > 0;
    }

    public boolean hasSellShop(UUID owner) {
        return locationTable.count(WhereClause.EQ("type", LocationType.SIGN_SHOP_SELL).whereEq("owner", owner)) > 0;
    }

    public List<ShopItemDbModel> getSellShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", ShopItemType.SIGN_SHOP_SELL)
                    .whereEq("available", true)
                    .whereEq("owner", owner)).stream()
                        .filter(ShopItemDbModel::isAvailable)
                        .filter(shopItemDbModel -> shopItemDbModel.getAmount()>shopItemDbModel.getSold())
                        .collect(Collectors.toList());
    }

    public List<ShopItemDbModel> getBuyShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", ShopItemType.SIGN_SHOP_BUY)
                    .whereEq("owner", owner)
                    .whereEq("available", true)).stream()
                        .filter(ShopItemDbModel::isAvailable)
                        .filter(shopItemDbModel -> shopItemDbModel.getAmount()>shopItemDbModel.getSold())
                        .collect(Collectors.toList());
    }

    public void insertLocation(LocationDbModel signShopSell) {
        locationTable.insert(signShopSell);
    }

    public void addShopItem(ShopItemDbModel shopItem) {
        shopItemTable.insert(shopItem);
    }

    public void close() {
        try {
            db.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<ShopItemDbModel> getAvailableInvoices() {
        WhereClause clause = WhereClause.EQ("type", ShopItemType.DIRECT)
                .where("sold", "<", "amount")
                .whereEq("available", true);
        return shopItemTable.select(clause);
    }

    public UUID getInvoiceCustomer(long uid) {
        InvoiceDbModel model = invoiceTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
        return model == null ? null : model.getCustomer();
    }

    public UUID getInvoicePayer(long uid) {
        InvoiceDbModel model = invoiceTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
        return model == null ? null : model.getPayer();
    }

    public void insertInvoice(InvoiceDbModel invoiceDbModel) {
        invoiceTable.insert(invoiceDbModel);
    }

    public Block getBlock(long uid) {
        LocationDbModel locationDbModel = locationTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
        return locationDbModel.getBlock();
    }

    public Entity getEntity(long uid){
        LocationDbModel locationDbModel = locationTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
        return locationDbModel.getEntity();
    }

    public LocationDbModel getLocationModel(UUID uniqueId, LocationType chestLotto) {
        return locationTable.selectUniqueUnchecked(WhereClause.EQ("owner", uniqueId).whereEq("type", chestLotto));
    }

    public void updateLocationModel(LocationDbModel locationModel) {
        locationTable.update(locationModel, WhereClause.EQ("uid", locationModel.getUid()));
    }

    public LocationDbModel getShopFrame(UUID uniqueId) {
        return locationTable.selectUniqueUnchecked(WhereClause.EQ("type", LocationType.FRAME).whereEq("entityuuid", uniqueId));
    }

    public List<StorageDbModel> getStorage(UUID owner) {
        return storageTable.select(WhereClause.EQ("owner", owner));
    }

    public void addStorageItem(StorageDbModel model) {
        storageTable.insert(model);
    }

    public void updateStorageItem(StorageDbModel storageDbModel) {
        storageTable.update(storageDbModel, WhereClause.EQ("uid", storageDbModel.getUid()));
    }

    public void removeStorageItem(StorageDbModel storageDbModel) {
        storageTable.delete(WhereClause.EQ("uid", storageDbModel.getUid()));
    }

    public Inventory getLottoItems(UUID owner) {
        Chest chest = locationTable.select(WhereClause.EQ("type", LocationType.CHEST_LOTTO).whereEq("owner", owner)).stream()
                .map(LocationDbModel::getBlock)
                .filter(block -> block.getState() instanceof Chest)
                .map(block -> ((Chest) block.getState()))
                .findFirst().orElse(null);
        return chest.getInventory();
    }

    public long getSystemUid(UUID uuid){
        return accountTable.selectUniqueUnchecked(WhereClause.EQ("uuid", uuid)).getUid();
    }

    public double getSystemBal(long uid){
        return accountTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid)).getBalance();
    }

    public void updateAccount(AccountDbModel model){
        accountTable.update(model, WhereClause.EQ("uid", model.getUid()));
    }

    public void addAccount(AccountDbModel dbModel) {
        accountTable.insert(dbModel);
    }

    public AccountDbModel getAccount(long uid) {
        return accountTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
    }

    public int getShopItemCount() {
        String sql = "select count() count, amount a, sold s, available ava from items where a > s and ava = true";
        try {
            Statement statement = db.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.getInt("count");
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop item count", throwables);
            throw new RuntimeException();
        }
    }

    public void removeLocationModel(LocationDbModel dbModel) {
        locationTable.delete(WhereClause.EQ("uid",dbModel.getUid()));
    }

    public LocationDbModel getLocationModelAt(Location location) {
        return locationTable.selectUniqueUnchecked(WhereClause.EQ("world", location.getWorld().getName())
                .whereEq("x", location.getX())
                .whereEq("y", location.getY())
                .whereEq("z", location.getZ())
        );
    }
}

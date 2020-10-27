package cat.nyaa.heh.db;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.business.signshop.*;
import cat.nyaa.heh.db.model.*;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.business.transaction.Transaction;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import co.aikar.taskchain.TaskChain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager INSTANCE;

    private DatabaseConfig databaseConfig;
    IConnectedDatabase db;

    ITypedTable<ShopItemDbModel> shopItemTable;
    ITypedTable<Tax> taxTable;
    ITypedTable<Transaction> transactionTable;
    ITypedTable<InvoiceDbModel> invoiceTable;
    ITypedTable<LocationDbModel> locationTable;
    ITypedTable<StorageDbModel> storageTable;
    ITypedTable<AccountDbModel> accountTable;

    private DatabaseManager(){
        databaseConfig = new DatabaseConfig();
        databaseConfig.load();
        try{
            db = DatabaseUtils.connect(HamsterEcoHelper.plugin, databaseConfig.backendConfig);
            loadTables();
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "error loading database", e);
            try {
                db.close();
            } catch (SQLException throwables) {
                Bukkit.getLogger().log(Level.SEVERE, "error closing database", e);
            }
            Bukkit.getPluginManager().disablePlugin(HamsterEcoHelper.plugin);
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

    public ShopItem getItem(long itemID) {
        ShopItemDbModel shopItemDbModel = shopItemTable.selectUniqueUnchecked(WhereClause.EQ("uid", itemID));
        return ShopItemDbModel.toShopItem(shopItemDbModel);
    }

    public List<ShopItem> getPlayerItems(ShopItemType type, UUID owner){
        List<ShopItem> collect = shopItemTable.select(WhereClause.EQ("type", type).whereEq("available", true).whereEq("owner", owner.toString())).stream()
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

    public long getUidMax(String tableName) throws SQLException {
        ResultSet resultSet = db.getConnection().createStatement().executeQuery(String.format("select max(uid) from %s", tableName));
        return resultSet.getLong(1);
    }

    public void insertShopItem(ShopItemDbModel shopItemDbModel){
        shopItemTable.insert(shopItemDbModel);
    }

    public List<ShopItem> getMarketItems() {
        List<ShopItem> collect = shopItemTable.select(
                    WhereClause.EQ("type", ShopItemType.MARKET)
                            .whereEq("available", true)
                ).stream()
                .filter(shopItemDbModel -> shopItemDbModel.getAmount() > shopItemDbModel.getSold())
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public List<ShopItem> getMarketItems(UUID owner) {
        List<ShopItem> collect = shopItemTable.select(
                WhereClause.EQ("type", ShopItemType.MARKET)
                        .whereEq("available", true)
                        .whereEq("owner", owner.toString())
        ).stream()
                .filter(shopItemDbModel -> shopItemDbModel.getAmount() > shopItemDbModel.getSold())
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public boolean hasBuyShop(UUID owner) {
        return locationTable.count(WhereClause.EQ("type", LocationType.SIGN_SHOP_BUY).whereEq("owner", owner)) > 0;
    }

    public boolean hasSellShop(UUID owner) {
        return locationTable.count(WhereClause.EQ("type", LocationType.SIGN_SHOP_SELL).whereEq("owner", owner)) > 0;
    }

    public List<ShopItem> getSellShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", ShopItemType.SIGN_SHOP_SELL)
                    .whereEq("available", true)
                    .whereEq("owner", owner)).stream()
                        .filter(ShopItemDbModel::isAvailable)
                        .filter(shopItemDbModel -> shopItemDbModel.getAmount()>shopItemDbModel.getSold())
                        .map(ShopItem::new)
                        .collect(Collectors.toList());
    }

    public List<ShopItem> getBuyShopItems(UUID owner) {
        return shopItemTable.select(WhereClause.EQ("type", ShopItemType.SIGN_SHOP_BUY)
                    .whereEq("owner", owner)
                    .whereEq("available", true)).stream()
                        .filter(ShopItemDbModel::isAvailable)
                        .filter(shopItemDbModel -> shopItemDbModel.getAmount()>shopItemDbModel.getSold())
                        .map(ShopItem::new)
                        .collect(Collectors.toList());
    }



    public List<BaseSignShop> getShops() {
        List<BaseSignShop> result = new ArrayList<>();
        List<LocationDbModel> toRemove = new ArrayList<>();
        locationTable.select(WhereClause.EQ("type", LocationType.SIGN_SHOP_BUY)).stream()
                .forEach(signShopDbModel -> {
                    try{
                        result.add(new SignShopBuy(signShopDbModel));
                    }catch (Exception e){
                        toRemove.add(signShopDbModel);
                    }
                });
        locationTable.select(WhereClause.EQ("type", LocationType.SIGN_SHOP_SELL)).stream()
                .forEach(signShopDbModel -> {
                    try{
                        result.add(new SignShopSell(signShopDbModel));
                    }catch (Exception e){
                        toRemove.add(signShopDbModel);
                    }
                });
        locationTable.select(WhereClause.EQ("type", LocationType.SIGN_SHOP_LOTTO)).stream()
                .forEach(signShopDbModel -> {
                    try{
                        result.add(new SignShopLotto(signShopDbModel));
                    }catch (Exception e){
                        toRemove.add(signShopDbModel);
                    }
                });
        if (toRemove.size()>0){
            new BukkitRunnable(){
                @Override
                public void run() {
                    Bukkit.getLogger().log(Level.WARNING, "deleting "+toRemove.size()+" locations due to invalid configuration");
                    toRemove.forEach(locationDbModel -> locationTable.delete(WhereClause.EQ("uid", locationDbModel.getUid())));
                }
            }.runTaskAsynchronously(HamsterEcoHelper.plugin);
        }
        return result;
    }
    public Map<UUID, List<SignShopBuy>> getBuyShops() {
        Map<UUID, List<SignShopBuy>> result = new HashMap<>();
        locationTable.select(WhereClause.EQ("type", LocationType.SIGN_SHOP_BUY)).stream()
                .forEach(signShopDbModel -> {
                    List<SignShopBuy> signShopSells = result.computeIfAbsent(signShopDbModel.getOwner(), uuid -> new ArrayList<>());
                    try {
                        signShopSells.add(new SignShopBuy(signShopDbModel));
                    }catch (Exception e){
                        Bukkit.getLogger().log(Level.WARNING, "", e);
                    }
                });
        return result;
    }

    public Map<UUID, List<SignShopSell>> getSellShops() {
        Map<UUID, List<SignShopSell>> result = new HashMap<>();
        locationTable.select(WhereClause.EQ("type", LocationType.SIGN_SHOP_BUY)).stream()
                .forEach(signShopDbModel -> {
                    List<SignShopSell> signShopSells = result.computeIfAbsent(signShopDbModel.getOwner(), uuid -> new ArrayList<>());
                    try {
                        signShopSells.add(new SignShopSell(signShopDbModel));
                    }catch (Exception e){
                        Bukkit.getLogger().log(Level.WARNING, "", e);
                    }
                });
        return result;
    }

    public void insertLocation(LocationDbModel signShopSell) {
        locationTable.insert(signShopSell);
    }

    public void addShopItem(ShopItem shopItem) {
        shopItemTable.insert(ShopItemDbModel.fromShopItem(shopItem));
    }

    public void close() {
        try {
            db.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<ShopItem> getAvailableInvoices() {
        WhereClause clause = WhereClause.EQ("type", ShopItemType.DIRECT)
                .where("sold", "<", "amount")
                .whereEq("available", true);
        return shopItemTable.select(clause).stream().map(shopItemDbModel -> shopItemDbModel.toShopItem()).collect(Collectors.toList());
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

    public void removeShop(BaseSignShop shopAt) {
        locationTable.delete(WhereClause.EQ("uid", shopAt.getUid()));
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

    public List<ShopItem> getLottoItems(UUID owner) throws NoLottoChestException {
        List<ShopItem> collect = shopItemTable.select(
                WhereClause.EQ("type", ShopItemType.LOTTO)
                        .whereEq("owner", owner)
                        .whereEq("available", true)
        ).stream()
                .filter(shopItemDbModel -> shopItemDbModel.getAmount() > shopItemDbModel.getSold())
                .map(shopItemDbModel -> ShopItemDbModel.toShopItem(shopItemDbModel))
                .collect(Collectors.toList());
        return collect;
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

    public int getShopItemCount(UUID owner, ShopItemType shopItemType) {
        String sql = "select count(*) count, amount a, sold s, available ava from items where a > s and ava = true and owner = ? and type = ?";
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(sql);
            statement.setString(1, owner.toString());
            statement.setString(2, shopItemType.name());
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.getInt("count");
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop item count", throwables);
            throw new RuntimeException();
        }
    }

    public int getShopItemCount() {
        String sql = "select count(*) count, amount a, sold s, available ava from items where a > s and ava = true";
        try {
            Statement statement = db.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.getInt("count");
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop item count", throwables);
            throw new RuntimeException();
        }
    }

    public List<ShopItem> getShopItems(int current, int batchSize) {
        String sql = "select amount, available, nbt, owner, price, sold, time, type, uid from items where available = true and amount > sold ORDER BY uid limit ? offset ?;";
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(sql);
            statement.setInt(1, current);
            statement.setInt(2, batchSize);
            List<ShopItem> results = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ShopItemDbModel obj = shopItemTable.getJavaTypeModifier().getObjectFromResultSet(rs);
                    results.add(new ShopItem(obj));
                }
            }
            return results;
        } catch (SQLException | ReflectiveOperationException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop item count", throwables);
            throw new RuntimeException();
        }
    }

    public List<ShopItem> getShopItems(String keywords) {
        String sql = "select amount, available, nbt, owner, price, sold, time, type, uid, meta from items where amount > sold and available = true and meta like ? ORDER BY uid;";
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(sql);
            statement.setString(1, String.format("%%%s%%",keywords));
            List<ShopItem> results = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ShopItemDbModel obj = shopItemTable.getJavaTypeModifier().getObjectFromResultSet(rs);
                    results.add(new ShopItem(obj));
                }
            }
            return results;
        } catch (SQLException | ReflectiveOperationException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop items", throwables);
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

    public UUID getInvoiceFrom(long uid) {
        InvoiceDbModel uid1 = invoiceTable.selectUniqueUnchecked(WhereClause.EQ("uid", uid));
        if (uid1 == null){
            return null;
        }
        return uid1.getFrom();
    }

    public List<LocationDbModel> getFrameShops() {
        List<LocationDbModel> models = locationTable.select(WhereClause.EQ("type", LocationType.FRAME));
        return models;
    }

    public void removeLocationModelById(long uid) {
        locationTable.delete(WhereClause.EQ("uid", uid));
    }

    public int getMarketItemCount(UUID uniqueId) {
        String sql = "select count(*) count, amount a, sold s, available ava from items where type = ? and a > s and ava = true and owner = ?";
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(sql);
            statement.setString(1,ShopItemType.MARKET.name());
            statement.setString(2,uniqueId.toString());
            List<ShopItem> results = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                return rs.getInt("count");
            }
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, "error loading shop items", throwables);
            throw new RuntimeException();
        }
    }

    public void invalidateItem(ShopItem item) {
        item.setAvailable(false);
        shopItemTable.update(ShopItemDbModel.fromShopItem(item), WhereClause.EQ("uid", item.getUid()));
    }
}

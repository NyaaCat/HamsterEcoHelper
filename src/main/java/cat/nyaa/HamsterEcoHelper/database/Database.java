package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database implements Cloneable {
    public final IConnectedDatabase database;
    private final HamsterEcoHelper plugin;

    public Database(HamsterEcoHelper plugin) throws SQLException, ClassNotFoundException {
        database = DatabaseUtils.connect(plugin, plugin.config.backendConfig);
        this.plugin = plugin;
        int newDatabaseVersion = DatabaseUpdater.updateDatabase(Database.this, plugin.config.database_version);
        if (newDatabaseVersion != plugin.config.database_version) {
            plugin.config.database_version = newDatabaseVersion;
            plugin.config.save();
        }
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        ITypedTable<TempStorageRepo> table = database.getUnverifiedTable(TempStorageRepo.class);
        WhereClause where = WhereClause.EQ("player_id", player.getUniqueId().toString());
        TempStorageRepo repo = table.selectUniqueUnchecked(where);
        if (repo == null) {
            return Collections.emptyList();
        }
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(repo.yaml);
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
        List<ItemStack> ret = new ArrayList<>();
        for (String key : cfg.getKeys(false)) {
            ret.add(cfg.getItemStack(key));
        }
        return ret;
    }

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        ITypedTable<TempStorageRepo> table = database.getUnverifiedTable(TempStorageRepo.class);
        WhereClause where = WhereClause.EQ("player_id", player.getUniqueId().toString());
        TempStorageRepo repo = table.selectUniqueUnchecked(where);
        YamlConfiguration cfg = new YamlConfiguration();
        if (repo == null) {
            cfg.set("0", item);
        } else {
            YamlConfiguration tmp = new YamlConfiguration();
            try {
                tmp.loadFromString(repo.yaml);
            } catch (InvalidConfigurationException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            List<ItemStack> items = new ArrayList<>();
            for (String key : tmp.getKeys(false)) {
                items.add(tmp.getItemStack(key));
            }
            items.add(item);

            for (int i = 0; i < items.size(); i++) {
                cfg.set(Integer.toString(i), items.get(i));
            }
        }
        if (repo == null) {
            repo = new TempStorageRepo();
            repo.playerId = player.getUniqueId();
        } else {
            table.delete(where);
        }
        repo.yaml = cfg.saveToString();
        table.insert(repo);
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        ITypedTable<TempStorageRepo> table = database.getUnverifiedTable(TempStorageRepo.class);
        WhereClause playerRecord = WhereClause.EQ("player_id", player.getUniqueId().toString());
        TempStorageRepo result = table.selectUniqueUnchecked(playerRecord);
        if (result != null) {
            table.delete(playerRecord);
        }
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem> list = new ArrayList<>();
        ITypedTable<MarketItem> table = database.getUnverifiedTable(MarketItem.class);
        WhereClause where = new WhereClause("amount", ">", 0);
        List<MarketItem> tmp = table.select(seller == null ? where : where.whereEq("player_id", seller.toString()));
        Collections.reverse(tmp);
        for (int i = 0; i < tmp.size(); i++) {
            if (i + 1 > offset) {
                list.add(tmp.get(i));
                if (list.size() >= limit) {
                    break;
                }
            }
        }
        return list;
    }

    public long marketOffer(Player player, ItemStack itemStack, double unit_price) {
        MarketItem item = new MarketItem();
        item.item = itemStack.clone();
        item.amount = itemStack.getAmount();
        item.playerId = player.getUniqueId();
        item.unitPrice = unit_price;
        long id = 1;
        ITypedTable<MarketItem> table = database.getUnverifiedTable(MarketItem.class);
        for (MarketItem marketItem : table.select(WhereClause.EMPTY)) {
            if (marketItem.id >= id) {
                id = marketItem.id + 1;
            }
        }
        item.id = id;
        table.insert(item);
        return item.id;
    }

    public void marketBuy(Player player, long itemId, int amount) {
        ITypedTable<MarketItem> table = database.getUnverifiedTable(MarketItem.class);
        WhereClause where = WhereClause.EQ("id", itemId);
        MarketItem marketItem = table.selectUniqueUnchecked(where);
        if (marketItem != null) {
            marketItem.amount = marketItem.amount - amount;
            table.update(marketItem, where);
        }
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        ITypedTable<MarketItem> table = database.getUnverifiedTable(MarketItem.class);
        WhereClause where = WhereClause.EQ("player_id", player.getUniqueId().toString()).where("amount", ">", 0);
        return table.select(where).size();
    }

    public int getMarketItemCount() {
        return database.getUnverifiedTable(MarketItem.class).select(new WhereClause("amount", ">", 0)).size();
    }

    public MarketItem getMarketItem(long id) {
        return database.getUnverifiedTable(MarketItem.class).selectUniqueUnchecked(WhereClause.EQ("id", id));
    }


    public ItemLog getItemLog(long id) {
        return database.getUnverifiedTable(ItemLog.class).selectUniqueUnchecked(WhereClause.EQ("id", id));
    }

    public long addItemLog(OfflinePlayer player, ItemStack item, double price, int amount) {
        ItemLog i = new ItemLog();
        i.owner = player.getUniqueId();
        i.item = item.clone();
        i.price = price;
        i.amount = amount;
        long id = 1;
        ITypedTable<ItemLog> table = database.getUnverifiedTable(ItemLog.class);
        for (ItemLog log : table.select(WhereClause.EMPTY)) {
            if (log.id >= id) {
                id = log.id + 1;
            }
        }
        i.id = id;
        table.insert(i);
        return i.id;
    }

    public List<Sign> getShopSigns() {
        return database.getUnverifiedTable(Sign.class).select(WhereClause.EMPTY);
    }

    public Sign createShopSign(OfflinePlayer player, Block block, ShopMode mode, double lottoPrice) {
        Sign shopLocation = new Sign();
        shopLocation.owner = player.getUniqueId();
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        if (mode == ShopMode.LOTTO) {
            shopLocation.lotto_price = lottoPrice;
        }
        database.getUnverifiedTable(Sign.class).delete(WhereClause.EQ("id", shopLocation.id));
        database.getUnverifiedTable(Sign.class).insert(shopLocation);
        return shopLocation;
    }

    public boolean removeShopSign(Block block) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(block.getLocation());
        return removeShopSign(shopLocation);
    }

    public boolean removeShopSign(String world, int x, int y, int z) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(world, x, y, z);
        return removeShopSign(shopLocation);
    }

    private boolean removeShopSign(Sign shopLocation) {
        ITypedTable<Sign> table = database.getUnverifiedTable(Sign.class);
        WhereClause where = WhereClause.EQ("id", shopLocation.id);
        if (table.selectUniqueUnchecked(where) != null) {
            table.delete(where);
            return true;
        }
        return false;
    }

    public List<SignShop> getSignShops() {
        return database.getUnverifiedTable(SignShop.class).select(WhereClause.EMPTY);
    }

    public SignShop getSignShop(UUID owner) {
        SignShop signShop = database.getUnverifiedTable(SignShop.class).selectUniqueUnchecked(WhereClause.EQ("id", owner.toString()));
        if (signShop != null) {
            return signShop;
        }
        signShop = new SignShop();
        signShop.owner = owner;
        return signShop;
    }

    public void setSignShop(UUID owner, SignShop shop) {
        insertOrUpdate(owner.toString(), "id", SignShop.class, shop);
    }

    public ShopStorageLocation getChestLocation(UUID owner) {
        return database.getUnverifiedTable(ShopStorageLocation.class).selectUniqueUnchecked(WhereClause.EQ("owner", owner.toString()));
    }

    public void setChestLocation(UUID owner, ShopStorageLocation location) {
        insertOrUpdate(owner.toString(), "owner", ShopStorageLocation.class, location);
    }

    public LottoStorageLocation getLottoStorageLocation(UUID owner) {
        return database.getUnverifiedTable(LottoStorageLocation.class).selectUniqueUnchecked(WhereClause.EQ("owner", owner.toString()));
    }

    public void setLottoStorageLocation(UUID owner, LottoStorageLocation location) {
        insertOrUpdate(owner.toString(), "owner", LottoStorageLocation.class, location);
    }

    public Invoice draftInvoice(UUID buyer, UUID seller, ItemStack itemStack, double totalPrice, double tax) {
        long id;
        ITypedTable<Invoice> table = database.getUnverifiedTable(Invoice.class);
        id = table.select(WhereClause.EMPTY).stream().parallel().mapToLong(Invoice::getId).max().orElse(0) + 1;
        Invoice invoice = new Invoice(id, buyer, seller, itemStack, totalPrice, tax);
        table.insert(invoice);
        return invoice;
    }

    public Invoice cancelInvoice(long id) {
        ITypedTable<Invoice> table = database.getUnverifiedTable(Invoice.class);
        WhereClause where = WhereClause.EQ("id", id);
        Invoice invoice = table.selectUniqueUnchecked(where);
        invoice.setCanceled();
        table.update(invoice, where, "state", "updated_time");
        return invoice;
    }

    public Invoice payInvoice(long id, OfflinePlayer drawee) {
        ITypedTable<Invoice> table = database.getUnverifiedTable(Invoice.class);
        WhereClause where = WhereClause.EQ("id", id);
        Invoice invoice = table.selectUniqueUnchecked(where);
        invoice.setDraweeId(drawee.getUniqueId());
        invoice.setCompleted();
        table.update(invoice, where, "state", "drawee_id", "updated_time");
        return invoice;
    }

    public Invoice queryInvoice(long id) {
        return database.getUnverifiedTable(Invoice.class).selectUniqueUnchecked(WhereClause.EQ("id", id));
    }

    public List<Invoice> queryBuyerInvoice(UUID buyerId) {
        return database.getUnverifiedTable(Invoice.class).select(WhereClause.EQ("buyer_id", buyerId));
    }

    public List<Invoice> querySellerInvoice(UUID sellerId) {
        return database.getUnverifiedTable(Invoice.class).select(WhereClause.EQ("seller_id", sellerId));
    }

    public List<Invoice> queryDraweeInvoice(UUID draweeId) {
        return database.getUnverifiedTable(Invoice.class).select(WhereClause.EQ("drawee_id", draweeId));
    }

    public List<KitSign> getAllKitSign() {
        return database.getUnverifiedTable(KitSign.class).select(WhereClause.EMPTY);
    }

    public Kit getKit(String kitName) {
        return database.getUnverifiedTable(Kit.class).selectUniqueUnchecked(WhereClause.EQ("id", kitName));
    }

    public boolean createKit(Kit kit) {
        insertOrUpdate(kit.id, "id", Kit.class, kit);
        return true;
    }

    public void createKitSign(KitSign kitSign) {
        insertOrUpdate(kitSign.id, "id", KitSign.class, kitSign);
    }

    public boolean removeKitSign(KitSign kitSign) {
        WhereClause where = WhereClause.EQ("id", kitSign.id);
        ITypedTable<KitSign> table = database.getUnverifiedTable(KitSign.class);
        if (table.selectUniqueUnchecked(where) != null) {
            table.delete(where);
            return true;
        }
        return false;
    }

    public boolean removeKit(String kitName) {
        WhereClause where = WhereClause.EQ("id", kitName);
        ITypedTable<Kit> table = database.getUnverifiedTable(Kit.class);
        if (table.selectUniqueUnchecked(where) != null) {
            table.delete(where);
            return true;
        }
        return false;
    }

    public void addKitRecord(String kitName, OfflinePlayer player) {
        KitRecord r = new KitRecord();
        r.kitName = kitName;
        r.player = player.getUniqueId();
        long id = 1;
        ITypedTable<KitRecord> table = database.getUnverifiedTable(KitRecord.class);
        for (KitRecord record : table.select(WhereClause.EMPTY)) {
            if (record.id >= id) {
                id = record.id + 1;
            }
        }
        r.id = id;
        table.insert(r);
    }

    public KitRecord getKitRecord(String kitName, OfflinePlayer player) {
        return database.getUnverifiedTable(KitRecord.class).selectUniqueUnchecked(WhereClause.EQ("kit_name", kitName).whereEq("player", player.getUniqueId().toString()));
    }

    public boolean removeKitRecord(String kitName) {
        ITypedTable<KitRecord> table = database.getUnverifiedTable(KitRecord.class);
        WhereClause where = WhereClause.EQ("kit_name", kitName);
        if (table.selectUniqueUnchecked(where) != null) {
            table.delete(where);
            return true;
        }
        return false;
    }

    public boolean removeKitRecord(String kitName, OfflinePlayer player) {
        ITypedTable<KitRecord> table = database.getUnverifiedTable(KitRecord.class);
        WhereClause where = WhereClause.EQ("kit_name", kitName).whereEq("player", player.getUniqueId().toString());
        if (table.selectUniqueUnchecked(where) != null) {
            table.delete(where);
            return true;
        }
        return false;
    }

    private <T> void insertOrUpdate(Object uid, String columnName, Class<T> tableClass, T newRecord) {
        WhereClause where = WhereClause.EQ(columnName, uid);
        ITypedTable<T> table = database.getUnverifiedTable(tableClass);
        T oldRecord = table.selectUniqueUnchecked(where);
        if (oldRecord == null) {
            table.insert(newRecord);
        } else {
            table.update(newRecord, where);
        }
    }
}

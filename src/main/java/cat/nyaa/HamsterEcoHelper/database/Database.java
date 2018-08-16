package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import cat.nyaa.nyaacore.database.relational.Query;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database implements Cloneable {
    public final RelationalDB database;
    private final HamsterEcoHelper plugin;

    public Database(HamsterEcoHelper plugin) {
        database = DatabaseUtils.get(RelationalDB.class);
        this.plugin = plugin;
        int newDatabaseVersion = DatabaseUpdater.updateDatabase(Database.this, plugin.config.database_version);
        if (newDatabaseVersion != plugin.config.database_version) {
            plugin.config.database_version = newDatabaseVersion;
            plugin.config.save();
        }
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        try (Query<TempStorageRepo> result = database.queryTransactional(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
            if (result.count() == 0) return Collections.emptyList();
            YamlConfiguration cfg = new YamlConfiguration();
            try {
                cfg.loadFromString(result.selectUnique().yaml);
            } catch (InvalidConfigurationException ex) {
                ex.printStackTrace();
                return Collections.emptyList();
            }
            List<ItemStack> ret = new ArrayList<>();
            for (String key : cfg.getKeys(false)) {
                ret.add(cfg.getItemStack(key));
            }
            result.commit();
            return ret;
        }
    }

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        try (Query<TempStorageRepo> result = database.queryTransactional(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
            YamlConfiguration cfg = new YamlConfiguration();
            boolean update;
            if (result.count() == 0) {
                update = false;
                cfg.set("0", item);
            } else {
                update = true;
                YamlConfiguration tmp = new YamlConfiguration();
                try {
                    tmp.loadFromString(result.selectUnique().yaml);
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

            TempStorageRepo bean = new TempStorageRepo();
            bean.playerId = player.getUniqueId();
            bean.yaml = cfg.saveToString();
            if (update) {
                result.update(bean);
            } else {
                result.insert(bean);
            }
            result.commit();
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        try (Query<TempStorageRepo> query = database.queryTransactional(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
            if (query.count() != 0) {
                query.delete();
                query.commit();
            }
        }
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem> list = new ArrayList<>();
        try (Query<MarketItem> result =
                     seller == null ?
                             database.queryTransactional(MarketItem.class).where("amount", ">", 0) :
                             database.queryTransactional(MarketItem.class).where("amount", ">", 0).whereEq("player_id", seller.toString())) {
            if (result.count() > 0) {
                List<MarketItem> tmp = result.select();
                Collections.reverse(tmp);
                for (int i = 0; i < tmp.size(); i++) {
                    if (i + 1 > offset) {
                        list.add(tmp.get(i));
                        if (list.size() >= limit) {
                            break;
                        }
                    }
                }
            }
            result.commit();
            return list;
        }
    }

    public long marketOffer(Player player, ItemStack itemStack, double unit_price) {
        MarketItem item = new MarketItem();
        item.setItemStack(itemStack);
        item.amount = itemStack.getAmount();
        item.playerId = player.getUniqueId();
        item.unitPrice = unit_price;
        long id = 1;
        try (Query<MarketItem> query = database.queryTransactional(MarketItem.class)) {
            for (MarketItem marketItem : query.select()) {
                if (marketItem.id >= id) {
                    id = marketItem.id + 1;
                }
            }
            item.id = id;
            query.insert(item);
            query.commit();
        }
        return item.id;
    }

    public void marketBuy(Player player, long itemId, int amount) {
        try (Query<MarketItem> query = database.queryTransactional(MarketItem.class).whereEq("id", itemId)) {
            if (query.count() != 0) {
                MarketItem mItem = query.selectUnique();
                mItem.amount = mItem.amount - amount;
                mItem.id = itemId;
                query.update(mItem);
            }
            query.commit();
        }
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        int count = database.query(MarketItem.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0).count();
        return count > 0 ? count : 0;
    }

    public int getMarketItemCount() {
        int count = database.query(MarketItem.class).where("amount", ">", 0).count();
        return count > 0 ? count : 0;
    }

    public MarketItem getMarketItem(long id) {
        return database.query(MarketItem.class).whereEq("id", id).selectUniqueUnchecked();
    }


    public ItemLog getItemLog(long id) {
        return database.query(ItemLog.class).whereEq("id", id).selectUniqueUnchecked();
    }

    public long addItemLog(OfflinePlayer player, ItemStack item, double price, int amount) {
        ItemLog i = new ItemLog();
        i.owner = player.getUniqueId();
        i.setItemStack(item);
        i.price = price;
        i.amount = amount;
        long id = 1;
        try (Query<ItemLog> query = database.queryTransactional(ItemLog.class)) {
            for (ItemLog log : query.select()) {
                if (log.id >= id) {
                    id = log.id + 1;
                }
            }
            i.id = id;
            query.insert(i);
            query.commit();
            return i.id;
        }
    }

    public List<Sign> getShopSigns() {
        return database.query(Sign.class).select();
    }

    public Sign createShopSign(OfflinePlayer player, Block block, ShopMode mode) {
        Sign shopLocation = new Sign();
        shopLocation.owner = player.getUniqueId();
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        try (Query<Sign> sign = database.queryTransactional(Sign.class).whereEq("id", shopLocation.id)) {
            sign.delete();
            sign.insert(shopLocation);
            sign.commit();
        }
        return shopLocation;
    }

    public Sign createLottoSign(OfflinePlayer player, Block block, ShopMode mode, double price) {
        Sign shopLocation = new Sign();
        shopLocation.owner = player.getUniqueId();
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        shopLocation.lotto_price = price;
        try (Query<Sign> sign = database.queryTransactional(Sign.class).whereEq("id", shopLocation.id)) {
            if (sign != null) {
                sign.delete();
                sign.insert(shopLocation);
            }
            sign.commit();
        }
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
        try (Query<Sign> sign = database.queryTransactional(Sign.class).whereEq("id", shopLocation.id)) {
            if (sign != null) {
                sign.delete();
                sign.commit();
                return true;
            }
        }
        return false;
    }

    public List<SignShop> getSignShops() {
        return database.query(SignShop.class).select();
    }

    public SignShop getSignShop(UUID owner) {
        try (Query<SignShop> shop = database.queryTransactional(SignShop.class).whereEq("id", owner.toString())) {
            if (shop != null && shop.count() == 1) {
                return shop.selectUnique();
            }
        }
        SignShop s = new SignShop();
        s.owner = owner;
        return s;
    }

    public void setSignShop(UUID owner, SignShop shop) {
        try (Query<SignShop> s = database.queryTransactional(SignShop.class).whereEq("id", owner.toString())) {
            s.delete();
            s.insert(shop);
            s.commit();
        }
    }

    public ShopStorageLocation getChestLocation(UUID owner) {
        return database.query(ShopStorageLocation.class).whereEq("owner", owner.toString()).selectUniqueUnchecked();
    }

    public void setChestLocation(UUID owner, ShopStorageLocation location) {
        try (Query<ShopStorageLocation> s = database.queryTransactional(ShopStorageLocation.class).whereEq("owner", owner.toString())) {
            s.delete();
            s.insert(location);
            s.commit();
        }
    }

    public LottoStorageLocation getLottoStorageLocation(UUID owner) {
        return database.query(LottoStorageLocation.class).whereEq("owner", owner.toString()).selectUniqueUnchecked();
    }

    public void setLottoStorageLocation(UUID owner, LottoStorageLocation location) {
        try (Query<LottoStorageLocation> s = database.queryTransactional(LottoStorageLocation.class).whereEq("owner", owner.toString())) {
            s.delete();
            s.insert(location);
            s.commit();
        }
    }
}

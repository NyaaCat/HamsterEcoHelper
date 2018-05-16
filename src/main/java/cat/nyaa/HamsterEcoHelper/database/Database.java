package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.Query;
import cat.nyaa.nyaacore.database.RelationalDB;
import cat.nyaa.nyaacore.database.TransactionalQuery;
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
    private final HamsterEcoHelper plugin;
    public final RelationalDB database;

    public Database(HamsterEcoHelper plugin) {
        database = DatabaseUtils.get();
        this.plugin = plugin;
        database.connect();
        int newDatabaseVersion = DatabaseUpdater.updateDatabase(Database.this, plugin.config.database_version);
        if (newDatabaseVersion != plugin.config.database_version) {
            plugin.config.database_version = newDatabaseVersion;
            plugin.config.save();
        }
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        try (TransactionalQuery<TempStorageRepo> result = database.transaction(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
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
            return ret;
        }
    }

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        try (TransactionalQuery<TempStorageRepo> result = database.transaction(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
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
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        try (TransactionalQuery<TempStorageRepo> query = database.transaction(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString())) {
            if (query.count() != 0) {
                query.delete();
            }
        }
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem> list = new ArrayList<>();
        try (TransactionalQuery<MarketItem> result =
                     seller == null ?
                             database.transaction(MarketItem.class).where("amount", ">", 0) :
                             database.transaction(MarketItem.class).where("amount", ">", 0).whereEq("player_id", seller.toString())) {
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
        try (TransactionalQuery<MarketItem> query = database.transaction(MarketItem.class)) {
            for (MarketItem marketItem : query.select()) {
                if (marketItem.id >= id) {
                    id = marketItem.id + 1;
                }
            }
            item.id = id;
            query.insert(item);
        }
        return item.id;
    }

    public void marketBuy(Player player, long itemId, int amount) {
        try (TransactionalQuery<MarketItem> query = database.transaction(MarketItem.class).whereEq("id", itemId)) {
            if (query.count() != 0) {
                MarketItem mItem = query.selectUnique();
                mItem.amount = mItem.amount - amount;
                mItem.id = itemId;
                query.update(mItem);
            }
        }
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        try (TransactionalQuery<MarketItem> query = database.transaction(MarketItem.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0)) {
            if (query.count() > 0) {
                return query.count();
            }
        }
        return 0;
    }

    public int getMarketItemCount() {
        try (TransactionalQuery<MarketItem> query = database.transaction(MarketItem.class).where("amount", ">", 0)) {
            if (query.count() != 0) {
                return query.count();
            }
        }
        return 0;
    }

    public MarketItem getMarketItem(long id) {
        try (TransactionalQuery<MarketItem> query = database.transaction(MarketItem.class).whereEq("id", id)) {
            if (query.count() != 0) {
                return query.selectUnique();
            }
        }
        return null;
    }


    public ItemLog getItemLog(long id) {
        try (TransactionalQuery<ItemLog> log = database.transaction(ItemLog.class).whereEq("id", id)) {
            if (log != null && log.count() != 0) {
                return log.selectUnique();
            }
        }
        return null;
    }

    public long addItemLog(OfflinePlayer player, ItemStack item, double price, int amount) {
        ItemLog i = new ItemLog();
        i.owner = player.getUniqueId();
        i.setItemStack(item);
        i.price = price;
        i.amount = amount;
        long id = 1;
        try (TransactionalQuery<ItemLog> query = database.transaction(ItemLog.class)) {
            for (ItemLog log : query.select()) {
                if (log.id >= id) {
                    id = log.id + 1;
                }
            }
            i.id = id;
            query.insert(i);
            return i.id;
        }
    }

    public List<Sign> getShopSigns() {
        return database.auto(Sign.class).select();
    }

    public Sign createShopSign(OfflinePlayer player, Block block, ShopMode mode) {
        Sign shopLocation = new Sign();
        shopLocation.owner = player.getUniqueId();
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        try (TransactionalQuery<Sign> sign = database.transaction(Sign.class).whereEq("id", shopLocation.id)) {
            sign.delete();
            sign.insert(shopLocation);
        }
        return shopLocation;
    }

    public Sign createLottoSign(OfflinePlayer player, Block block, ShopMode mode, double price) {
        Sign shopLocation = new Sign();
        shopLocation.owner = player.getUniqueId();
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        shopLocation.lotto_price = price;
        try (TransactionalQuery<Sign> sign = database.transaction(Sign.class).whereEq("id", shopLocation.id)) {
            if (sign != null) {
                sign.delete();
                sign.insert(shopLocation);
            }
        }
        return shopLocation;
    }

    public boolean removeShopSign(Block block) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(block.getLocation());
        try (TransactionalQuery<Sign> sign = database.transaction(Sign.class).whereEq("id", shopLocation.id)) {
            if (sign != null) {
                sign.delete();
                return true;
            }
        }
        return false;
    }

    public boolean removeShopSign(String world, int x, int y, int z) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(world, x, y, z);
        try (TransactionalQuery<Sign> sign = database.transaction(Sign.class).whereEq("id", shopLocation.id)) {
            if (sign != null) {
                sign.delete();
                return true;
            }
        }
        return false;
    }

    public List<SignShop> getSignShops() {
        return database.auto(SignShop.class).select();
    }

    public SignShop getSignShop(UUID owner) {
        try (TransactionalQuery<SignShop> shop = database.transaction(SignShop.class).whereEq("id", owner.toString())) {
            if (shop != null && shop.count() == 1) {
                return shop.selectUnique();
            }
        }
        SignShop s = new SignShop();
        s.owner = owner;
        return s;
    }

    public void setSignShop(UUID owner, SignShop shop) {
        try (TransactionalQuery<SignShop> s = database.transaction(SignShop.class).whereEq("id", owner.toString())) {
            s.delete();
            s.insert(shop);
        }
    }

    public ShopStorageLocation getChestLocation(UUID owner) {
        return database.auto(ShopStorageLocation.class).whereEq("owner", owner.toString()).selectUniqueUnchecked();
    }

    public void setChestLocation(UUID owner, ShopStorageLocation location) {
        try (TransactionalQuery<ShopStorageLocation> s = database.transaction(ShopStorageLocation.class).whereEq("owner", owner.toString())) {
            s.delete();
            s.insert(location);
        }

    }

    public LottoStorageLocation getLottoStorageLocation(UUID owner) {
        return database.auto(LottoStorageLocation.class).whereEq("owner", owner.toString()).selectUniqueUnchecked();
    }

    public void setLottoStorageLocation(UUID owner, LottoStorageLocation location) {
        try (TransactionalQuery<LottoStorageLocation> s = database.transaction(LottoStorageLocation.class).whereEq("owner", owner.toString())) {
            s.delete();
            s.insert(location);
        }
    }
}

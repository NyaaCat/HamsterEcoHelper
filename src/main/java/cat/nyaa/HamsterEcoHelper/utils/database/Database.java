package cat.nyaa.HamsterEcoHelper.utils.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.ItemLog;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.TempStorageRepo;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.LottoStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.ShopStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.Sign;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.SignShop;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.Query;
import cat.nyaa.nyaacore.database.RelationalDB;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Database implements Cloneable {
    private final RelationalDB db;
    private final HamsterEcoHelper plugin;

    private Database(RelationalDB db, HamsterEcoHelper plugin){
        this.db = db;
        this.plugin = plugin;
    }

    public Database(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        db = DatabaseUtils.get();
        db.connect();
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo> result = db.query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
        if (result == null || result.count() == 0) return Collections.emptyList();
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

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        Query<TempStorageRepo> result = db.query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
        YamlConfiguration cfg = new YamlConfiguration();
        boolean update;
        if (result == null || result.count() == 0) {
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
        bean.setPlayerId(player.getUniqueId());
        bean.yaml = cfg.saveToString();
        if (update) {
            result.update(bean);
        } else {
            db.query(TempStorageRepo.class).insert(bean);
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo> query = db.query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
        if (query != null && query.count() != 0) {
            query.delete();
        }
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem> list = new ArrayList<>();
        Query<MarketItem> result;
        if (seller == null) {
            result = db.query(MarketItem.class).where("amount", ">", 0);
        } else {
            result = db.query(MarketItem.class).where("amount", ">", 0).whereEq("player_id", seller.toString());
        }
        if (result != null && result.count() > 0) {
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

    public long marketOffer(Player player, ItemStack itemStack, double unit_price) {
        MarketItem item = new MarketItem();
        item.setItemStack(itemStack);
        item.amount = itemStack.getAmount();
        item.setPlayerId(player.getUniqueId());
        item.setUnitPrice(unit_price);
        long id = 1;
        for (MarketItem marketItem : db.query(MarketItem.class).select()) {
            if (marketItem.id >= id) {
                id = marketItem.id + 1;
            }
        }
        item.setId(id);
        db.query(MarketItem.class).insert(item);
        return item.getId();
    }

    public void marketBuy(Player player, long itemId, int amount) {
        Query<MarketItem> query = db.query(MarketItem.class).whereEq("id", itemId);
        if (query != null && query.count() != 0) {
            MarketItem mItem = query.selectUnique();
            mItem.setAmount(mItem.getAmount() - amount);
            mItem.setId(itemId);
            query.update(mItem);
        }
        return;
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        Query<MarketItem> query = db.query(MarketItem.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0);
        if (query != null && query.count() > 0) {
            return query.count();
        }
        return 0;
    }

    public int getMarketItemCount() {
        Query<MarketItem> query = db.query(MarketItem.class).where("amount", ">", 0);
        if (query != null && query.count() != 0) {
            return query.count();
        }
        return 0;
    }

    public MarketItem getMarketItem(long id) {
        Query<MarketItem> query = db.query(MarketItem.class).whereEq("id", id);
        if (query != null && query.count() != 0) {
            return query.selectUnique();
        }
        return null;
    }


    public ItemLog getItemLog(long id) {
        Query<ItemLog> log = db.query(ItemLog.class).whereEq("id", id);
        if (log != null && log.count() != 0) {
            return log.selectUnique();
        }
        return null;
    }

    public long addItemLog(OfflinePlayer player, ItemStack item, double price, int amount) {
        ItemLog i = new ItemLog();
        i.setOwner(player.getUniqueId());
        i.setItemStack(item);
        i.setPrice(price);
        i.amount = amount;
        long id = 1;
        for (ItemLog log : db.query(ItemLog.class).select()) {
            if (log.id >= id) {
                id = log.id + 1;
            }
        }
        i.setId(id);
        db.query(ItemLog.class).insert(i);
        return i.getId();
    }

    public List<Sign> getShopSigns() {
        return db.query(Sign.class).select();
    }

    public Sign createShopSign(OfflinePlayer player, Block block, ShopMode mode) {
        Sign shopLocation = new Sign();
        shopLocation.setOwner(player.getUniqueId());
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        Query sign = db.query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
        }
        db.query(Sign.class).insert(shopLocation);
        return shopLocation;
    }

    public Sign createLottoSign(OfflinePlayer player, Block block, ShopMode mode, double price) {
        Sign shopLocation = new Sign();
        shopLocation.setOwner(player.getUniqueId());
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        shopLocation.setLotto_price(price);
        Query sign = db.query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
        }
        db.query(Sign.class).insert(shopLocation);
        return shopLocation;
    }

    public boolean removeShopSign(Block block) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(block.getLocation());
        Query sign = db.query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
            return true;
        }
        return false;
    }

    public boolean removeShopSign(String world, int x, int y, int z) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(world, x, y, z);
        Query sign = db.query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
            return true;
        }
        return false;
    }

    public List<SignShop> getSignShops() {
        return db.query(SignShop.class).select();
    }

    public SignShop getSignShop(UUID owner) {
        Query<SignShop> shop = db.query(SignShop.class).whereEq("id", owner.toString());
        if (shop != null && shop.count() == 1) {
            return shop.selectUnique();
        }
        SignShop s = new SignShop();
        s.setOwner(owner);
        return s;
    }

    public void setSignShop(UUID owner, SignShop shop) {
        Query s = db.query(SignShop.class).whereEq("id", owner.toString());
        if (s != null) {
            s.delete();
        }
        db.query(SignShop.class).insert(shop);
    }

    public ShopStorageLocation getChestLocation(UUID owner) {
        Query<ShopStorageLocation> loc = db.query(ShopStorageLocation.class).whereEq("owner", owner.toString());
        if (loc != null && loc.count() != 0) {
            return loc.selectUnique();
        }
        return null;
    }

    public void setChestLocation(UUID owner, ShopStorageLocation location) {
        Query s = db.query(ShopStorageLocation.class).whereEq("owner", owner.toString());
        if (s != null) {
            s.delete();
        }
        db.query(ShopStorageLocation.class).insert(location);
    }

    public LottoStorageLocation getLottoStorageLocation(UUID owner) {
        Query<LottoStorageLocation> loc = db.query(LottoStorageLocation.class).whereEq("owner", owner.toString());
        if (loc != null && loc.count() != 0) {
            return loc.selectUnique();
        }
        return null;
    }

    public void setLottoStorageLocation(UUID owner, LottoStorageLocation location) {
        Query s = db.query(LottoStorageLocation.class).whereEq("owner", owner.toString());
        if (s != null) {
            s.delete();
        }
        db.query(LottoStorageLocation.class).insert(location);
    }

    protected Object clone() throws CloneNotSupportedException {
        if(db == null) throw new CloneNotSupportedException();
        return new Database(db, plugin);
    }

}

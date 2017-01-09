package cat.nyaa.HamsterEcoHelper.utils.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.ItemLog;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.TempStorageRepo;
import cat.nyaa.utils.database.SQLiteDatabase;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database extends SQLiteDatabase {
    private final HamsterEcoHelper plugin;

    public Database(HamsterEcoHelper plugin) {
        super();
        this.plugin = plugin;
        connect();
    }

    @Override
    protected String getFileName() {
        return "HamsterEcoHelper.db";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected Class<?>[] getTables() {
        return new Class<?>[]{
                TempStorageRepo.class,
                ItemLog.class,
                MarketItem.class
        };
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo> result = query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
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
        Query<TempStorageRepo> result = query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
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
            query(TempStorageRepo.class).insert(bean);
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo> query = query(TempStorageRepo.class).whereEq("player_id", player.getUniqueId().toString());
        if (query != null && query.count() != 0) {
            query.delete();
        }
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem> list = new ArrayList<>();
        Query<MarketItem> result;
        if (seller == null) {
            result = query(MarketItem.class).where("amount", ">", 0);
        } else {
            result = query(MarketItem.class).where("amount", ">", 0).whereEq("player_id", seller.toString());
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
        item.setAmount(itemStack.getAmount());
        item.setPlayerId(player.getUniqueId());
        item.setUnitPrice(unit_price);
        item.setId(query(MarketItem.class).count() + 1);
        query(MarketItem.class).insert(item);
        return item.getId();
    }

    public void marketBuy(Player player, long itemId, int amount) {
        Query<MarketItem> query = query(MarketItem.class).whereEq("id", itemId);
        if (query != null && query.count() != 0) {
            MarketItem mItem = query.selectUnique();
            mItem.setAmount(mItem.getAmount() - amount);
            mItem.setId(itemId);
            query.update(mItem);
        }
        return;
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        Query<MarketItem> query = query(MarketItem.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0);
        if (query != null && query.count() > 0) {
            return query.count();
        }
        return 0;
    }

    public int getMarketPageCount() {
        Query<MarketItem> query = query(MarketItem.class).where("amount", ">", 0);
        if (query == null || query.count() == 0) {
            return 0;
        }
        return (query.count() + MarketManager.pageSize - 1) / MarketManager.pageSize;
    }

    public int getMarketItemCount() {
        Query<MarketItem> query = query(MarketItem.class).where("amount", ">", 0);
        if (query != null && query.count() != 0) {
            return query.count();
        }
        return 0;
    }

    public MarketItem getMarketItem(long id) {
        Query<MarketItem> query = query(MarketItem.class).whereEq("id", id);
        if (query != null && query.count() != 0) {
            return query.selectUnique();
        }
        return null;
    }


    public ItemLog getItemLog(long id) {
        Query<ItemLog> log = query(ItemLog.class).whereEq("id", id);
        if (log != null && log.count() != 0) {
            return log.selectUnique();
        }
        return null;
    }

    public long addItemLog(OfflinePlayer player, ItemStack item, int price, int amount) {
        ItemLog i = new ItemLog();
        i.setOwner(player.getUniqueId());
        i.setItemStack(item);
        i.setPrice(price);
        i.setAmount(amount);
        i.setId(query(ItemLog.class).count() + 1);
        this.query(ItemLog.class).insert(i);
        return i.getId();
    }
}

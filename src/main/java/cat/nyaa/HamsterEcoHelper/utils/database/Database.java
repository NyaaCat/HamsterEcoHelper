package cat.nyaa.HamsterEcoHelper.utils.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopItem;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.ItemDB;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem_old;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.TempStorageRepo;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.*;
import cat.nyaa.nyaacore.database.SQLiteDatabase;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
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
        yamlToNBT();
    }

    @Override
    protected String getFileName() {
        return "HamsterEcoHelper.db";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Class<?>[] getTables() {
        return new Class<?>[]{
                TempStorageRepo.class,
                MarketItem_old.class,
                Sign.class,
                ShopStorageLocation.class,
                SignShop.class,
                LottoStorageLocation.class,
                ItemDB.class,
                SignShopItem.class,
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
        item.itemID = getItemID(itemStack);
        item.setAmount(itemStack.getAmount());
        item.playerId = player.getUniqueId();
        item.unitPrice = unit_price;
        long id = 1;
        for (MarketItem marketItem : query(MarketItem.class).select()) {
            if (marketItem.id >= id) {
                id = marketItem.id + 1;
            }
        }
        item.id = id;
        query(MarketItem.class).insert(item);
        return item.id;
    }

    public void marketBuy(long itemId, int amount) {
        Query<MarketItem> query = query(MarketItem.class).whereEq("id", itemId);
        if (query != null && query.count() != 0) {
            MarketItem mItem = query.selectUnique();
            mItem.setAmount(mItem.getAmount() - amount);
            mItem.id = itemId;
            query.update(mItem);
        }
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        Query<MarketItem> query = query(MarketItem.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0);
        if (query != null && query.count() > 0) {
            return query.count();
        }
        return 0;
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

    public List<Sign> getShopSigns() {
        return query(Sign.class).select();
    }

    public Sign createShopSign(OfflinePlayer player, Block block, ShopMode mode) {
        Sign shopLocation = new Sign();
        shopLocation.setOwner(player.getUniqueId());
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        Query sign = query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
        }
        this.query(Sign.class).insert(shopLocation);
        return shopLocation;
    }

    public Sign createLottoSign(OfflinePlayer player, Block block, ShopMode mode, double price) {
        Sign shopLocation = new Sign();
        shopLocation.setOwner(player.getUniqueId());
        shopLocation.setLocation(block.getLocation());
        shopLocation.shopMode = mode;
        shopLocation.setLotto_price(price);
        Query sign = query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
        }
        this.query(Sign.class).insert(shopLocation);
        return shopLocation;
    }

    public boolean removeShopSign(Block block) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(block.getLocation());
        Query sign = query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
            return true;
        }
        return false;
    }

    public boolean removeShopSign(String world, int x, int y, int z) {
        Sign shopLocation = new Sign();
        shopLocation.setLocation(world, x, y, z);
        Query sign = query(Sign.class).whereEq("id", shopLocation.getId());
        if (sign != null) {
            sign.delete();
            return true;
        }
        return false;
    }

    public List<Sign> getSignShops() {
        return query(Sign.class).select();
    }

    public List<SignShopItem> getSignShopItems(UUID owner, ShopMode type) {
        Query<SignShopItem> items = query(SignShopItem.class).whereEq("player_id", owner.toString())
                .where("amount", ">", 0);
        if (items != null && type != null) {
            items = items.whereEq("type", type);
        }
        if (items != null && items.count() > 0) {
            List<SignShopItem> tmp = items.select();
            Collections.reverse(tmp);
            return tmp;
        }
        return new ArrayList<SignShopItem>();
    }

    public void updateSignShopItem(UUID owner, SignShopItem item) {
        Query<SignShopItem> query = query(SignShopItem.class).whereEq("id", item.id);
        if (query != null && query.count() > 0) {
            SignShopItem signShopItem = query.selectUnique();
            signShopItem.amount = item.amount;
            signShopItem.playerId = owner;
            signShopItem.unitPrice = item.unitPrice;
            signShopItem.type = item.type;
            signShopItem.itemID = item.itemID;
            query.update(item);
        }
    }

    public SignShopItem getSignShopItem(long id) {
        Query<SignShopItem> query = query(SignShopItem.class).whereEq("id", id);
        if (query != null && query.count() > 0) {
            return query.selectUnique();
        }
        return null;
    }

    public void removeSignShopItem(long id) {
        Query<SignShopItem> query = query(SignShopItem.class).whereEq("id", id);
        if (query != null && query.count() > 0) {
            query.delete();
        }
    }

    public Long addItemToSignShop(UUID owner, ItemStack itemStack, double unitPrice, ShopMode type) {
        SignShopItem item = new SignShopItem();
        item.itemID = getItemID(itemStack);
        item.setAmount(itemStack.getAmount());
        item.playerId = owner;
        item.unitPrice = unitPrice;
        item.type = type;
        long id = 1;
        for (SignShopItem shopItem : query(SignShopItem.class).select()) {
            if (shopItem.id >= id) {
                id = shopItem.id + 1;
            }
        }
        item.id = id;
        query(SignShopItem.class).insert(item);
        return item.id;
    }

    public ShopStorageLocation getChestLocation(UUID owner) {
        Query<ShopStorageLocation> loc = query(ShopStorageLocation.class).whereEq("owner", owner.toString());
        if (loc != null && loc.count() != 0) {
            return loc.selectUnique();
        }
        return null;
    }

    public void setChestLocation(UUID owner, ShopStorageLocation location) {
        Query s = query(ShopStorageLocation.class).whereEq("owner", owner.toString());
        if (s != null) {
            s.delete();
        }
        query(ShopStorageLocation.class).insert(location);
    }

    public LottoStorageLocation getLottoStorageLocation(UUID owner) {
        Query<LottoStorageLocation> loc = query(LottoStorageLocation.class).whereEq("owner", owner.toString());
        if (loc != null && loc.count() != 0) {
            return loc.selectUnique();
        }
        return null;
    }

    public void setLottoStorageLocation(UUID owner, LottoStorageLocation location) {
        Query s = query(LottoStorageLocation.class).whereEq("owner", owner.toString());
        if (s != null) {
            s.delete();
        }
        query(LottoStorageLocation.class).insert(location);
    }

    public ItemStack getItemByID(long id) {
        Query<ItemDB> query = query(ItemDB.class).whereEq("id", id);
        if (query != null && query.count() != 0) {
            return query.selectUnique().getItemStack();
        }
        return null;
    }

    public long getItemID(ItemStack item) {
        ItemDB i = new ItemDB();
        i.setItemStack(item.clone());
        Query<ItemDB> r = query(ItemDB.class).whereEq("item", ItemStackUtils.itemToBase64(i.getItemStack()));
        if (r != null && r.count() > 0) {
            return r.selectUnique().id;
        } else {
            long id = 1L;
            List<ItemDB> query = query(ItemDB.class).select();
            if (query != null && query(ItemDB.class).count() > 0) {
                for (ItemDB data : query) {
                    if (data.id >= id) {
                        id = data.id + 1;
                    }
                }
            }
            i.id = id;
            this.query(ItemDB.class).insert(i);
            return i.id;
        }
    }

    @SuppressWarnings("deprecation")
    public void yamlToNBT() {
        if (getItemByID(1L) != null) {
            return;
        } else {
            plugin.logger.warning("convert item database...");
        }
        List<MarketItem_old> MarketItems = query(MarketItem_old.class).select();
        if (MarketItems != null && MarketItems.size() > 0) {
            for (MarketItem_old item : MarketItems) {
                if (item.getAmount() > 0) {
                    MarketItem mItem = new MarketItem();
                    plugin.logger.info("market item id: " + item.getId());
                    mItem.id = item.getId();
                    mItem.setAmount(item.amount);
                    mItem.itemID = getItemID(item.getItemStack());
                    mItem.playerId = item.getPlayerId();
                    mItem.unitPrice = item.getUnitPrice();
                    query(MarketItem.class).insert(mItem);
                }
            }
        }
        List<SignShop> signShops = query(SignShop.class).select();
        if (MarketItems != null && MarketItems.size() > 0) {
            for (SignShop shop : signShops) {
                for (ShopMode mode : ShopMode.values()) {
                    if (mode == ShopMode.LOTTO) {
                        continue;
                    }
                    plugin.logger.info("signshop: " + shop.getOwner().toString() + " " + mode.name());
                    for (ShopItem item : shop.getItems(mode)) {
                        if (item.getAmount() > 0) {
                            addItemToSignShop(shop.getPlayer().getUniqueId(), item.itemStack, item.getUnitPrice(), mode);
                        }
                    }
                }
            }
        }
        getItemID(new ItemStack(Material.STONE));
    }
}

package cat.nyaa.HamsterEcoHelper.utils.database;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.signshop.ShopItem;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.*;
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
                MarketItem.class,
                Sign.class,
                ShopStorageLocation.class,
                SignShop.class,
                LottoStorageLocation.class,
                ItemDB.class,
                SignShopItem.class,
                MarketItem_v2.class,
                TempStorageRepo_v2.class
        };
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo_v2> result = query(TempStorageRepo_v2.class).whereEq("player_id", player.getUniqueId().toString());
        if (result == null || result.count() == 0) return Collections.emptyList();
        return result.selectUnique().getItems();
    }

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        Query<TempStorageRepo_v2> result = query(TempStorageRepo_v2.class).whereEq("player_id", player.getUniqueId().toString());
        boolean update;
        List<ItemStack> items = new ArrayList<>();
        items.add(item.clone());
        if (result == null || result.count() == 0) {
            update = false;
        } else {
            update = true;
            items.addAll(result.selectUnique().getItems());
        }
        TempStorageRepo_v2 bean = new TempStorageRepo_v2();
        bean.setPlayerId(player.getUniqueId());
        bean.setItems(items);
        if (update) {
            result.update(bean);
        } else {
            query(TempStorageRepo_v2.class).insert(bean);
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        Query<TempStorageRepo_v2> query = query(TempStorageRepo_v2.class).whereEq("player_id", player.getUniqueId().toString());
        if (query != null && query.count() != 0) {
            query.delete();
        }
    }

    public List<MarketItem_v2> getMarketItems(int offset, int limit, UUID seller) {
        ArrayList<MarketItem_v2> list = new ArrayList<>();
        Query<MarketItem_v2> result;
        if (seller == null) {
            result = query(MarketItem_v2.class).where("amount", ">", 0);
        } else {
            result = query(MarketItem_v2.class).where("amount", ">", 0).whereEq("player_id", seller.toString());
        }
        if (result != null && result.count() > 0) {
            List<MarketItem_v2> tmp = result.select();
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
        MarketItem_v2 item = new MarketItem_v2();
        item.itemID = getItemID(itemStack);
        item.setAmount(itemStack.getAmount());
        item.setPlayerId(player.getUniqueId());
        item.setUnitPrice(unit_price);
        long id = 1;
        for (MarketItem_v2 marketItem : query(MarketItem_v2.class).select()) {
            if (marketItem.getId() >= id) {
                id = marketItem.getId() + 1;
            }
        }
        item.setId(id);
        query(MarketItem_v2.class).insert(item);
        return item.getId();
    }

    public void marketBuy(long itemId, int amount) {
        Query<MarketItem_v2> query = query(MarketItem_v2.class).whereEq("id", itemId);
        if (query != null && query.count() != 0) {
            MarketItem_v2 mItem = query.selectUnique();
            mItem.setAmount(mItem.getAmount() - amount);
            mItem.setId(itemId);
            query.update(mItem);
        }
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        Query<MarketItem_v2> query = query(MarketItem_v2.class).whereEq("player_id", player.getUniqueId().toString()).where("amount", ">", 0);
        if (query != null && query.count() > 0) {
            return query.count();
        }
        return 0;
    }

    public int getMarketItemCount() {
        Query<MarketItem_v2> query = query(MarketItem_v2.class).where("amount", ">", 0);
        if (query != null && query.count() != 0) {
            return query.count();
        }
        return 0;
    }

    public MarketItem_v2 getMarketItem(long id) {
        Query<MarketItem_v2> query = query(MarketItem_v2.class).whereEq("id", id);
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
        Query<SignShopItem> query = query(SignShopItem.class).whereEq("id", item.getId());
        if (query != null && query.count() > 0) {
            SignShopItem signShopItem = query.selectUnique();
            signShopItem.setAmount(item.getAmount());
            signShopItem.setPlayerId(owner);
            signShopItem.setUnitPrice(item.getUnitPrice());
            signShopItem.setType(item.getType());
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
        item.setPlayerId(owner);
        item.setUnitPrice(unitPrice);
        item.setType(type);
        long id = 1;
        for (SignShopItem shopItem : query(SignShopItem.class).select()) {
            if (shopItem.getId() >= id) {
                id = shopItem.getId() + 1;
            }
        }
        item.setId(id);
        query(SignShopItem.class).insert(item);
        return item.getId();
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
            return r.selectUnique().getId();
        } else {
            long id = 1L;
            List<ItemDB> query = query(ItemDB.class).select();
            if (query != null && query(ItemDB.class).count() > 0) {
                for (ItemDB data : query) {
                    if (data.getId() >= id) {
                        id = data.getId() + 1;
                    }
                }
            }
            i.setId(id);
            this.query(ItemDB.class).insert(i);
            return i.getId();
        }
    }

    @SuppressWarnings("deprecation")
    public void yamlToNBT() {
        if (getItemByID(1L) != null) {
            return;
        } else {
            plugin.logger.warning("convert item database...");
        }
        List<MarketItem> MarketItems = query(MarketItem.class).select();
        if (MarketItems != null && MarketItems.size() > 0) {
            for (MarketItem item : MarketItems) {
                if (item.getAmount() > 0) {
                    MarketItem_v2 mItem = new MarketItem_v2();
                    plugin.logger.info("market item id: " + item.getId());
                    mItem.setId(item.getId());
                    mItem.setAmount(item.amount);
                    mItem.itemID = getItemID(item.getItemStack(1));
                    mItem.setPlayerId(item.getPlayerId());
                    mItem.setUnitPrice(item.getUnitPrice());
                    query(MarketItem_v2.class).insert(mItem);
                }
            }
        }
        List<SignShop> signShops = query(SignShop.class).select();
        if (MarketItems != null && MarketItems.size() > 0) {
            long id = 1L;
            for (SignShop shop : signShops) {
                for (ShopMode mode : ShopMode.values()) {
                    if (mode == ShopMode.LOTTO) {
                        continue;
                    }
                    plugin.logger.info("signshop: " + shop.getOwner().toString() + " " + mode.name());
                    for (ShopItem item : shop.getItems(mode)) {
                        if (item.getAmount() > 0) {
                            SignShopItem signShopItem = new SignShopItem();
                            signShopItem.setId(id);
                            signShopItem.setAmount(item.getAmount());
                            signShopItem.itemID = getItemID(item.getItemStack(1));
                            signShopItem.setPlayerId(shop.getPlayer().getUniqueId());
                            signShopItem.setUnitPrice(item.getUnitPrice());
                            signShopItem.setType(mode);
                            query(SignShopItem.class).insert(signShopItem);
                            id++;
                        }
                    }
                }
            }
        }
        List<TempStorageRepo> repos = query(TempStorageRepo.class).select();
        if (repos != null && !repos.isEmpty()) {
            for (TempStorageRepo repo : repos) {
                if (repo != null) {
                    YamlConfiguration cfg = new YamlConfiguration();
                    try {
                        cfg.loadFromString(repo.yaml);
                    } catch (InvalidConfigurationException ex) {
                        ex.printStackTrace();
                        continue;
                    }
                    List<ItemStack> items = new ArrayList<>();
                    for (String key : cfg.getKeys(false)) {
                        items.add(cfg.getItemStack(key));
                    }
                    TempStorageRepo_v2 s = new TempStorageRepo_v2();
                    s.setPlayerId(repo.getPlayerId());
                    s.setItems(items);
                    query(TempStorageRepo_v2.class).insert(s);
                }
            }
        }
        getItemID(new ItemStack(Material.STONE));
    }
}

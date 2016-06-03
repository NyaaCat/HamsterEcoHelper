package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.validation.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import java.util.*;

public class Database {
    private final HamsterEcoHelper plugin;
    private final EbeanServer db;

    public Database(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        db = plugin.getDatabase();
        try {
            db.find(TempStorageRepo.class).findRowCount();
        } catch (PersistenceException ex) {
            plugin.logger.info(I18n.get("internal.info.installing_db"));
            plugin.installDDL();
        }
    }

    public EbeanServer getDB() {
        return db;
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        TempStorageRepo result = db.find(TempStorageRepo.class, player.getUniqueId());
        if (result == null) return Collections.emptyList();
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(result.yaml);
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
        TempStorageRepo result = db.find(TempStorageRepo.class, player.getUniqueId());
        YamlConfiguration cfg = new YamlConfiguration();
        boolean update;
        if (result == null) {
            update = false;
            cfg.set("0", item);
        } else {
            update = true;
            YamlConfiguration tmp = new YamlConfiguration();
            try {
                tmp.loadFromString(result.yaml);
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
            db.update(bean);
        } else {
            db.insert(bean);
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        db.delete(TempStorageRepo.class, player.getUniqueId());
    }

    public static List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(TempStorageRepo.class);
        list.add(MarketItem.class);
        list.add(Mailbox.class);
        return list;
    }

    @Entity
    @Table(name = "temporary_storage")
    public static class TempStorageRepo {
        @Id
        @NotNull
        public UUID playerId = UUID.randomUUID();
        @NotNull
        public String yaml = "";

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID playerId) {
            this.playerId = playerId;
        }

        public String getYaml() {
            return Base64.getEncoder().encodeToString(yaml.getBytes());
        }

        public void setYaml(String yaml) {
            this.yaml = new String(Base64.getDecoder().decode(yaml));
        }
    }

    public boolean addItemToMailbox(Player player, ItemStack item) {
        Mailbox mailbox = db.find(Mailbox.class, player.getUniqueId());
        int amount = item.getAmount();
        ItemStack[] items = null;
        if (mailbox == null) {
            mailbox = new Mailbox();
            items = mailbox.getMailbox();
        } else {
            items = mailbox.getMailbox();
        }
        if (items != null && items.length > 0) {
            for (int slot = 0; slot < items.length; slot++) {
                ItemStack tmp = items[slot];
                if (tmp != null && tmp.isSimilar(item) && tmp.getAmount() < item.getMaxStackSize()) {
                    if ((tmp.getAmount() + amount) <= item.getMaxStackSize()) {
                        tmp.setAmount(amount + tmp.getAmount());
                        items[slot] = tmp;
                        setMailbox(player, items);
                        return true;
                    } else {
                        amount = amount - (item.getMaxStackSize() - tmp.getAmount());
                        tmp.setAmount(item.getMaxStackSize());
                        items[slot] = tmp;
                        continue;
                    }
                } else if (tmp == null || tmp.getType() == Material.AIR) {
                    item.setAmount(amount);
                    items[slot] = item;
                    setMailbox(player, items);
                    return true;
                }
            }
        }
        return false;
    }

    public List<MarketItem> getMarketItems(int offset, int limit, UUID seller) {
        Query<MarketItem> list;
        if (seller == null) {
            list = db.find(MarketItem.class).where().ge("amount", 1).order().desc("id").setFirstRow(offset).setMaxRows(limit);
            return list.findList();
        } else {
            list = db.find(MarketItem.class).where().ge("amount", 1).eq("playerId", seller).order().desc("id").setFirstRow(offset).setMaxRows(limit);
            return list.findList();
        }
    }

    public int marketOffer(Player player, ItemStack itemStack, double unit_price) {
        MarketItem item = new MarketItem();
        item.setItemStack(itemStack);
        item.setAmount(itemStack.getAmount());
        item.setPlayerId(player.getUniqueId());
        item.setUnitPrice(unit_price);
        db.save(item);
        return item.getId();
    }

    public void marketBuy(Player player, int itemId, int amount) {
        MarketItem mItem = db.find(MarketItem.class, itemId);
        if (mItem != null) {
            mItem.setAmount(mItem.getAmount() - amount);
            db.update(mItem);
        }
        return;
    }

    public int getMarketPlayerItemCount(OfflinePlayer player) {
        int count = db.find(MarketItem.class).where().ge("amount", 1).eq("playerId", player.getUniqueId()).findRowCount();
        if (count > 0) {
            return count;
        }
        return 0;
    }

    public int getMarketPageCount() {
        int count = db.find(MarketItem.class).findRowCount();
        if (count > 0) {
            return db.find(MarketItem.class).where().ge("amount", 1).findPagingList(MarketManager.pageSize).getTotalPageCount();
        }
        return 0;
    }

    public MarketItem getMarketItem(int id) {
        MarketItem mItem = db.find(MarketItem.class, id);
        if (mItem != null) {
            return mItem;
        }
        return null;
    }

    public ItemStack[] getMailbox(Player player) {
        Mailbox mailbox = db.find(Mailbox.class, player.getUniqueId());
        if (mailbox != null && mailbox.getMailbox() != null) {
            return mailbox.getMailbox();
        }
        return new ItemStack[54];
    }

    public void setMailbox(Player player, ItemStack[] inventory) {
        Mailbox mailbox = db.find(Mailbox.class, player.getUniqueId());
        Mailbox tmp = new Mailbox();
        tmp.setMailbox(player, inventory);
        if (mailbox != null) {
            db.delete(mailbox);
        }
        db.insert(tmp);
        return;
    }

    @Entity
    @Table(name = "mailbox")
    public static class Mailbox {
        @Id
        @NotNull
        public UUID playerId;
        @NotNull
        public String inv = "";

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID uuid) {
            this.playerId = uuid;
        }

        public String getInv() {
            return inv;
        }

        public void setInv(String inv) {
            this.inv = inv;
        }

        public ItemStack[] getMailbox() {
            if (inv != null && inv.length() > 0) {
                YamlConfiguration yaml = new YamlConfiguration();
                try {
                    yaml.loadFromString(new String(Base64.getDecoder().decode(inv)));
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                return ((List<ItemStack>) yaml.get("inv")).toArray(new ItemStack[0]);
            }
            return new ItemStack[54];
        }

        public boolean setMailbox(Player player, ItemStack[] items) {
            playerId = player.getUniqueId();
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("inv", items);
            this.inv = Base64.getEncoder().encodeToString(yaml.saveToString().getBytes());
            return true;
        }
    }

    @Entity
    @Table(name = "market")
    public static class MarketItem {
        @Id
        public int id;
        @NotNull
        public UUID playerId;
        @NotNull
        public String item;
        @NotNull
        private int amount;
        @NotNull
        private double unitPrice;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID uuid) {
            this.playerId = uuid;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public ItemStack getItemStack() {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.loadFromString(new String(Base64.getDecoder().decode(item)));
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ItemStack itemStack = yaml.getItemStack("item");
            itemStack.setAmount(this.amount);
            return itemStack;
        }

        public ItemStack getItemStack(int amount) {
            ItemStack item = getItemStack();
            item.setAmount(amount);
            return item;
        }

        public void setItemStack(ItemStack item) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("item", item);
            this.item = Base64.getEncoder().encodeToString(yaml.saveToString().getBytes());
            amount = item.getAmount();
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unit_price) {
            this.unitPrice = unit_price;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getPlayerName() {
            return Bukkit.getOfflinePlayer(playerId).getName();
        }

        public OfflinePlayer getPlayer() {
            return Bukkit.getOfflinePlayer(playerId);
        }
    }
}

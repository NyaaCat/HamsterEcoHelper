package cat.nyaa.HamsterEcoHelper.market;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLite extends Database {
    private Plugin plugin;
    private Connection conn;

    public void init(Plugin pl) {
        plugin = pl;
        try {
            File file = new File(plugin.getDataFolder(), "market.db");
            if (!file.exists()) {
                file.createNewFile();
            }
            this.conn = DriverManager.getConnection("jdbc:SQLite:" + file.getAbsolutePath());
            ResultSet result = this.conn.prepareStatement("SELECT name FROM sqlite_master WHERE type=\"table\" ORDER BY name;").executeQuery();
            while (result.next()) {
                if (result.getString("name").equals("heh_market")) {
                    return;
                }
            }
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try {
            this.conn.prepareStatement("CREATE TABLE \"heh_market\" (\n" +
                    "\t`id`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`player_uuid`\tTEXT NOT NULL,\n" +
                    "\t`itemstack`\tBLOB NOT NULL,\n" +
                    "\t`unit_price`\tDOUBLE NOT NULL,\n" +
                    "\t`amount`\tINTEGER NOT NULL\n" +
                    ");").executeUpdate();
            this.conn.prepareStatement("CREATE  TABLE \"heh_market_player\" (\"player_uuid\" TEXT PRIMARY KEY  NOT NULL  UNIQUE , \"mailbox\" BLOB)").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getItemCount() {
        try {
            ResultSet result = this.conn.prepareStatement("SELECT COUNT(*) FROM heh_market").executeQuery();
            while (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<MarketItem> getItems(int offset, int limit, String seller) {
        List<MarketItem> list = new ArrayList<>();
        try {
            PreparedStatement stms;
            ResultSet result;
            if (seller.length() > 1) {
                stms = conn.prepareStatement("SELECT * FROM heh_market WHERE  \"player_uuid\" =? ORDER BY id DESC LIMIT ?,?;");
                stms.setString(1, seller);
                stms.setInt(2, offset);
                stms.setInt(3, limit);
            } else {
                stms = conn.prepareStatement("SELECT * FROM heh_market ORDER BY id DESC LIMIT ?,?;");
                stms.setInt(1, offset);
                stms.setInt(2, limit);

            }
            result = stms.executeQuery();
            while (result.next()) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(result.getString("itemstack"));
                MarketItem mItem = new MarketItem();
                mItem.setItemStack(yaml.getItemStack("item"));
                mItem.setId(result.getInt("id"));
                mItem.setAmount(result.getInt("amount"));
                mItem.setUnit_price(result.getDouble("unit_price"));
                mItem.setPlayer_uuid(result.getString("player_uuid"));
                list.add(mItem);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void offer(Player player, ItemStack itemStack, double unit_price, int amount) {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("item", itemStack);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO \"main\".\"heh_market\" (\"player_uuid\",\"itemstack\",\"unit_price\",\"amount\") VALUES (?,?,?,?)");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, yaml.saveToString());
            stmt.setDouble(3, unit_price);
            stmt.setInt(4, amount);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    public void buy(Player player, int itemId, int amount) {
        try {
            MarketItem item = getMarketItem(itemId);
            if (item.getAmount() == amount) {
                conn.prepareStatement("DELETE FROM heh_market WHERE id=" + itemId).execute();
                return;
            } else {
                PreparedStatement stmt = conn.prepareStatement("UPDATE \"main\".\"heh_market\" SET \"amount\" = ? WHERE  \"id\" = " + itemId);
                stmt.setInt(1, item.getAmount() - amount);
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    public int getPlayerItemCount(OfflinePlayer player) {
        try {
            PreparedStatement stmt = this.conn.prepareStatement("SELECT COUNT(*) FROM heh_market WHERE  \"player_uuid\" =?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MarketItem getMarketItem(int id) {
        try {
            ResultSet result = conn.prepareStatement("SELECT * FROM heh_market WHERE id=" + id + ";").executeQuery();
            while (result.next()) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(result.getString("itemstack"));
                MarketItem mItem = new MarketItem();
                mItem.setItemStack(yaml.getItemStack("item"));
                mItem.setId(id);
                mItem.setAmount(result.getInt("amount"));
                mItem.setUnit_price(result.getInt("unit_price"));
                mItem.setPlayer_uuid(result.getString("player_uuid"));
                return mItem;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemStack[] getMailbox(Player player) {
        try {
            PreparedStatement sql = conn.prepareStatement("SELECT * FROM heh_market_player WHERE player_uuid=? ;");
            sql.setString(1, player.getUniqueId().toString());
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(result.getString("mailbox"));
                return ((List<ItemStack>) yaml.get("mailbox")).toArray(new ItemStack[0]);
            }
            return new ItemStack[54];
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setMailbox(Player player, ItemStack[] inventory) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("mailbox", inventory);
        try {
            PreparedStatement stmt = conn.prepareStatement("REPLACE INTO heh_market_player (player_uuid,mailbox) VALUES (?, ?);");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, yaml.saveToString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}

package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.signshop.ShopItem;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@DataTable("signshop")
public class SignShop {
    @DataColumn("id")
    @PrimaryKey
    public UUID owner;
    public String yaml = "";
    private YamlConfiguration config = null;

    @DataColumn("yaml")
    public String getYaml() {
        if (config == null) {
            return Base64.getEncoder().encodeToString(this.yaml.getBytes());
        }
        return Base64.getEncoder().encodeToString(this.config.saveToString().getBytes());
    }

    public void setYaml(String yaml) {
        this.yaml = new String(Base64.getDecoder().decode(yaml));
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    public void setItems(List<ShopItem> list, ShopMode mode) {
        saveItems(mode.name(), list);
    }

    public List<ShopItem> getItems(ShopMode mode) {
        return loadItems(mode.name());
    }

    public List<ShopItem> loadItems(String path) {
        if (config == null) {
            load();
        }
        ArrayList<ShopItem> list = new ArrayList<>();
        if (config.isConfigurationSection(path)) {
            ConfigurationSection section = config.getConfigurationSection(path);
            for (String k : section.getKeys(false)) {
                list.add(new ShopItem(section.getConfigurationSection(k)));
            }
        }
        return list;
    }

    public void saveItems(String path, List<ShopItem> list) {
        if (config == null) {
            load();
        }
        config.set(path, null);
        ConfigurationSection section = config.createSection(path);
        for (int i = 0; i < list.size(); i++) {
            ShopItem item = list.get(i);
            if (item.amount > 0 && item.getItemStack(1).getType() != Material.AIR) {
                list.get(i).save(section.createSection(String.valueOf(i)));
            }
        }
    }

    public void yamlToNBT() {
        if (config == null) {
            load();
        }
        setItems(getItems(ShopMode.BUY), ShopMode.BUY);
        setItems(getItems(ShopMode.SELL), ShopMode.SELL);
    }

    private void load() {
        config = new YamlConfiguration();
        try {
            config.loadFromString(this.yaml);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}

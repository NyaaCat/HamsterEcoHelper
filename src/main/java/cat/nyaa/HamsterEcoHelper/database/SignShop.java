package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.signshop.ShopItem;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Table(name = "signshop")
public class SignShop {
    @Column(name = "id")
    @Id
    public UUID owner;
    public String yaml = "";

    @Column(name = "yaml")
    public String getYaml() {
        return Base64.getEncoder().encodeToString(this.yaml.getBytes());
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
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(this.yaml);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<ShopItem> list = new ArrayList<>();
        if (configuration.isConfigurationSection(path)) {
            ConfigurationSection section = configuration.getConfigurationSection(path);
            for (String k : section.getKeys(false)) {
                list.add(new ShopItem(section.getConfigurationSection(k)));
            }
        }
        return list;
    }

    public void saveItems(String path, List<ShopItem> list) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(this.yaml);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        configuration.set(path, null);
        ConfigurationSection section = configuration.createSection(path);
        for (int i = 0; i < list.size(); i++) {
            ShopItem item = list.get(i);
            if (item.amount > 0 && item.getItemStack(1).getType() != Material.AIR) {
                list.get(i).save(section.createSection(String.valueOf(i)));
            }
        }
        this.yaml = configuration.saveToString();
    }
}

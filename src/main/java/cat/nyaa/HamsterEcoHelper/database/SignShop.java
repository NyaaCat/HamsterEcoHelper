package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.signshop.ShopItem;
import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
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

@Table("signshop")
public class SignShop {
    @Column(name = "id", primary = true)
    public UUID owner;

    public String yaml = "";
    private YamlConfiguration config = null;

    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(String owner) {
        this.owner = UUID.fromString(owner);
    }

    @Column(name = "yaml", columnDefinition = "MEDIUMTEXT")
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
                try{
                    list.add(new ShopItem(section.getConfigurationSection(k)));
                }catch (Exception e){
                    logCorruptedItem();
                }
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
                ShopItem shopItem = list.get(i);
                if (shopItem.itemStack == null){
                    logCorruptedItem();
                    continue;
                }
                shopItem.save(section.createSection(String.valueOf(i)));
            }
        }
    }

    private void logCorruptedItem() {
        OfflinePlayer player = getPlayer();
        String playerName = "";
        if (player == null){
            playerName = "null";
        }else {
            playerName = player.getName();
        }
        Bukkit.getLogger().severe("null item found saving shop for " + playerName + ", uid " + getOwner() +".");
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

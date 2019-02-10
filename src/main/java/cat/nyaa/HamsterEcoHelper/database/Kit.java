package cat.nyaa.HamsterEcoHelper.database;


import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "kititems")
public class Kit {
    @Column(name = "id")
    @Id
    public String id;
    public String yaml = "";
    private YamlConfiguration config = null;

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

    public List<ItemStack> getItems() {
        if (config == null) {
            load();
        }
        String path = "items";
        List<ItemStack> list = new ArrayList<>();
        if (config.isString(path)) {
            list.addAll(ItemStackUtils.itemsFromBase64(config.getString(path)));
        }
        return list;
    }

    public void setItems(List<ItemStack> list) {
        if (config == null) {
            load();
        }
        String path = "items";
        config.set(path, ItemStackUtils.itemsToBase64(list));
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

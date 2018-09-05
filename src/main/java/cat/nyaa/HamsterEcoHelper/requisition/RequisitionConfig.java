package cat.nyaa.HamsterEcoHelper.requisition;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RequisitionConfig extends FileConfigure {
    private final HamsterEcoHelper plugin;
    public List<RequisitionSpecification> itemsForReq = new ArrayList<>();

    public RequisitionConfig(HamsterEcoHelper pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "requisition-items.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        itemsForReq.clear();
        ISerializable.deserialize(config, this);
        for (String idx : config.getKeys(false)) {
            RequisitionSpecification tmp = new RequisitionSpecification();
            ConfigurationSection c = config.getConfigurationSection(idx);
            tmp.deserialize(c);
            if (Bukkit.getVersion().contains("MC: 1.13")) {
                if (config.getString("nbt_version", Bukkit.getVersion()).contains("MC: 1.12")) {
                    tmp.itemTemplate = ItemStackUtils.itemFromBase64(c.getString("nbt_backup"));
                }
            }
            itemsForReq.add(tmp);
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        for (String k : config.getKeys(false)) {
            config.set(k, null);
        }
        ISerializable.serialize(config, this);
        for (int i = 0; i < itemsForReq.size(); i++) {
            RequisitionSpecification item = itemsForReq.get(i);
            ConfigurationSection c = config.createSection(Integer.toString(i));
            item.serialize(c);
            if (Bukkit.getVersion().contains("MC: 1.12")) {
                c.set("nbt_backup", ItemStackUtils.itemToBase64(item.itemTemplate));
            }
            c.set("nbt_version", Bukkit.getVersion());
        }
    }
}


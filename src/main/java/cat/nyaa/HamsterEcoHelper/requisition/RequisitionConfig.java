package cat.nyaa.HamsterEcoHelper.requisition;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
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
            tmp.deserialize(config.getConfigurationSection(idx));
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
            itemsForReq.get(i).serialize(config.createSection(Integer.toString(i)));
        }
    }
}


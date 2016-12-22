package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AuctionConfig extends FileConfigure {
    private final HamsterEcoHelper plugin;
    public List<AuctionItemTemplate> itemsForAuction = new ArrayList<>();

    public AuctionConfig(HamsterEcoHelper pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "auction-items.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        itemsForAuction.clear();
        ISerializable.deserialize(config, this);
        for (String idx : config.getKeys(false)) {
            AuctionItemTemplate tmp = new AuctionItemTemplate();
            tmp.deserialize(config.getConfigurationSection(idx));
            itemsForAuction.add(tmp);
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        for (String k : config.getKeys(false)) {
            config.set(k, null);
        }
        ISerializable.serialize(config, this);
        for (int i = 0; i < itemsForAuction.size(); i++) {
            itemsForAuction.get(i).serialize(config.createSection(Integer.toString(i)));
        }
    }
}

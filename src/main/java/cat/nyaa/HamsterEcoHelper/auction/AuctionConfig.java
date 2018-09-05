package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
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
            ConfigurationSection c = config.getConfigurationSection(idx);
            tmp.deserialize(c);
            if (Bukkit.getVersion().contains("MC: 1.13")) {
                if (config.getString("nbt_version", Bukkit.getVersion()).contains("MC: 1.12")) {
                    tmp.templateItemStack = ItemStackUtils.itemFromBase64(c.getString("nbt_backup"));
                }
            }
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
            AuctionItemTemplate item = itemsForAuction.get(i);
            ConfigurationSection c = config.createSection(Integer.toString(i));
            item.serialize(c);
            if (Bukkit.getVersion().contains("MC: 1.12")) {
                config.getConfigurationSection(Integer.toString(i)).set("nbt_backup", ItemStackUtils.itemToBase64(item.templateItemStack));
            }
            c.set("nbt_version", Bukkit.getVersion());
        }
    }
}

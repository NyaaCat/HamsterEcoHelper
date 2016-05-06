package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.data.AuctionItemTemplate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private final HamsterEcoHelper plugin;

    public String language = "en_US";
    public List<AuctionItemTemplate> itemsForAuction;
    public int auctionIntervalTicks;
    public int bidTimeoutTicks;
    private YamlConfiguration items;

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    public void loadFromPlugin() {
        plugin.saveDefaultConfig();
        language = plugin.getConfig().getString("language", "en_US");
        auctionIntervalTicks = plugin.getConfig().getInt("auctionIntervalTicks", 60 * 60 * 20);
        bidTimeoutTicks = plugin.getConfig().getInt("bidTimeoutTicks", 30 * 20);

        itemsForAuction = new ArrayList<>();
        items = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"items.yml"));
        ConfigurationSection tmp = items.getConfigurationSection("itemsForAuction");
        if(tmp!=null) {
            for (String idx : tmp.getKeys(false)) {
                itemsForAuction.add(AuctionItemTemplate.fromConfig(items.getConfigurationSection(idx)));
            }
        }
    }

    public void saveToPlugin() {
        ConfigurationSection tmp = items.createSection("itemsForAuction");
        for (int i = 0; i < itemsForAuction.size(); i++) {
            itemsForAuction.get(i).dumpTo(tmp.createSection(Integer.toString(i)));
        }
        try {
            items.save(new File(plugin.getDataFolder(),"items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

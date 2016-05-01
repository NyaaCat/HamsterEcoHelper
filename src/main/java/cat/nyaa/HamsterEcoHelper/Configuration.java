package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.data.AuctionItemTemplate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private final HamsterEcoHelper plugin;

    public String language = "en_US";
    public List<AuctionItemTemplate> itemsForAuction;
    public int auctionIntervalTicks;
    public int bidTimeoutTicks;

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    public void loadFromPlugin() {
        plugin.saveDefaultConfig();
        ConfigurationSection c = plugin.getConfig();
        language = c.getString("language", "en_US");
        auctionIntervalTicks = c.getInt("auctionIntervalTicks", 60 * 60 * 20);
        bidTimeoutTicks = c.getInt("bidTimeoutTicks", 30 * 20);

        itemsForAuction = new ArrayList<>();
        ConfigurationSection tmp = c.getConfigurationSection("itemsForAuction");
        for (String idx : tmp.getKeys(false)) {
            itemsForAuction.add(AuctionItemTemplate.fromConfig(c.getConfigurationSection(idx)));
        }
    }

    public void saveToPlugin() {
        ConfigurationSection c = plugin.getConfig();
        c.set("language", language);
        c.set("auctionIntervalTicks", auctionIntervalTicks);
        c.set("bidTimeoutTicks", bidTimeoutTicks);

        ConfigurationSection tmp = c.createSection("itemsForAuction");
        for (int i = 0; i < itemsForAuction.size(); i++) {
            itemsForAuction.get(i).dumpTo(tmp.createSection(Integer.toString(i)));
        }

        plugin.saveConfig();
    }
}

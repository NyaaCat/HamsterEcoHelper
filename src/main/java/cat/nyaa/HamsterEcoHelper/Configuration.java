package cat.nyaa.HamsterEcoHelper;

import org.bukkit.configuration.ConfigurationSection;

public class Configuration {
    private final HamsterEcoHelper plugin;
    String language = "en_US";

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    public void loadFromPlugin() {
        plugin.saveDefaultConfig();
        ConfigurationSection c = plugin.getConfig();
        language = c.getString("language", "en_US");
    }

    public void saveToPlugin() {
        ConfigurationSection c = plugin.getConfig();
        c.set("language", language);
        plugin.saveConfig();
    }
}

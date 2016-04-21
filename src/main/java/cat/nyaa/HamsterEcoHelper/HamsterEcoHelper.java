package cat.nyaa.HamsterEcoHelper;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper instance;
    public Logger logger;
    public Configuration config;

    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        saveDefaultConfig();
        config = new Configuration(this);
        config.loadFromPlugin();
        I18n.load(this, config.language);
    }

    @Override
    public void onEnable() {
        getCommand("hemsterecohelper").setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        config.saveToPlugin();
    }
}





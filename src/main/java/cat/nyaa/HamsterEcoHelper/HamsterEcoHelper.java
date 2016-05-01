package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper instance;
    public Logger logger;
    public Configuration config;
    public AuctionManager auctionManager;
    public EconomyHelper eco;

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
        auctionManager = new AuctionManager(this);
        eco = new EconomyHelper(this);
    }

    @Override
    public void onDisable() {
        auctionManager.halt();
        auctionManager.cancel();
        config.saveToPlugin();
    }
}





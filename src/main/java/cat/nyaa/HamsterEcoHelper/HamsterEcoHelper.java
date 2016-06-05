package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionManager;
import cat.nyaa.HamsterEcoHelper.utils.Database;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
import cat.nyaa.HamsterEcoHelper.utils.Mute;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper instance;
    public Logger logger;
    public Configuration config;
    public AuctionManager auctionManager;
    public RequisitionManager reqManager;
    public EconomyUtil eco;
    public Database database;
    public Events eventHandler;
    public CommandHandler commandHandler;

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
        try {
            commandHandler = new CommandHandler(this);
            getCommand("hamsterecohelper").setExecutor(commandHandler);
            database = new Database(this);
            eco = new EconomyUtil(this);
            auctionManager = new AuctionManager(this);
            reqManager = new RequisitionManager(this);
            MarketManager.init(this);
            eventHandler = new Events(this);
            enableComplete = true;
            Mute.init();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(I18n.get("internal.error.enable_fail"));
            getPluginLoader().disablePlugin(this);
        }
    }

    private boolean enableComplete = false;
    @Override
    public void onDisable() {
        if (!enableComplete) return;
        auctionManager.halt();
        auctionManager.cancel();
        reqManager.halt();
        reqManager.cancel();
        config.saveToPlugin();
        enableComplete = false;
    }

    public void reset() {
        auctionManager.halt();
        auctionManager.cancel();
        reqManager.halt();
        reqManager.cancel();
        I18n.reset();
        reloadConfig();
        config.loadFromPlugin();
        I18n.load(this, config.language);
        auctionManager = new AuctionManager(this);
        reqManager = new RequisitionManager(this);
    }

    @Override
    public void installDDL() {
        super.installDDL();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return Database.getDatabaseClasses();
    }
}





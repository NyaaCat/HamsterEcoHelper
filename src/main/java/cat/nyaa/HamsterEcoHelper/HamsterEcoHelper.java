package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import cat.nyaa.HamsterEcoHelper.market.Market;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionManager;
import cat.nyaa.HamsterEcoHelper.utils.Database;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
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
            getCommand("hemsterecohelper").setExecutor(new CommandHandler(this));
            database = new Database(this);
            eco = new EconomyUtil(this);
            auctionManager = new AuctionManager(this);
            reqManager = new RequisitionManager(this);
            Market.init(this);
            eventHandler = new Events(this);
            enableComplete = true;
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

    @Override
    public void installDDL() {
        super.installDDL();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return Database.getDatabaseClasses();
    }
}





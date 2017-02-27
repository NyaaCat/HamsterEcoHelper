package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import cat.nyaa.HamsterEcoHelper.balance.BalanceAPI;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionManager;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopManager;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
import cat.nyaa.HamsterEcoHelper.utils.database.Database;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopListener;
import org.bukkit.plugin.java.JavaPlugin;

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
    public MarketManager marketManager;
    public I18n i18n;
    private boolean enableComplete = false;
    public BalanceAPI balanceAPI;
    public SignShopManager signShopManager;
    public SignShopListener signShopListener;

    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        saveDefaultConfig();
        config = new Configuration(this);
        config.loadFromPlugin();
        this.i18n = new I18n(this, this.config.language);
    }

    @Override
    public void onEnable() {
        try {
            this.i18n.load();
            commandHandler = new CommandHandler(this, this.i18n);
            getCommand("hamsterecohelper").setExecutor(commandHandler);
            database = new Database(this);
            eco = new EconomyUtil(this);
            auctionManager = new AuctionManager(this);
            reqManager = new RequisitionManager(this);
            marketManager = new MarketManager(this);
            eventHandler = new Events(this);
            balanceAPI = new BalanceAPI(this);
            signShopManager = new SignShopManager(this);
            signShopListener = new SignShopListener(this);
            enableComplete = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(I18n._("log.error.enable_fail"));
            getPluginLoader().disablePlugin(this);
        }
    }

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
        signShopManager.closeAllGUI();
        i18n.reset();
        reloadConfig();
        config.loadFromPlugin();
        i18n.load();
        auctionManager = new AuctionManager(this);
        reqManager = new RequisitionManager(this);
        signShopManager = new SignShopManager(this);
    }
}





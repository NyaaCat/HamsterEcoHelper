package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.ads.AdsManager;
import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import cat.nyaa.HamsterEcoHelper.balance.SystemBalance;
import cat.nyaa.HamsterEcoHelper.database.Database;
import cat.nyaa.HamsterEcoHelper.kit.KitListener;
import cat.nyaa.HamsterEcoHelper.kit.KitManager;
import cat.nyaa.HamsterEcoHelper.market.MarketCache;
import cat.nyaa.HamsterEcoHelper.market.MarketListener;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionManager;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopListener;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopManager;
import cat.nyaa.HamsterEcoHelper.transaction.TransactionManager;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
import cat.nyaa.nyaacore.component.ISystemBalance;
import cat.nyaa.nyaacore.component.NyaaComponent;
import com.earth2me.essentials.Essentials;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
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
    public TransactionManager transactionManager;
    public I18n i18n;
    public SystemBalance systemBalance;
    public SignShopManager signShopManager;
    public SignShopListener signShopListener;
    public MarketListener marketListener;
    public AdsManager adsManager;
    public Essentials ess = null;
    public KitManager kitManager;
    public KitListener kitListener;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        config = new Configuration(this);
        config.load();
        i18n = new I18n(this, this.config.language);
        commandHandler = new CommandHandler(this, this.i18n);
        getCommand("hamsterecohelper").setExecutor(commandHandler);
        getCommand("hamsterecohelper").setTabCompleter(commandHandler);
        getCommand("hm").setExecutor(new HMCommand(this, i18n));
        getCommand("hsellto").setExecutor(new HSellToCommand(this, i18n));
        getCommand("hpay").setExecutor(new HPayCommand(this, i18n));
        getCommand("hauc").setExecutor(new HAucCommand(this, i18n));
        getCommand("hbid").setExecutor(new HBidCommand(this, i18n));
        getCommand("hreq").setExecutor(new HReqCommand(this, i18n));
        getCommand("hsell").setExecutor(new HSellCommand(this, i18n));
        try {
            database = new Database(this);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        eco = new EconomyUtil(this);
        systemBalance = new SystemBalance(this);
        NyaaComponent.register(ISystemBalance.class, systemBalance);
        auctionManager = new AuctionManager(this);
        reqManager = new RequisitionManager(this);
        marketManager = new MarketManager(this);
        transactionManager = new TransactionManager(this);
        marketListener = new MarketListener(this);
        signShopManager = new SignShopManager(this);
        signShopListener = new SignShopListener(this);
        adsManager = new AdsManager(this);
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        }
        eventHandler = new Events(this);
        kitManager = new KitManager(this);
        kitListener = new KitListener(this);
    }

    @Override
    public void onDisable() {
        auctionManager.halt();
        auctionManager.cancel();
        reqManager.halt();
        reqManager.cancel();
        systemBalance.cancel();
        config.save();
        try {
            database.database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ess = null;
    }

    public void reload() {
        auctionManager.halt();
        auctionManager.cancel();
        reqManager.halt();
        reqManager.cancel();
        signShopManager.closeAllGUI();
        marketManager.closeAllGUI();
        marketManager.cancel();
        adsManager.cancel();
        systemBalance.cancel();
        config.load();
        i18n.load();
        systemBalance = new SystemBalance(this);
        NyaaComponent.register(ISystemBalance.class, systemBalance);
        auctionManager = new AuctionManager(this);
        reqManager = new RequisitionManager(this);
        signShopManager = new SignShopManager(this);
        MarketCache.needUpdateItemCount = true;
        MarketCache.playerItemCount.invalidateAll();
        MarketCache.playerName.invalidateAll();
        marketManager = new MarketManager(this);
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        }
        adsManager = new AdsManager(this);
        kitManager = new KitManager(this);
    }
}





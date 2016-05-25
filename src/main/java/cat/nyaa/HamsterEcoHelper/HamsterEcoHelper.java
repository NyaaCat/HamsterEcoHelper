package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionManager;
import cat.nyaa.HamsterEcoHelper.market.Market;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionManager;
import cat.nyaa.HamsterEcoHelper.utils.EconomyHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper instance;
    public Logger logger;
    public Configuration config;
    public AuctionManager auctionManager;
    public RequisitionManager reqManager;
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
        eco = new EconomyHelper(this);
        auctionManager = new AuctionManager(this);
        reqManager = new RequisitionManager(this);
        Market.init(this);
        getServer().getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        auctionManager.halt();
        auctionManager.cancel();
        reqManager.halt();
        reqManager.cancel();
        config.saveToPlugin();
    }
}





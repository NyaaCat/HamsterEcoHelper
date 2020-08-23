package cat.nyaa.heh;

import cat.nyaa.heh.business.auction.Auction;
import cat.nyaa.heh.command.*;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.events.Events;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper plugin;
    public Configuration config;
    I18n i18n;
    AdminCommands adminCommands;
    AuctionCommand auctionCommand;
    BidCommand bidCommand;
    BusinessCommands businessCommands;
    MainCommand mainCommand;
    RequisitionCommand requisitionCommand;
    SellCommand sellCommand;
    ShopCommands shopCommands;
    Events events ;

    @Override
    public void onEnable() {
        plugin = this;
        reload();
        registerCommands();
        events = new Events(this);
        Bukkit.getPluginManager().registerEvents(events, this);
    }

    private void registerCommands() {
        mainCommand = new MainCommand(this, i18n);
        Bukkit.getPluginCommand("hamsterecohelper").setExecutor(mainCommand);
    }

    @Override
    public void onDisable() {
        plugin = null;
        Auction.abort();
        DatabaseManager.getInstance().close();
        UiManager.getInstance().getMarketUis().forEach(marketGUI -> marketGUI.close());
    }

    public void reload() {
        config = new Configuration();
        config.load();
        i18n = new I18n(plugin, config.language);
        i18n.load();
        DatabaseManager.getInstance();
        MarketConnection.getInstance();
        SignShopConnection.getInstance();
        TransactionController.getInstance();
        SystemAccountUtils.init();
        EcoUtils.getInstance();
        ButtonRegister.getInstance();
    }
}





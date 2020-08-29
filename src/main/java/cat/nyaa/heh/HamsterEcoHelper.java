package cat.nyaa.heh;

import cat.nyaa.heh.business.auction.Auction;
import cat.nyaa.heh.business.signshop.ItemFrameShop;
import cat.nyaa.heh.business.signshop.SignShopManager;
import cat.nyaa.heh.command.*;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.events.listeners.SignEvents;
import cat.nyaa.heh.events.listeners.UiEvents;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper plugin;
    public Configuration config;
    I18n i18n;

    MainCommand mainCommand;
    UiEvents uiEvents;
    SignEvents signEvents;

    Auction auction;
    DatabaseManager databaseManager;
    UiManager uiManager;

    @Override
    public void onEnable() {
        plugin = this;
        reload();
        registerCommands();
        uiEvents = new UiEvents(this);
        signEvents = new SignEvents();
        Bukkit.getPluginManager().registerEvents(uiEvents, this);
        Bukkit.getPluginManager().registerEvents(signEvents, this);
    }

    private void registerCommands() {
        mainCommand = new MainCommand(this, i18n);
        Bukkit.getPluginCommand("hamsterecohelper").setExecutor(mainCommand);
    }

    @Override
    public void onDisable() {
        if (auction != null){
            auction.abort();
        }
        databaseManager.close();
        uiManager.getMarketUis().forEach(marketGUI -> marketGUI.close());
        plugin = null;
    }

    public void reload() {
        config = new Configuration();
        config.load();
        i18n = new I18n(plugin, config.language);
        i18n.load();
        databaseManager = DatabaseManager.getInstance();
        uiManager = UiManager.getInstance();
        MarketConnection.getInstance();
        SignShopConnection.getInstance();
        TransactionController.getInstance();
        SystemAccountUtils.init();
        EcoUtils.getInstance();
        ButtonRegister.getInstance();
        SignShopManager ssm = SignShopManager.getInstance();
        ssm.load();
        new BukkitRunnable(){
            @Override
            public void run() {
                ssm.updateSigns();
            }
        }.runTaskAsynchronously(this);
        ItemFrameShop.reloadFrames();
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }
}





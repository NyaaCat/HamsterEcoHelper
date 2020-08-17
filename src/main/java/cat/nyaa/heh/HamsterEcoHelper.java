package cat.nyaa.heh;

import cat.nyaa.heh.auction.Auction;
import cat.nyaa.heh.command.MainCommand;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.transaction.TransactionController;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper plugin;
    public Configuration config;
    I18n i18n;
    MainCommand mainCommand;
    Events events ;

    @Override
    public void onEnable() {
        plugin = this;
        reload();
        mainCommand = new MainCommand(this, i18n);
        Bukkit.getPluginCommand("hamsterecohelper").setExecutor(mainCommand);
        events = new Events(this);
        Bukkit.getPluginManager().registerEvents(events, this);
    }

    @Override
    public void onDisable() {
        plugin = null;
        Auction.abort();
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





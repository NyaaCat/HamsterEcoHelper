package cat.nyaa.heh;

import cat.nyaa.heh.api.HamsterEcoHelperAPI;
import cat.nyaa.heh.business.auction.Auction;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.signshop.ItemFrameShop;
import cat.nyaa.heh.business.signshop.SignShopManager;
import cat.nyaa.heh.business.signshop.SignShopSell;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.command.*;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.events.listeners.SignEvents;
import cat.nyaa.heh.events.listeners.UiEvents;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class HamsterEcoHelper extends JavaPlugin implements HamsterEcoHelperAPI {
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
        onReload();
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

    public void onReload() {
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
        ButtonRegister.getInstance().load();
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

    @Override
    public boolean withdrawPlayer(OfflinePlayer player, double amount, ShopItemType type, String taxReason) {
        return TransactionController.getInstance().withdrawWithTax(player, amount, type, taxReason);
    }

    @Override
    public double getSystemBalance() {
        return SystemAccountUtils.getSystemBalance();
    }

    @Override
    public boolean depositToSystem(String reason, double amount) {
        try {
            Tax tax = TransactionController.getInstance().newTax(SystemAccountUtils.getSystemUuid(), 0, amount, System.currentTimeMillis(), reason);
            TransactionController.getInstance().retrieveTax(tax);
            return true;
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "error while depositing system", e);
            return false;
        }
    }

    @Override
    public boolean depositToSystem(OfflinePlayer from, String reason, double amount) {
        boolean withdraw = SystemAccountUtils.withdraw(from, amount);
        if (!withdraw){
            return false;
        }
        try {
            Tax tax = TransactionController.getInstance().newTax(from.getUniqueId(), 0, amount, System.currentTimeMillis(), reason);
            TransactionController.getInstance().retrieveTax(tax);
            return withdraw;
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "error while depositing system", e);
            return false;
        }
    }



    @Override
    public boolean chargeFee(OfflinePlayer from, String reason, double amount) {
        boolean success = SystemAccountUtils.withdraw(from, amount);
        if (success){
            Tax tax = TransactionController.getInstance().newTax(from.getUniqueId(), 0, amount, System.currentTimeMillis(), reason);
            TransactionController.getInstance().retrieveTax(tax);
        }
        return success;
    }

    @Override
    public boolean withdrawFromSystem(String reason, double amount) {
        try {
            Tax tax = TransactionController.getInstance().newTax(SystemAccountUtils.getSystemUuid(), 0, -amount, System.currentTimeMillis(), reason);
            TransactionController.getInstance().retrieveTax(tax);
            return true;
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "error while withdrawing system", e);
            return false;
        }
    }

    @Override
    public boolean withdrawFromSystem(OfflinePlayer from, String reason, double amount) {
        boolean deposit = SystemAccountUtils.deposit(from, amount);
        if (!deposit){
            return false;
        }
        try {
            Tax tax = TransactionController.getInstance().newTax(from.getUniqueId(), 0, -amount, System.currentTimeMillis(), reason);
            TransactionController.getInstance().retrieveTax(tax);
            return deposit;
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "error while withdrawing system", e);
            return false;
        }
    }

    @Override
    public Inventory openShopfor(Player opener, OfflinePlayer shopOwner) {
        SignShopSell signShopSell = new SignShopSell(shopOwner.getUniqueId());
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(signShopSell);
        Inventory inventory = signShopGUI.getInventory();
        signShopGUI.open(opener);
        return inventory;
    }

    public HamsterEcoHelperAPI getImpl(){
        return this;
    }
}





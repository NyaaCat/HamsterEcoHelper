package cat.nyaa.heh.updater;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import heh7_2.database.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class UpdaterMain extends JavaPlugin {
    I18n i18n;

    @Override
    public void onEnable() {
        super.onEnable();
        i18n = new I18n(this, "en_US");
        getCommand("hupdater").setExecutor(new UpdaterCommands(this, i18n));
    }

    public class UpdaterCommands extends CommandReceiver{
        /**
         * @param plugin for logging purpose only
         * @param _i18n
         */
        public UpdaterCommands(Plugin plugin, ILocalizer _i18n) {
            super(plugin, _i18n);
        }

        @SubCommand(value = "update" , permission = "heh.update")
        public void onUpdate(CommandSender sender, Arguments arguments) throws SQLException, ClassNotFoundException {
            File dataFolder = UpdaterMain.this.getDataFolder().getParentFile();
            File dbFile = new File(dataFolder, "./HamsterEcoHelper/HamsterEcoHelper.db");
            Database database = new Database(dbFile);
            List<SignShop> signShops = database.getSignShops();
            List<MarketItem> marketItems = database.getMarketItems(0, Integer.MAX_VALUE, null);
            List<Sign> shopSigns = database.getShopSigns();
            List<LottoStorageLocation> lottoStorageLocations = database.getLottoStorageLocations();
            List<Invoice> invoices = database.getInvoices();
            List<ShopStorageLocation> chestLocations = database.getChestLocations();

        }

        @Override
        public String getHelpPrefix() {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {


    }
}

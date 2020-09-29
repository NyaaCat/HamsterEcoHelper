package cat.nyaa.heh.updater;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import heh7_2.database.*;
import heh8_0.db.DatabaseManager;
import heh8_0.db.ShopItemType;
import heh8_0.db.model.*;
import heh8_0.db.utils.UidUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        public void UpdaterCommands(){

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


            DatabaseManager dbManagerV8 = DatabaseManager.getInstance();
            File dbFileV8 = new File(dataFolder, "./hehV8/HamsterEcoHelper.db");
            dbFileV8.getParentFile().mkdirs();
            UidUtils uidUtils = UidUtils.create();

            Logger logger = Bukkit.getLogger();
            logger.log(Level.INFO, "update start");
            logger.log(Level.INFO, "update sign shop");
            signShops.forEach(signShop -> Arrays.stream(ShopMode.values()).forEach(shopMode -> {
                List<ShopItem> items = signShop.getItems(shopMode);
                if (items.size() > 0){
                    items.stream().map(item -> {
                        ShopItemType type = getShopItemType(shopMode);
                        return createShopItemDbModel(uidUtils, signShop.owner, item.getItemStack(1), item.amount, item.unitPrice, type);
                    }).forEach(dbManagerV8::addShopItem);
                }
            }));
            logger.log(Level.INFO, "update market");
            marketItems.forEach(marketItem -> {
                ShopItemDbModel shopItemDbModel = createShopItemDbModel(uidUtils, marketItem.playerId, marketItem.getItemStack(), marketItem.amount, marketItem.unitPrice, ShopItemType.MARKET);
                dbManagerV8.addShopItem(shopItemDbModel);
            });
            logger.log(Level.INFO, "update sign location");
            shopSigns.forEach(sign -> {
                LocationType locationType = getLocationType(sign.shopMode);
                DataModel data = getData(sign.shopMode, sign.owner, sign.lotto_price);
                LocationDbModel locationModel = createLocationModel(uidUtils, sign.world, sign.x, sign.y, sign.z, locationType, null, sign.owner, data);
                dbManagerV8.insertLocation(locationModel);
            });
            logger.log(Level.INFO, "update lotto storage");
            lottoStorageLocations.forEach(lottoStorageLocation -> {
                LocationType locationType = LocationType.CHEST_LOTTO;
                LocationDbModel locationModel = createLocationModel(uidUtils, lottoStorageLocation.world, lottoStorageLocation.x, lottoStorageLocation.y, lottoStorageLocation.z,
                        locationType, null, lottoStorageLocation.owner, null);
                dbManagerV8.insertLocation(locationModel);
            });
            logger.log(Level.INFO, "update invoice");
            invoices.forEach(invoice -> {
                ShopItemDbModel shopItemDbModel = createShopItemDbModel(
                        uidUtils,
                        invoice.getSellerId(),
                        invoice.getItemStack(),
                        (int) invoice.getAmount(),
                        invoice.getTotalPrice()/invoice.getAmount(),
                        ShopItemType.DIRECT);
                dbManagerV8.addShopItem(shopItemDbModel);

                InvoiceDbModel model = createInvoiceModel(invoice.getSellerId(), invoice.getBuyerId(), invoice.getDraweeId(), shopItemDbModel, invoice.getCreatedTime());
                dbManagerV8.insertInvoice(model);
            });
            logger.log(Level.INFO, "update chest location");
            chestLocations.forEach(chestLocation -> {
                LocationDbModel locationModel = createLocationModel(uidUtils, chestLocation.world, chestLocation.x, chestLocation.y, chestLocation.z,
                        LocationType.CHEST_BUY, null, chestLocation.owner, null);
                dbManagerV8.insertLocation(locationModel);
            });
        }

        private InvoiceDbModel createInvoiceModel(UUID sellerId, UUID buyerId, UUID payer, ShopItemDbModel item, long time) {
            InvoiceDbModel model = new InvoiceDbModel();
            model.setCustomer(buyerId);
            model.setPayer(payer);
            model.setFrom(sellerId);
            model.setUid(item.getUid());
            model.setTime(time);
            return model;
        }

        private DataModel getData(ShopMode shopMode, UUID owner, Double lottoPrice) {
            LocationType type = LocationType.SIGN_SHOP_SELL;
            DataModel model = null;
            String ownerStr = Bukkit.getOfflinePlayer(owner).getName();
            switch (shopMode){
                case BUY:
                    ArrayList<String> lores = new ArrayList<>();
                    lores.add("[BUY]");
                    lores.add(ownerStr);
                    lores.add("");
                    lores.add("");
                    model = new SignShopData(lores);
                    break;
                case SELL:
                    ArrayList<String> lores1 = new ArrayList<>();
                    lores1.add("[SELL]");
                    lores1.add(ownerStr);
                    lores1.add("");
                    lores1.add("");
                    model = new SignShopData(lores1);
                    break;
                case LOTTO:
                    ArrayList<String> lores2 = new ArrayList<>();
                    lores2.add("[LOTTO]");
                    lores2.add(ownerStr);
                    lores2.add(String.format("%.2f", lottoPrice));
                    lores2.add("");
                    model = new SignShopData(lores2);
                    type = (LocationType.SIGN_SHOP_LOTTO);
                    break;
            }
            return model;
        }

        private LocationDbModel createLocationModel(UidUtils uidUtils, String world, Long x, Long y, Long z, LocationType locationType, UUID entityId, UUID owner, DataModel data) {
            LocationDbModel dbModel = new LocationDbModel();
            dbModel.setUid(uidUtils.getNextUid());
            dbModel.setWorld(world);
            dbModel.setX (x);
            dbModel.setY (y);
            dbModel.setZ (z);
            dbModel.setLocationType(locationType);
            dbModel.setEntityUUID(entityId);
            dbModel.setOwner(owner);
            dbModel.setData(data);
            return dbModel;
        }

        private LocationType getLocationType(ShopMode shopMode) {
            LocationType type = LocationType.SIGN_SHOP_SELL;
            switch (shopMode){
                case BUY:
                    type = (LocationType.SIGN_SHOP_BUY);
                    break;
                case SELL:
                    type = (LocationType.SIGN_SHOP_SELL);
                    break;
                case LOTTO:
                    type = (LocationType.SIGN_SHOP_LOTTO);
                    break;
            }
            return type;
        }

        private ShopItemType getShopItemType(ShopMode shopMode) {
            ShopItemType type = ShopItemType.SIGN_SHOP_SELL;
            switch (shopMode){
                case BUY:
                    type = (ShopItemType.SIGN_SHOP_BUY);
                    break;
                case SELL:
                    type = (ShopItemType.SIGN_SHOP_SELL);
                    break;
                case LOTTO:
                    type = (ShopItemType.LOTTO);
                    break;
            }
            return type;
        }

        private ShopItemDbModel createShopItemDbModel(UidUtils uidUtils, UUID owner, ItemStack itemStack, int amount, Double unitPrice, ShopItemType type) {
            ShopItemDbModel dbModel = new ShopItemDbModel();
            dbModel.setUid(uidUtils.getNextUid());
            dbModel.setOwner(owner);
            dbModel.setType(type);
            dbModel.setNbt(ItemStackUtils.itemToBase64(itemStack));
            dbModel.setPrice(unitPrice);
            dbModel.setAmount(amount);
            dbModel.setSold(0);
            dbModel.setAvailable(true);
            dbModel.setTime(System.currentTimeMillis());
            String ownerStr = Bukkit.getOfflinePlayer(owner).getName();
            dbModel.setMeta(itemStack.toString() + "owner:" + ownerStr);
            return dbModel;
        }

        @Override
        public String getHelpPrefix() {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {


    }
}

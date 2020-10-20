package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.LocationConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.Message;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SignShopBuy extends BaseSignShop{
    private final LocationType type = LocationType.SIGN_SHOP_BUY;

    public SignShopBuy(LocationDbModel model){
       super(model);
    }

    public SignShopBuy(UUID owner) {
        super(owner);
    }

    @Override
    public void loadItems() {
        items = SignShopConnection.getInstance().getBuyShopItems(owner);
    }

    @Override
    public String getTitle() {
        return I18n.format("shop.title.buy");
    }

    @Override
    public SignShopGUI newGUI() {
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(this);
        signShopGUI.refreshGUI();
        return signShopGUI;
    }

    private static Cache<Location, Inventory> inventoryCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    public void doBusiness(Player buyer, ShopItem item, int amount){

        double fee = HamsterEcoHelper.plugin.config.signShopFeeBase;
        LocationDbModel reqLocationModel = LocationConnection.getInstance().getReqLocationModel(owner);
        if(reqLocationModel == null || !(reqLocationModel.getBlock().getState() instanceof Chest)){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
            String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : offlinePlayer.getName();
            new Message(I18n.format("shop.sign.buy.no_chest")).send(offlinePlayer);
            new Message(I18n.format("shop.sign.sell.no_chest", name)).send(offlinePlayer);
            return;
        }
        Location location = new Location(Bukkit.getWorld(reqLocationModel.getWorld()), reqLocationModel.getX(), reqLocationModel.getY(), reqLocationModel.getZ());
        Inventory inventory;
        try {
            inventory = inventoryCache.get(location,() -> {
                Chest block = (Chest) reqLocationModel.getBlock().getState();
                return block.getBlockInventory();
            });
        } catch (ExecutionException e) {
            HamsterEcoHelper.plugin.getLogger().log(Level.SEVERE, "error loading inventory: ", e);
            return;
        }
        TransactionController.getInstance().makeTransaction(owner, buyer.getUniqueId(), item, amount, fee, inventory, buyer.getInventory(), TaxReason.REASON_SIGN_SHOP);
        item.setSold(0);
        ShopItemManager.getInstance().updateShopItem(item);
        updateUi();
    }


    @Override
    public LocationType getType() {
        return type;
    }
}

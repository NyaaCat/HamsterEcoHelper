package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.LocationConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

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
        SignShopConnection.getInstance().getBuyShopItems(owner);
    }

    @Override
    public String getTitle() {
        return I18n.format("shop.title.buy");
    }

    @Override
    public SignShopGUI newGUI() {
        SignShopGUI signShopGUI = new SignShopGUI(this);
        signShopGUI.refreshGUI();
        return signShopGUI;
    }

    @Override
    public void doBusiness(Player buyer, ShopItem item, int amount){
        //todo configure sign shop storage space.
        double fee = HamsterEcoHelper.plugin.config.signShopFeeBase;
        LocationDbModel reqLocationModel = LocationConnection.getInstance().getReqLocationModel(owner);
        Block block = reqLocationModel.getBlock();
        if(!(block.getState() instanceof Chest)){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
            String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : offlinePlayer.getName();
            new Message(I18n.format("shop.sign.buy.no_chest")).send(offlinePlayer);
            new Message(I18n.format("shop.sign.sell.no_chest", name)).send(offlinePlayer);
            return;
        }
        Inventory blockInventory = ((Chest) block.getState()).getBlockInventory();
        TransactionController.getInstance().makeTransaction(owner, buyer.getUniqueId(), item, amount, fee, blockInventory, buyer.getInventory());
        updateUi();
    }

    @Override
    public LocationType getType() {
        return type;
    }
}

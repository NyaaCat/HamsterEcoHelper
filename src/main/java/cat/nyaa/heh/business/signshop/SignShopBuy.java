package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.business.transaction.TransactionRequest;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.BaseUi;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import org.bukkit.entity.Player;

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
        items = SignShopConnection.getInstance().getBuyShopItems(owner);
    }

    @Override
    public String getTitle() {
        return I18n.format("shop.title.buy");
    }

    @Override
    public BaseUi<ShopItem> newGUI() {
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(this);
        signShopGUI.refreshGUI();
        return signShopGUI;
    }

    @Override
    public boolean doBusiness(Player buyer, ShopItem item, int amount){
        double fee = HamsterEcoHelper.plugin.config.signShopFeeBase;
        TransactionRequest build = new TransactionRequest.TransactionBuilder()
                .item(item)
                .seller(buyer.getUniqueId())
                .buyer(item.getOwner())
                .fee(fee)
                .amount(amount)
                .reason(TaxReason.REASON_SIGN_SHOP)
                .forceStorage(true).build();

        boolean success = TransactionController.getInstance().makeTransaction(build);
        item.setSold(0);
        ShopItemManager.getInstance().updateShopItem(item);
        updateUi();
        return success;
    }


    @Override
    public LocationType getType() {
        return type;
    }
}

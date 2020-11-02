package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TaxMode;
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

public class SignShopSell extends BaseSignShop{
    private final LocationType type = LocationType.SIGN_SHOP_SELL;

    public SignShopSell(UUID owner){
        super(owner);
    }

    public SignShopSell(LocationDbModel model) {
        super(model);
    }

    @Override
    public void loadItems() {
        this.items = SignShopConnection.getInstance().getSellShopItems(owner);
    }

    @Override
    public String getTitle() {
        return I18n.format("shop.title.sell");
    }

    @Override
    public boolean doBusiness(Player related, ShopItem item, int amount) {
        double fee = HamsterEcoHelper.plugin.config.signShopFeeBase;
        TransactionRequest req = new TransactionRequest.TransactionBuilder()
                .reason(TaxReason.REASON_SIGN_SHOP)
                .seller(owner)
                .buyer(related.getUniqueId())
                .item(item)
                .amount(amount)
                .fee(fee)
                .taxMode(TaxMode.ADDITION)
                .build();

        TransactionController.getInstance().makeTransaction(req);
        updateUi();
        return true;
    }

    @Override
    public LocationType getType() {
        return type;
    }

    @Override
    public BaseUi<ShopItem> newGUI() {
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(this);
        signShopGUI.refreshGUI();
        return signShopGUI;
    }
}

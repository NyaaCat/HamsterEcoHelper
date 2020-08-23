package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
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
        SignShopConnection.getInstance().getBuyShopItems(owner);
    }

    @Override
    public String getTitle() {
        return I18n.format("shop.title.buy");
    }

    @Override
    public void doBusiness(Player buyer, ShopItem item, int amount){
        //todo configure sign shop storage space.
        TransactionController.getInstance().makeTransaction(owner, buyer.getUniqueId(), item, amount);
        updateUi();
    }

    @Override
    public LocationType getType() {
        return type;
    }
}

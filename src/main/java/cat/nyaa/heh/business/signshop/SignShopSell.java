package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
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
    public void doBusiness(Player related, ShopItem item, int amount) {
        //todo configure sign shop storage space.
        TransactionController.getInstance().makeTransaction(related.getUniqueId(), owner, item, amount);
        updateUi();
    }

    @Override
    public LocationType getType() {
        return type;
    }
}

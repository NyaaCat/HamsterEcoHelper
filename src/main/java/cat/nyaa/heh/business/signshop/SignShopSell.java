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
        double fee = HamsterEcoHelper.plugin.config.signShopFeeBase;
        LocationDbModel reqLocationModel = LocationConnection.getInstance().getReqLocationModel(owner);
        Block block = reqLocationModel.getBlock();
        if(!(block.getState() instanceof Chest)){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
            String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : offlinePlayer.getName();
            new Message(I18n.format("shop.sign.buy.no_chest")).send(offlinePlayer);
            new Message(I18n.format("shop.sign.sell.no_chest", name)).send(offlinePlayer);
        }

        TransactionController.getInstance().makeTransaction(related.getUniqueId(), owner, item, amount, fee);
        updateUi();
    }

    @Override
    public LocationType getType() {
        return type;
    }

    @Override
    public SignShopGUI newGUI() {
        return new SignShopGUI(this);
    }
}

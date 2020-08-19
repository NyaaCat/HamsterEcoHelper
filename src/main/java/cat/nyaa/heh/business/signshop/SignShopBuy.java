package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.SignShopDbModel;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignShopBuy extends BaseSignShop{
    private final SignShopType type = SignShopType.SELL;

    public SignShopBuy(){}

    public SignShopBuy(SignShopDbModel model){
        this.owner = model.getOwner();
        String worldName = model.getWorld();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new NullPointerException("world " + worldName +" don't exist");
        }
        Block blockAt = world.getBlockAt(model.getX(), model.getY(), model.getZ());
        BlockState state = blockAt.getState();
        if (!(state instanceof Sign)){
            throw new IllegalStateException(String.format("block at world:%s, x:%d, y:%d, z:%d is not a sign.", worldName, model.getX(), model.getY(), model.getZ()));
        }
        this.sign = (Sign) state;
        loadItems();
    }

    @Override
    public void loadItems() {
        SignShopConnection.getInstance().getBuyShopItems(owner);
    }

    @Override
    public void doBusiness(Player buyer, ShopItem item, int amount){
        //todo configure sign shop storage space.
        TransactionController.getInstance().makeTransaction(owner, buyer.getUniqueId(), item, amount);
        updateUi();
    }

    @Override
    public SignShopType getType() {
        return type;
    }
}

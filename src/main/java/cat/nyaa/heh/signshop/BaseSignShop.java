package cat.nyaa.heh.signshop;

import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.SignShopDbModel;
import cat.nyaa.heh.enums.SignShopType;
import cat.nyaa.heh.item.ShopItem;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseSignShop {
    protected List<ShopItem> items = new ArrayList<>();
    protected Location location;
    protected Sign sign;
    protected UUID owner;
    protected boolean signExist = false;

    public void setSign(Sign sign) {
        this.sign = sign;
        Block block = sign.getBlock();
        this.location = block.getLocation();
        signExist = true;
    }

    public abstract SignShopType getType();

    public Location getLocation() {
        return location;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isSignExist(){
        return signExist;
    }

    public SignShopDbModel toDbModel(){
        return new SignShopDbModel(this);
    }

    public void offer(ItemStack itemStack, double unitPrice){
        //todo
    }

    public abstract void doBusiness(Player related, ShopItem item, int amount);

    public void retrieve(ShopItem item, int amount){
        //todo
    }

    public abstract void loadItems();
}

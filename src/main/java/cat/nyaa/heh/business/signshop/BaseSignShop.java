package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.SignShopDbModel;
import cat.nyaa.heh.item.ShopItemType;
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
    public abstract void loadItems();

    /**
     * do business of this transaction
     * @param related
     * @param item
     * @param amount
     */
    public abstract void doBusiness(Player related, ShopItem item, int amount);

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

    public long offer(Player player, ItemStack itemStack, double unitPrice){
        ShopItem shopItem = new ShopItem(player.getUniqueId(), ShopItemType.SIGNSHOP_BUY, itemStack, unitPrice);
        this.internalAddItemToList(shopItem);
        long uid = SignShopConnection.getInstance().addItem(this, shopItem);
        return uid;
    }

    public void retrieve(ShopItem item, int amount){
        if (item.getAmount() - item.getSoldAmount() - amount <= 0){
            item.setSold(item.getAmount());
        }else {
            item.setSold(item.getSoldAmount() + amount);
        }
        SignShopConnection.getInstance().updateItem(item);
        updateUi();
    }

    public void updateUi(){
        //todo
    }

    public List<ShopItem> getItems() {
        return items;
    }

    private void internalAddItemToList(ShopItem shopItem) {
        items.add(shopItem);
    }
    private void internalRemoveItemFromList(ShopItem shopItem) {
        items.remove(shopItem);
    }

}

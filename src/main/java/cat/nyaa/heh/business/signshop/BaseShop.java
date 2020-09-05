package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.db.model.LocationType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseShop {
    protected List<ShopItem> items = new ArrayList<>();

    public List<ShopItem> getItems() {
        return items;
    }

    /**
     * do business of this transaction
     * @param related
     * @param item
     * @param amount
     */
    public abstract void doBusiness(Player related, ShopItem item, int amount);

    public abstract LocationType getType();

    public abstract void loadItems();

    private void internalAddItemToList(ShopItem shopItem) {
        items.add(shopItem);
    }
    private void internalRemoveItemFromList(ShopItem shopItem) {
        items.remove(shopItem);
    }

    void add(ShopItem shopItem) {
        internalAddItemToList(shopItem);
    }

    public void remove(ShopItem shopItem) {
        internalRemoveItemFromList(shopItem);
    }
}

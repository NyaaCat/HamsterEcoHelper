package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.signshop.SignShopManager;
import cat.nyaa.heh.signshop.SignShopSell;
import cat.nyaa.heh.transaction.TransactionController;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.ShopComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class SignShopComponent extends ShopComponent {
    SignShopSell signShop;

    public SignShopComponent(Inventory inventory, SignShopSell signShop) {
        super(inventory);
        this.signShop = signShop;
    }

    @Override
    public void refreshUi() {

    }

    @Override
    public void loadData() {
        signShop.loadItems();
        this.items = signShop.getItems();
    }

    @Override
    public void loadData(List<ShopItem> data) {
        this.items = data;
    }
}

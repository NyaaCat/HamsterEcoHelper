package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.signshop.SignShopSell;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class SignShopComponent extends BasePagedComponent {
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

    }

    @Override
    public void loadData(List<ShopItem> data) {

    }

    @Override
    public void updateAsynchronously() {

    }

    @Override
    public void postUpdate() {

    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {

    }

    @Override
    public void onRightClick(InventoryClickEvent event) {

    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {

    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {

    }

    @Override
    public void onDrag(InventoryClickEvent event) {

    }
}

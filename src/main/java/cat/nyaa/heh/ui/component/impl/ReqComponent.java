package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.ui.BaseUi;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class ReqComponent extends LottoComponent{
    public ReqComponent(Inventory inventory, BaseSignShop signShop) {
        super(inventory, signShop);
    }

    @Override
    public void loadData() {
        // TODO: 2020/10/25 load items
    }

    @Override
    public void loadData(List<ShopItem> data) {
        super.loadData(data);
    }
}

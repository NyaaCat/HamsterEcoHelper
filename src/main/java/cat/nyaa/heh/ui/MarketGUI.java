package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.MarketComponent;

import java.util.List;

public class MarketGUI extends BaseUi<ShopItem> {
    MarketGUI(){
        super();
    }

    @Override
    public void refreshGUI(){
        getPagedComponent().loadData();
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<ShopItem> list){
        getPagedComponent().loadData(list);
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    @Override
    protected BasePagedComponent<ShopItem> newPagedComponent() {
        return new MarketComponent(uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.market");
    }

}

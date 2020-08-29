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
        BasePagedComponent<ShopItem> pageComponent = this.getPageComponent();
        pageComponent.loadData();
        pageComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<ShopItem> list){
        BasePagedComponent<ShopItem> pageComponent = this.getPageComponent();
        pageComponent.loadData(list);
        pageComponent.refreshUi();
    }

    @Override
    protected BasePagedComponent<ShopItem> getPageComponent() {
        return new MarketComponent(uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.market", pagedComponent.getCurrentPage()+1, pagedComponent.getSize()/pagedComponent.getPageSize()+2);
    }

}

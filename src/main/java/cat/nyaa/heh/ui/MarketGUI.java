package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.MarketComponent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class MarketGUI extends BaseUi {
    MarketGUI(){
        super();
    }

    public void refreshGUI(){
        BasePagedComponent pageComponent = this.getPageComponent();
        pageComponent.loadData();
        pageComponent.refreshUi();
    }

    public void refreshGUI(List<ShopItem> list){
        BasePagedComponent pageComponent = this.getPageComponent();
        pageComponent.loadData(list);
        pageComponent.refreshUi();
    }

    @Override
    protected BasePagedComponent getPageComponent() {
        return new MarketComponent(uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.market");
    }

    @Override
    public Inventory getInventory() {
        return uiInventory;
    }
}

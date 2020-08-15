package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.MarketComponent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class MarketGUI extends BaseUi {
    public MarketGUI(){
        super();
    }

    public void refreshGUI(){
        this.getPageComponent().loadData();
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

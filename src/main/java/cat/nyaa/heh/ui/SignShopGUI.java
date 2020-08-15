package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.SignShopComponent;
import org.bukkit.inventory.Inventory;

public class SignShopGUI extends BaseUi{
    SignShopGUI(){
        super();
    }

    @Override
    protected BasePagedComponent getPageComponent() {
        return new SignShopComponent(uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.sign_shop");
    }

    @Override
    public Inventory getInventory() {
        return uiInventory;
    }
}

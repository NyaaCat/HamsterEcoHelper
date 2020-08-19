package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.SignShopComponent;

public class SignShopGUI extends BaseUi{
    private BaseSignShop signShop;

    SignShopGUI(BaseSignShop signShop){
        super();
        this.signShop = signShop;
    }

    @Override
    protected BasePagedComponent getPageComponent() {
        return new SignShopComponent(uiInventory, signShop);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.sign_shop");
    }
}

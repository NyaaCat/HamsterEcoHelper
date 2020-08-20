package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.SignShopComponent;

import java.util.List;
import java.util.UUID;

public class SignShopGUI extends BaseUi{
    protected UUID owner;
    private BaseSignShop signShop;

    SignShopGUI(BaseSignShop signShop){
        super();
        this.signShop = signShop;
        this.owner = signShop.getOwner();
    }

    @Override
    protected BasePagedComponent getPageComponent() {
        return new SignShopComponent(uiInventory, signShop);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.sign_shop");
    }

    @Override
    public void refreshGUI() {
        //todo
    }

    @Override
    public void refreshGUI(List<ShopItem> items) {
        //todo
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}

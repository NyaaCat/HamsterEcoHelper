package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.SignShopComponent;

import java.util.List;
import java.util.UUID;

public class SignShopGUI extends BaseUi<ShopItem>{
    protected UUID owner;
    private BaseSignShop signShop;

    public SignShopGUI(BaseSignShop signShop){
        super();
        this.signShop = signShop;
        this.owner = signShop.getOwner();
    }

    @Override
    protected BasePagedComponent<ShopItem> getPageComponent() {
        return new SignShopComponent(uiInventory, signShop);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.sign_shop", pagedComponent.getCurrentPage()+1, pagedComponent.getSize()/ pagedComponent.getPageSize()+2);
    }

    @Override
    public void refreshGUI() {
        pagedComponent.loadData();
        pagedComponent.refreshUi();
        buttonComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<ShopItem> items) {
        pagedComponent.loadData(items);
        pagedComponent.refreshUi();
        buttonComponent.refreshUi();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}

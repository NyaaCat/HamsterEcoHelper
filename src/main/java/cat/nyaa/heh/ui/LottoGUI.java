package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.LottoComponent;

import java.util.List;
import java.util.UUID;

public class LottoGUI extends BaseUi<ShopItem> {
    protected UUID owner;
    private BaseSignShop signShop;

    public LottoGUI(BaseSignShop signShop){
        super();
        this.signShop = signShop;
        this.owner = signShop.getOwner();
        createComponents();
        refreshGUI();
    }

    @Override
    protected BasePagedComponent<ShopItem> newPagedComponent() {
        return new LottoComponent(uiInventory, signShop);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.lotto");
    }

    @Override
    public void refreshGUI() {
        getPagedComponent().loadData();
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<ShopItem> items) {
        getPagedComponent().loadData(items);
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    public UUID getOwner() {
        return owner;
    }
}

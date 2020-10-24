package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.impl.StorageComponent;

import java.util.List;
import java.util.UUID;

public class StorageGUI extends BaseUi<StorageItem> {

    private UUID owner;

    public StorageGUI(UUID owner) {
        super();
        this.owner = owner;
        pagedComponent = newPagedComponent();
    }

    @Override
    protected BasePagedComponent<StorageItem> newPagedComponent() {
        return new StorageComponent(owner, uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.storage");
    }

    @Override
    public void refreshGUI() {
        getPagedComponent().loadData();
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<StorageItem> items) {
        getPagedComponent().loadData(items);
        getPagedComponent().refreshUi();
        buttonComponent.refreshUi();
    }

    public UUID getOwner() {
        return owner;
    }
}

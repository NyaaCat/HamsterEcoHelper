package cat.nyaa.heh.ui;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.ShopComponent;
import cat.nyaa.heh.ui.component.StorageComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class StorageGUI extends BaseUi<StorageItem> {

    private UUID owner;

    public StorageGUI(UUID owner) {
        super();
        this.owner = owner;
    }

    @Override
    protected BasePagedComponent<StorageItem> getPageComponent() {
        return new StorageComponent(owner, uiInventory);
    }

    @Override
    protected String getTitle() {
        return I18n.format("ui.title.storage", pagedComponent.getCurrentPage()+1, pagedComponent.getSize()/ pagedComponent.getPageSize()+2);
    }

    @Override
    public void refreshGUI() {
        pagedComponent.loadData();
        pagedComponent.refreshUi();
    }

    @Override
    public void refreshGUI(List<StorageItem> items) {
        pagedComponent.loadData(items);
        pagedComponent.refreshUi();
    }
}

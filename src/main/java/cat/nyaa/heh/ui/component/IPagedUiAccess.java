package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.ui.component.button.ButtonHandler;
import org.bukkit.inventory.InventoryHolder;

public interface IPagedUiAccess extends InventoryHolder, ButtonHandler, RefreshableUi, InfoHolder {
    void setPage(int page);
    int getCurrentPage();
    int getPageSize();
    int getSize();
}

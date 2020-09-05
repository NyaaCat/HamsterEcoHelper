package cat.nyaa.heh.ui.component;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickEventHandler {
    void onLeftClick(InventoryClickEvent event);
    void onRightClick(InventoryClickEvent event);
    void onShiftLeftClick(InventoryClickEvent event);
    void onMiddleClick(InventoryClickEvent event);
    void onDrag(InventoryClickEvent event);
}

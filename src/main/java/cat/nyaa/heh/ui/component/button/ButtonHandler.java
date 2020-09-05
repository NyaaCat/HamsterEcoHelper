package cat.nyaa.heh.ui.component.button;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ButtonHandler {
    void onButtonClicked(GUIButton button, InventoryClickEvent event);
}

package cat.nyaa.heh;

import cat.nyaa.heh.ui.BaseUi;
import cat.nyaa.heh.ui.UiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class Events implements Listener {
    private final HamsterEcoHelper plugin;

    public Events(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onUiClicked(InventoryClickEvent event){
        if (!isHehUi(event.getInventory())){
            return;
        }
        UiManager instance = UiManager.getInstance();
        BaseUi ui = instance.getUi(event.getInventory());
        ui.onClickRawSlot(event);
    }

    private boolean isHehUi(Inventory inventory) {
        return UiManager.getInstance().isHehUi(inventory);
    }

    @EventHandler
    public void onUiClose(InventoryCloseEvent event){
        if (!isHehUi(event.getInventory())){
            return;
        }
    }
}

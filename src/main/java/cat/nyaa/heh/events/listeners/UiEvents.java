package cat.nyaa.heh.events.listeners;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.ui.BaseUi;
import cat.nyaa.heh.ui.UiManager;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class UiEvents implements Listener {
    private final HamsterEcoHelper plugin;

    public UiEvents(HamsterEcoHelper plugin) {
        this.plugin = plugin;
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

    @EventHandler
    public void onUiClicked(InventoryDragEvent event){
        if (!isHehUi(event.getInventory())){
            return;
        }
        UiManager instance = UiManager.getInstance();
        BaseUi ui = instance.getUi(event.getInventory());
        ui.onDragRawSlot(event);
    }

    private boolean isHehUi(Inventory inventory) {
        return UiManager.getInstance().isHehUi(inventory);
    }

    @EventHandler
    public void onUiClose(InventoryCloseEvent event){
        if (!isHehUi(event.getInventory())){
            return;
        }
        UiManager instance = UiManager.getInstance();
        instance.removeUi(event.getInventory());
    }
}

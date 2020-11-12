package cat.nyaa.heh.events.listeners;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.ui.BaseUi;
import cat.nyaa.heh.ui.UiManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UiEvents implements Listener {
    private final HamsterEcoHelper plugin;

    public UiEvents(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSampleClicked(InventoryClickEvent event){
        if (isHehUi(event.getInventory())){
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (checkAndRemoveSample(item)) {
            event.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    private boolean checkAndRemoveSample(ItemStack item) {
        if (item == null){
            return false;
        }
        if (item.getType().isAir()) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null){
            return false;
        }

        if (ShopItem.isSample(item)) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
        if (checkAndRemoveSample(itemInMainHand)) {
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
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

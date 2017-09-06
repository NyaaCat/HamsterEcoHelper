package cat.nyaa.HamsterEcoHelper.quest.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;

/**
 * Every instance of this class should represent it's very own data source.
 * No two instance should share single source.
 * e.g. You should never make two instances for one sign shop.
 * otherwise synchronization issues will occur
 */
public abstract class PaginatedListGui implements InventoryHolder {

    protected final Map<UUID, Inventory> openedUI = new HashMap<>();
    protected final Map<UUID, Integer> openedUiPages = new HashMap<>();
    protected final String basicTitle;

    private PairList<String, ItemStack> currentItems;

    protected PaginatedListGui(String title) {
        basicTitle = title;
    }

    /**
     * Show the player this GUI
     */
    public void openFor(Player player) {
        openedUiPages.put(player.getUniqueId(), 1);
        Inventory inv = Bukkit.createInventory(this, 54, basicTitle + " - Page 1");
        currentItems = getFullGuiContent();
        ItemStack[] t = currentItems.getValues(0,45).toArray(new ItemStack[0]);
        inv.setContents(t);
        openedUI.put(player.getUniqueId(), inv);
        player.openInventory(inv);
        // TODO
    }

    /**
     * This method should never be called because one GUI
     * can have multiple inventories.
     * The sole purpose is to make the compiler happy.
     *
     * The InventoryHolder is used to identify which GUI an inventory belongs to
     */
    @Override
    public final Inventory getInventory() {
        throw new UnsupportedOperationException();
    }

    /**
     * event callback.
     * plugins are required to register their own listener.
     * this may be changed when this API gets merged into NyaaCore.
     * @param ev
     */
    public void onInventoryClicked(InventoryClickEvent ev) {
        // TODO
        ev.setCancelled(true);
        if (ev.getClickedInventory() == null) return;
        if (!(ev.getWhoClicked() instanceof Player)) {
            System.err.print("inventory not clicked by player?");
            return;
        }
        if (ev.getClickedInventory() != openedUI.get(ev.getWhoClicked().getUniqueId())) {
            System.err.print("user clicked an unknown inventory.");
            System.err.print("clicked:"+ev.getInventory());
            System.err.print("expected:"+openedUI.get(ev.getWhoClicked().getUniqueId()));
            //return;
            // TODO it seems bukkit does some magic when opening an inventory
            //       so we need another way to track inventories (not by "==")
        }
        Integer page = openedUiPages.get(ev.getWhoClicked().getUniqueId());
        if (page == null) {
            System.err.print("user clicked an unknown inventory page no.");
            return;
        }

        itemClicked((Player)ev.getWhoClicked(), currentItems.getKey((page-1)*45+ev.getSlot()));
    }

    /**
     * Should be implemented by subclasses.
     * @return An ordered map of items to be shown in this GUI
     */
    protected abstract PairList<String, ItemStack> getFullGuiContent();
    protected abstract void itemClicked(Player player, String itemKey);
}

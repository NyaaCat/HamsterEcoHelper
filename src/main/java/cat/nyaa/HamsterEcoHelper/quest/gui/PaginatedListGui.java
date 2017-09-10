package cat.nyaa.HamsterEcoHelper.quest.gui;

import cat.nyaa.HamsterEcoHelper.I18n;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Every instance of this class should represent it's very own data source.
 * No two instance should share single source.
 * e.g. You should never make two instances for one sign shop.
 * otherwise synchronization issues will occur
 */
public abstract class PaginatedListGui implements InventoryHolder {
    protected final Map<UUID, Inventory> openedUI = new TreeMap<>();
    protected final Map<UUID, Integer> openedUiPages = new TreeMap<>(); // pages count from 0
    protected final String basicTitle;

    /* the following three variables are updated (and should only be update)
     * in {@link #contentChanged()}
     */
    private boolean guiFrozen = false;
    private PairList<String, ItemStack> fullContentCache = null;
    private int maxPage; // how may pages this gui has. pageIdx in range [0, maxPage)

    protected PaginatedListGui(String title) {
        basicTitle = title;
        contentChanged();
    }

    /**
     * Show the player this GUI
     */
    public void openFor(Player player) {
        showPlayerPage(player, 0);
    }

    /**
     * Show page to player.
     * If page not in range, close gui & clear player record.
     * @param p
     * @param page
     */
    private void showPlayerPage(Player p, int page) {
        UUID id = p.getUniqueId();
        if (page >=0 && page < maxPage) {
            Inventory inv = Bukkit.createInventory(this, 54, basicTitle + I18n.format("user.quest.title_page", page + 1));
            ItemStack[] tmp = fullContentCache.getValues(page*45, (page+1)*45).toArray(new ItemStack[0]);
            inv.setContents(tmp);

            // set prev/next page buttons
            if (page == 0) {
                inv.setItem(45, getNamedItem(Material.BARRIER, I18n.format("user.quest.first_page")));
            } else {
                inv.setItem(45, getNamedItem(Material.ARROW, I18n.format("user.quest.prev_page")));
            }
            if (page == maxPage - 1) {
                inv.setItem(53, getNamedItem(Material.BARRIER, I18n.format("user.quest.last_page")));
            } else {
                inv.setItem(53, getNamedItem(Material.ARROW, I18n.format("user.quest.next_page")));
            }

            openedUiPages.put(id, page);
            openedUI.put(id, inv);
            p.openInventory(inv);
        } else {
            openedUiPages.remove(id);
            openedUI.remove(id);
            if (isCurrentGui(p)) p.closeInventory();
        }
    }

    public ItemStack getNamedItem(Material material, String title) {
        ItemStack stack = new ItemStack(material);
        ItemMeta m = stack.getItemMeta();
        m.setDisplayName(title);
        stack.setItemMeta(m);
        return stack;
    }

    /**
     * Check if the inventory opened by the player
     * is managed by this GUI object
     * i.e. inventoryHolder == this
     */
    public boolean isCurrentGui(Player p) {
        return p.getOpenInventory() != null &&
                p.getOpenInventory().getTopInventory() != null &&
                p.getOpenInventory().getTopInventory().getHolder() == this;
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
        // TODO change all System.err
        ev.setCancelled(true);
        if (guiFrozen) return;
        if (ev.getClickedInventory() == null) return;
        if (!(ev.getWhoClicked() instanceof Player)) {
            System.err.print("inventory not clicked by player?");
            return;
        }
        Player p = (Player)ev.getWhoClicked();
        if (!openedUI.containsKey(p.getUniqueId()) || !openedUiPages.containsKey(p.getUniqueId())) {
            System.err.print("user not registered in gui");
            return;
        }
        Inventory inv = ev.getClickedInventory();
        if (!inv.equals(openedUI.get(p.getUniqueId()))) {
            System.err.print("user clicked on unknown inventory");
            return;
        }
        Integer page = openedUiPages.get(ev.getWhoClicked().getUniqueId());
        Integer slot = ev.getSlot();
        if (slot >= 0 && slot < 45) { //clicked on item
            String key = fullContentCache.getKey(page * 45 + slot);
            if (key != null) {
                itemClicked(p, key);
            } else {
                // TODO user clicked on an empty slot. Need better handling
                //System.err.print("user clicked slot out of range");
            }
        } else if (slot == 45) { // previous page
            if (page - 1 >= 0) showPlayerPage(p, page - 1);
        } else if (slot == 53) { // next page
            if (page + 1 < maxPage) showPlayerPage(p, page + 1);
        } else if (slot > 45 && slot < 53) { // custom buttons
            // TODO not implemented
        } else { // unknown buttons
            System.err.print("user clicked on unknown slot");
        }
    }

    /**
     * Should be implemented by subclasses.
     * PaginatedListGui will cache the returned List.
     * When the content is changed, subclass should call {@link #contentChanged}
     *
     * @return An ordered map of items to be shown in this GUI
     */
    protected abstract PairList<String, ItemStack> getFullGuiContent();

    /**
     * This method will be called when a player clicked on an item.
     * TODO: subclasses may need another cache to store the itemKeys, maybe we can avoid this?
     * @param itemKey the key returned by {@link #getFullGuiContent()}
     */
    protected abstract void itemClicked(Player player, String itemKey);

    /**
     * Invoked when all inventories of this gui are closed by players.
     * Subclasses may or may not implement this.
     * TODO not implemented, inventory close event
     */
    protected void guiCompletelyClosed() {

    }

    /**
     * Subclasses should call this method when there's any change
     * on the contents.
     * When invoked, PaginatedListGui will invalid the cache immediately
     * call {@link #getFullGuiContent()} again, and refresh all opened inventories.
     * TODO: players may unintentionally click on the wrong item if there are many players competing.
     * TODO: adding a short cooldown time may be a good idea?
     */
    protected void contentChanged() {
        guiFrozen = true; // TODO gui may not need to be frozen, further investigation needed.

        // update cache
        fullContentCache = getFullGuiContent();
        maxPage = fullContentCache.size() <= 0? 1 :(fullContentCache.size()-1)/45+1;

        // update opened inventories
        if (!openedUI.isEmpty() || !openedUiPages.isEmpty()) {
            Map<Player, Integer> newPages = new TreeMap<>();

            for (UUID id : openedUI.keySet()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null) continue;
                Integer page = openedUiPages.get(id);
                if (page == null) continue;
                if (page < 0) continue;
                if (page >= maxPage) page = maxPage - 1;
                newPages.put(p, page);
            }
            openedUI.clear();
            openedUiPages.clear();
            for (Map.Entry<Player, Integer> e : newPages.entrySet()) {
                showPlayerPage(e.getKey(), e.getValue());
            }
        }

        guiFrozen = false;
    }
}

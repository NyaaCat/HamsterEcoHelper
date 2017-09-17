package cat.nyaa.HamsterEcoHelper.quest.gui;

import cat.nyaa.HamsterEcoHelper.I18n;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
    public static final int PAGE_CAPACITY = 45; // NOTE: this size doesn't include control line. So the actual size will be 9 larger.
    // TODO openedUI & openedUiPages desync handling
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
            Inventory inv = Bukkit.createInventory(this, PAGE_CAPACITY + 9, basicTitle + I18n.format("user.quest.title_page", page + 1));
            ItemStack[] tmp = fullContentCache.getValues(page*PAGE_CAPACITY, (page+1)*PAGE_CAPACITY).toArray(new ItemStack[0]);
            inv.setContents(tmp);

            // set prev/next page buttons
            if (page == 0) {
                inv.setItem(PAGE_CAPACITY, getNamedItem(Material.BARRIER, I18n.format("user.quest.first_page")));
            } else {
                inv.setItem(PAGE_CAPACITY, getNamedItem(Material.ARROW, I18n.format("user.quest.prev_page")));
            }
            if (page == maxPage - 1) {
                inv.setItem(PAGE_CAPACITY+8, getNamedItem(Material.BARRIER, I18n.format("user.quest.last_page")));
            } else {
                inv.setItem(PAGE_CAPACITY+8, getNamedItem(Material.ARROW, I18n.format("user.quest.next_page")));
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
     * TODO this may be changed when this API gets merged into NyaaCore.
     *
     * @param ev
     */
    public void onInventoryClicked(InventoryClickEvent ev, Inventory inv, Player p) {
        // TODO change all System.err
        if (ev.getClickedInventory() != inv) throw new IllegalArgumentException();
        if (inv == null) throw new IllegalArgumentException();
        if (this != inv.getHolder()) throw new IllegalArgumentException();
        if (p == null || p != ev.getWhoClicked()) throw new IllegalArgumentException();
        if (!ev.isLeftClick()) return;

        ev.setCancelled(true);
        if (guiFrozen) return;
        UUID id = p.getUniqueId();
        if (!openedUI.containsKey(id) || !openedUiPages.containsKey(id)) {
            System.err.print("user not registered in gui");
            return;
        }
        if (!inv.equals(openedUI.get(id))) {
            System.err.print("user clicked on inventory not eq record");
            return;
        }
        Integer page = openedUiPages.get(id);
        Integer slot = ev.getSlot();
        if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR) return;
        if (slot >= 0 && slot < PAGE_CAPACITY) { //clicked on item
            String key = fullContentCache.getKey(page * PAGE_CAPACITY + slot);
            if (key != null) {
                itemClicked(p, key, ev.isShiftClick());
            } else {
                // TODO user clicked on an empty slot. Need better handling
                //System.err.print("user clicked slot out of range");
            }
        } else if (slot == PAGE_CAPACITY) { // previous page
            if (page - 1 >= 0) showPlayerPage(p, page - 1);
        } else if (slot == PAGE_CAPACITY + 8) { // next page
            if (page + 1 < maxPage) showPlayerPage(p, page + 1);
        } else if (slot > PAGE_CAPACITY && slot < PAGE_CAPACITY + 8) { // custom buttons
            // TODO not implemented
        } else { // unknown buttons
            System.err.print("user clicked on unknown slot");
        }
    }

    public void onInventoryClosed(InventoryCloseEvent ev) {
        UUID id = ev.getPlayer().getUniqueId();
        openedUiPages.remove(id);
        openedUI.remove(id);
        if (openedUI.size() == 0) guiCompletelyClosed();
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
    protected abstract void itemClicked(Player player, String itemKey, boolean isShiftClick);

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
     *
     * You must call this from subclass constructor to initialize the cache.
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

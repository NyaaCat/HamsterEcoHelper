package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.utils.Database;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Events implements Listener {
    private final HamsterEcoHelper plugin;
    public Events(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (MarketManager.viewMailbox.contains(player)) {
            MarketManager.viewMailbox.remove(player);
            MarketManager.setMailbox(player, e.getInventory().getContents());
            return;
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getTitle().contains(I18n.get("user.market.title")) && MarketManager.viewItem.containsKey(player)) {
            UUID seller = MarketManager.viewSeller.get(player);
            HashMap<Integer, Integer> slot = MarketManager.viewItem.get(player);
            if (slot.containsKey(event.getRawSlot())) {
                if (event.getInventory().getSize() == 54 &&
                        event.getInventory().getItem(48) != null &&
                        event.getInventory().getItem(48).getType() == Material.CHEST) {
                    int itemId = MarketManager.viewItem.get(player).get(event.getRawSlot());
                    event.setCancelled(true);
                    Database.MarketItem marketItem = MarketManager.getItem(itemId);
                    if (marketItem != null && marketItem.getItemStack().getType() != Material.AIR) {
                        if (event.isShiftClick()) {
                            MarketManager.buy(player, itemId, marketItem.getAmount());
                        } else {
                            MarketManager.buy(player, itemId, 1);
                        }
                    }
                    MarketManager.view(player, MarketManager.viewPage.get(player), seller);
                    return;
                }
                MarketManager.viewItem.remove(player);
                return;
            }
            if (event.getRawSlot() == 45 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.view(player, MarketManager.viewPage.get(player) - 1, seller);
            } else if (event.getRawSlot() == 47 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.view(player, 1, player.getUniqueId());
            } else if (event.getRawSlot() == 48 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.openMailbox(player);
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.view(player, MarketManager.viewPage.get(player) + 1, seller);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        List<ItemStack> items = plugin.database.getTemporaryStorage(ev.getPlayer());
        if (items.size() > 0) {
            CommandHandler.msg(ev.getPlayer(), "user.info.has_temporary_storage");
        }
    }
}

package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.market.Market;
import cat.nyaa.HamsterEcoHelper.market.MarketItem;
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

public class Events implements Listener {
    private final HamsterEcoHelper plugin;
    public Events(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (Market.viewMailbox.contains(player)) {
            Market.viewMailbox.remove(player);
            Market.setMailbox(player, e.getInventory().getContents());
            return;
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String seller = Market.viewSeller.get(player);
        if (event.getInventory().getTitle().contains(I18n.get("user.market.title")) && Market.viewItem.containsKey(player)) {
            HashMap<Integer, Integer> slot = Market.viewItem.get(player);
            if (slot.containsKey(event.getRawSlot())) {
                if (event.getInventory().getSize() == 54 &&
                        event.getInventory().getItem(48) != null &&
                        event.getInventory().getItem(48).getType() == Material.CHEST) {
                    int itemId = Market.viewItem.get(player).get(event.getRawSlot());
                    event.setCancelled(true);
                    MarketItem marketItem = Market.getItem(itemId);
                    if (marketItem != null && marketItem.getItemStack().getType() != Material.AIR) {
                        if (event.isShiftClick()) {
                            Market.buy(player, itemId, marketItem.getAmount());
                        } else {
                            Market.buy(player, itemId, 1);
                        }
                    }
                    Market.view(player, Market.viewPage.get(player), seller);
                    return;
                }
                Market.viewItem.remove(player);
                return;
            }
            if (event.getRawSlot() == 45 && event.getCurrentItem().getType() != Material.AIR) {
                Market.view(player, Market.viewPage.get(player) - 1, seller);
            } else if (event.getRawSlot() == 47 && event.getCurrentItem().getType() != Material.AIR) {
                Market.view(player, 1, player.getUniqueId().toString());
            } else if (event.getRawSlot() == 48 && event.getCurrentItem().getType() != Material.AIR) {
                Market.openMailbox(player);
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                Market.view(player, Market.viewPage.get(player) + 1, seller);
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

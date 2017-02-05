package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.nyaautils.api.events.HamsterEcoHelperTransactionApiEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getTitle().contains(I18n._("user.market.title")) &&
                MarketManager.viewItem.containsKey(player)) {
            event.setCancelled(true);
            UUID seller = MarketManager.viewSeller.get(player);
            HashMap<Integer, Long> slot = MarketManager.viewItem.get(player);
            if (slot.containsKey(event.getRawSlot())) {
                if (event.getInventory().getSize() == 54 &&
                        event.getInventory().getItem(47) != null &&
                        event.getInventory().getItem(47).getType() == Material.PAPER) {
                    long itemId = MarketManager.viewItem.get(player).get(event.getRawSlot());
                    MarketItem marketItem = MarketManager.getItem(itemId);
                    if (marketItem != null && marketItem.getItemStack().getType() != Material.AIR) {
                        if (event.isShiftClick()) {
                            MarketManager.buy(player, itemId, marketItem.amount);
                        } else {
                            MarketManager.buy(player, itemId, 1);
                        }
                    }
                    MarketManager.openGUI(player, MarketManager.viewPage.get(player), seller);
                    return;
                }
                MarketManager.closeGUI(player);
                return;
            }
            if (event.getRawSlot() == 45 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.openGUI(player, MarketManager.viewPage.get(player) - 1, seller);
            } else if (event.getRawSlot() == 47 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.openGUI(player, 1, player.getUniqueId());
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                MarketManager.openGUI(player, MarketManager.viewPage.get(player) + 1, seller);
            } else {
                MarketManager.closeGUI(player);
            }
        } else {
            if (event.getCurrentItem() != null && MarketManager.isMarketItem(event.getCurrentItem())) {
                event.setCurrentItem(new ItemStack(Material.AIR));
            } else if (event.getCursor() != null && MarketManager.isMarketItem(event.getCursor())) {
                event.setCursor(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        List<ItemStack> items = plugin.database.getTemporaryStorage(ev.getPlayer());
        if (items.size() > 0) {
            ev.getPlayer().sendMessage(I18n._("user.info.has_temporary_storage"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getItemDrop() != null && MarketManager.isMarketItem(e.getItemDrop().getItemStack())) {
            e.getItemDrop().setItemStack(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (e.getItem() != null && MarketManager.isMarketItem(e.getItem().getItemStack())) {
            e.getItem().setItemStack(new ItemStack(Material.AIR));
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.config.death_penalty_worlds.contains(event.getEntity().getWorld().getName())) {
            double penalty = 0.0D;
            double playerBalance = plugin.eco.balance(event.getEntity());
            if (playerBalance < plugin.config.death_penalty_min) {
                penalty = plugin.config.death_penalty_min;
            } else {
                penalty = (playerBalance / 100) * plugin.config.death_penalty_percent;
            }
            if (penalty > plugin.config.death_penalty_max) {
                penalty = plugin.config.death_penalty_max;
            } else if (penalty < plugin.config.death_penalty_min) {
                penalty = plugin.config.death_penalty_min;
            }
            if (penalty > 0.0D && plugin.eco.withdraw(event.getEntity(), penalty)) {
                event.getEntity().sendMessage(I18n._("user.death_penalty.message", penalty));
                plugin.balanceAPI.deposit(penalty);
            }
        }
    }

    @EventHandler
    public void onHamsterEcoHelperTransactionApiEvent(HamsterEcoHelperTransactionApiEvent event) {
        if (plugin.balanceAPI.isEnabled() && event.getCost() > 0.0D) {
            plugin.balanceAPI.deposit(event.getCost());
        }
    }
}

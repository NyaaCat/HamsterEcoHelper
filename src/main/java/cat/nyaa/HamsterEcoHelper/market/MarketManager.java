package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.nyaacore.Message;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class MarketManager extends BukkitRunnable {
    public static String market_lore_code = ChatColor.translateAlternateColorCodes('&', "&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r");
    public long lastBroadcast;
    private HamsterEcoHelper plugin;

    public MarketManager(HamsterEcoHelper pl) {
        plugin = pl;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        runTaskTimer(plugin, 1, 600 * 20);
    }

    public static boolean isMarketItem(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) &&
                item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String lore : item.getItemMeta().getLore()) {
                if (lore.contains(market_lore_code)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getPlayerSlot(Player player) {
        int slot = 0;
        for (String group : plugin.config.marketSlot.keySet()) {
            int tmp = plugin.config.marketSlot.get(group);
            if (player.hasPermission("heh.offer." + group) && tmp > slot) {
                slot = tmp;
            }
        }
        return slot;
    }

    public boolean offer(Player player, ItemStack item, double unit_price) {
        if (getPlayerSlot(player) <= plugin.database.getMarketPlayerItemCount(player)) {
            player.sendMessage(I18n.format("user.market.not_enough_slot"));
            return false;
        }
        if (plugin.config.market_offer_fee > 0) {
            if (!plugin.eco.enoughMoney(player, plugin.config.market_offer_fee)) {
                player.sendMessage(I18n.format("user.warn.no_enough_money"));
                return false;
            } else {
                plugin.eco.withdraw(player, plugin.config.market_offer_fee);
                plugin.systemBalance.deposit(plugin.config.market_offer_fee, plugin);
            }
        }
        long marketItemID = plugin.database.marketOffer(player, item, unit_price);
        long itemID = plugin.database.getItemID(item);
        plugin.logger.info(I18n.format("log.info.market_offer", marketItemID, Utils.getItemName(item), item.getAmount(), unit_price, player.getName(), itemID));
        if (plugin.config.marketBroadcast && (System.currentTimeMillis() - lastBroadcast) > (plugin.config.marketBroadcastCooldown * 1000)) {
            lastBroadcast = System.currentTimeMillis();
            new Message("").append(I18n.format("user.market.broadcast"), item).broadcast();
        }
        updateAllGUI();
        return true;
    }

    public MarketItem getItem(long itemId) {
        return plugin.database.getMarketItem(itemId);
    }

    public void openGUI(Player player, int page, UUID seller) {
        MarketGUI marketGUI = new MarketGUI(plugin, player, seller);
        marketGUI.openGUI(player, page);
    }

    public void closeAllGUI() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null &&
                    player.getOpenInventory().getTopInventory().getHolder() instanceof MarketGUI) {
                player.closeInventory();
            }
        }
    }

    public void playSound(Player player, Sound sound) {
        if (plugin.config.marketPlaySound) {
            player.playSound(player.getLocation(), sound, 1, 2);
        }
        return;
    }

    public void updateAllGUI() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.isOnline() && p.getOpenInventory() != null &&
                    p.getOpenInventory().getTopInventory() != null &&
                    p.getOpenInventory().getTopInventory().getHolder() instanceof MarketGUI) {
                MarketGUI marketGUI = ((MarketGUI) p.getOpenInventory().getTopInventory().getHolder());
                openGUI(p, marketGUI.getCurrentPage(), marketGUI.seller);
            }
        }
    }

    @Override
    public void run() {
        if (plugin.config.market_placement_fee > 0 &&
                System.currentTimeMillis() - plugin.config.variablesConfig.market_placement_fee_timestamp >= 86400000) {
            plugin.config.variablesConfig.market_placement_fee_timestamp = System.currentTimeMillis();
            int itemCount = plugin.database.getMarketItemCount();
            if (itemCount > 0) {
                int fail = 0;
                List<MarketItem> items = plugin.database.getMarketItems(0, itemCount, null);
                for (MarketItem item : items) {
                    if (!plugin.eco.withdraw(item.getPlayer(), plugin.config.market_placement_fee)) {
                        fail++;
                        plugin.logger.info(I18n.format("log.info.placement_fee_fail",
                                item.id, item.getPlayer().getName(), "Not enough money"));
                    }
                }
                if (fail < itemCount) {
                    plugin.systemBalance.deposit((itemCount - fail) * plugin.config.market_placement_fee, plugin);
                }
                plugin.logger.info(I18n.format("log.info.placement_fee", itemCount, fail));
            }
        }
    }
}

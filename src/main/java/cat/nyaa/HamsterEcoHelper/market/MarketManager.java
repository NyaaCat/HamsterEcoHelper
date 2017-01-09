package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.database.Database;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem;
import cat.nyaa.utils.Message;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class MarketManager extends BukkitRunnable{
    private static Database db;
    private static HamsterEcoHelper plugin;
    public static HashMap<Player, HashMap<Integer, Long>> viewItem;
    public static HashMap<Player, Integer> viewPage;
    public static HashMap<Player, UUID> viewSeller;
    public static long lastBroadcast;
    public static int pageSize = 45;
    public static String market_lore_code = ChatColor.translateAlternateColorCodes('&',"&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r"); 

    public MarketManager(HamsterEcoHelper pl) {
        plugin = pl;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        db = plugin.database;
        viewItem = new HashMap<>();
        viewPage = new HashMap<>();
        viewSeller = new HashMap<>();
        runTaskTimer(plugin, 1, 600 * 20);
    }

    public static boolean offer(Player player, ItemStack item, double unit_price) {
        if (getPlayerSlot(player) <= db.getMarketPlayerItemCount(player)) {
            return false;
        }
        if (plugin.config.market_offer_fee > 0) {
            if (!plugin.eco.enoughMoney(player, plugin.config.market_offer_fee)) {
                player.sendMessage(I18n._("user.warn.no_enough_money"));
                return false;
            } else {
                plugin.eco.withdraw(player, plugin.config.market_offer_fee);
            }
        }
        long id = db.marketOffer(player, item, unit_price);
        plugin.logger.info(I18n._("log.info.market_offer", id, getItemName(item), item.getAmount(), unit_price, player.getName()));
        if (plugin.config.marketBroadcast && (System.currentTimeMillis() - lastBroadcast) > (plugin.config.marketBroadcastCooldown * 1000)) {
            lastBroadcast = System.currentTimeMillis();
            new Message("").append(item, I18n._("user.market.broadcast")).broadcast();
        }
        updateAllGUI();
        return true;
    }

    public static boolean buy(Player player, long itemId, int amount) {
        MarketItem item = getItem(itemId);
        if (item != null && item.getItemStack().getType() != Material.AIR && item.getAmount() > 0) {
            double price = item.getUnitPrice() * amount;
            double tax = 0.0D;
            if (plugin.config.market_tax > 0) {
                tax = (price / 100) * plugin.config.market_tax;
            }
            if (plugin.eco.enoughMoney(player, price + tax) || player.getUniqueId().equals(item.getPlayerId())) {
                int stat = Utils.giveItem(player, item.getItemStack(amount));
                player.sendMessage(I18n._("user.auc.item_given_" + Integer.toString(stat)));
                plugin.logger.info(I18n._("log.info.market_bought", itemId, getItemName(item.getItemStack()), 
                        amount, price, player.getName(), item.getPlayer().getName()));
                if (!player.getUniqueId().equals(item.getPlayerId())) {
                    if (item.getPlayer().isOnline()) {
                        new Message("")
                                .append(item.getItemStack(amount), I18n._("user.market.someone_bought",
                                        player.getName(), price + tax))
                                .send((Player) item.getPlayer());
                    }
                    new Message("")
                            .append(item.getItemStack(amount), I18n._("user.market.buy_success", item.getPlayer().getName(), price))
                            .send(player);
                    plugin.eco.withdraw(player, price + tax);
                    plugin.eco.deposit(item.getPlayer(), price);
                }
                db.marketBuy(player, itemId, amount);
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                updateAllGUI();
                return true;
            } else {
                player.sendMessage(I18n._("user.warn.no_enough_money"));
                playSound(player, Sound.ENTITY_ITEM_BREAK);
                return false;
            }
        }
        return false;
    }

    public static int getPlayerSlot(Player player) {
        int slot = 0;
        for (String group : plugin.config.marketSlot.keySet()) {
            int tmp = plugin.config.marketSlot.get(group);
            if (player.hasPermission("heh.offer." + group) && tmp > slot) {
                slot = tmp;
            }
        }
        return slot;
    }

    public static MarketItem getItem(long itemId) {
        return db.getMarketItem(itemId);
    }

    public static void openGUI(Player player, int page, UUID seller) {
        HashMap<Integer, Long> list = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(player, 54, I18n._("user.market.title"));
        int pageCount;
        if (seller != null && page >= 1) {
            viewSeller.put(player, seller);
            pageCount = (db.getMarketPlayerItemCount(Bukkit.getOfflinePlayer(seller)) + MarketManager.pageSize - 1) / MarketManager.pageSize;
        } else {
            viewSeller.put(player, null);
            pageCount = db.getMarketPageCount();
            seller = null;
        }
        int offset = 0;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        if (page > 1) {
            offset = (page - 1) * (MarketManager.pageSize);
        }
        viewPage.put(player, page);
        List<MarketItem> marketItem = db.getMarketItems(offset, MarketManager.pageSize, seller);
        if (marketItem != null) {
            for (int i = 0; i < marketItem.size(); i++) {
                MarketItem mItem = marketItem.get(i);
                list.put(i, mItem.getId());
                ItemMeta meta = mItem.getItemStack().getItemMeta();
                List<String> lore;
                if (meta.hasLore()) {
                    lore = meta.getLore();
                } else {
                    lore = new ArrayList<>();
                }
                if (plugin.config.market_tax > 0) {
                    double tax = (mItem.getUnitPrice() / 100) * plugin.config.market_tax;
                    lore.add(0, market_lore_code + ChatColor.RESET + I18n._("user.market.unit_price_with_tax",
                            mItem.getUnitPrice(), tax, plugin.config.market_tax));
                } else {
                    lore.add(0, market_lore_code + ChatColor.RESET + I18n._("user.market.unit_price",
                            mItem.getUnitPrice()));
                }
                lore.add(1, I18n._("user.market.offered", mItem.getPlayer().getName()));
                meta.setLore(lore);
                ItemStack itemStack = mItem.getItemStack();
                itemStack.setItemMeta(meta);
                inventory.setItem(i, itemStack);
            }
        }
        if (page > 1 || seller != null) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backItemMeta = back.getItemMeta();
            backItemMeta.setDisplayName(I18n._("user.info.back"));
            back.setItemMeta(backItemMeta);
            inventory.setItem(45, back);
        }
        if (page < pageCount) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(I18n._("user.info.next_page"));
            nextPage.setItemMeta(nextPageMeta);
            inventory.setItem(53, nextPage);
        }
        List<String> lore = new ArrayList<>();
        ItemStack myItem = new ItemStack(Material.PAPER);
        ItemMeta meta = myItem.getItemMeta();
        meta.setDisplayName(I18n._("user.market.my_items", db.getMarketPlayerItemCount(player), getPlayerSlot(player)));
        lore = new ArrayList<>();
        lore.add(I18n._("user.info.balance", plugin.eco.balance(player)));
        meta.setLore(lore);
        myItem.setItemMeta(meta);
        inventory.setItem(47, myItem);
        viewItem.put(player, list);
        player.openInventory(inventory);
    }

    public static void closeGUI(Player player) {
        if (player.isOnline() && player.getOpenInventory().getTitle().contains(I18n._("user.market.title"))) {
            player.getOpenInventory().close();
        }
        viewPage.remove(player);
        viewItem.remove(player);
        viewSeller.remove(player);
    }

    public static void playSound(Player player, Sound sound) {
        if (plugin.config.marketPlaySound) {
            player.playSound(player.getLocation(), sound, 1, 2);
        }
        return;
    }

    private static String getItemName(ItemStack item) {
        String itemName = "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().getDisplayName();
        }
        if (itemName.length() == 0) {
            itemName = item.getType().name() + ":" + item.getDurability();
        } else {
            itemName += "(" + item.getType().name() + ":" + item.getDurability() + ")";
        }
        return itemName;
    }

    public static void updateAllGUI() {
        for (Player player : viewPage.keySet()) {
            if (player.isOnline() && player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains(I18n._("user.market.title"))) {
                openGUI(player, viewPage.get(player), viewSeller.get(player));
            }
        }
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

    @Override
    public void run() {
        if (plugin.config.market_placement_fee > 0 &&
                System.currentTimeMillis() - plugin.config.market_placement_fee_timestamp >= 86400000) {
            plugin.config.market_placement_fee_timestamp = System.currentTimeMillis();
            int itemCount = db.getMarketItemCount();
            if (itemCount > 0) {
                int fail = 0;
                List<MarketItem> items = db.getMarketItems(0, itemCount, null);
                for (MarketItem item : items) {
                    if (!plugin.eco.withdraw(item.getPlayer(), plugin.config.market_placement_fee)) {
                        fail++;
                        plugin.logger.info(I18n._("log.info.placement_fee_fail",
                                item.getId(), item.getPlayer().getName(), "Not enough money"));
                    }
                }
                plugin.logger.info(I18n._("log.info.placement_fee", itemCount, fail));
            }
        }
    }
}

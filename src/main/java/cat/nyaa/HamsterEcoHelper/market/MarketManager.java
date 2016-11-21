package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Database;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
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

import java.util.*;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.msg;
import static org.bukkit.Bukkit.getServer;

public class MarketManager {
    private static Database db;
    private static HamsterEcoHelper plugin;
    public static HashMap<Player, HashMap<Integer, Integer>> viewItem;
    public static HashMap<Player, Integer> viewPage;
    public static HashMap<Player, UUID> viewSeller;
    public static List<Player> viewMailbox;
    public static long lastBroadcast;
    public static int pageSize = 45;
    public static String market_lore_code = ChatColor.translateAlternateColorCodes('&',"&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r"); 

    public static void init(HamsterEcoHelper pl) {
        plugin = pl;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        db = plugin.database;
        viewItem = new HashMap<>();
        viewPage = new HashMap<>();
        viewSeller = new HashMap<>();
        viewMailbox = new ArrayList<>();
    }

    public static boolean offer(Player player, ItemStack item, double unit_price) {
        if (getPlayerSlot(player) <= db.getMarketPlayerItemCount(player)) {
            return false;
        }
        int id = db.marketOffer(player, item, unit_price);
        plugin.logger.info(I18n.get("internal.info.market_offer", id, getItemName(item), item.getAmount(), unit_price, player.getName()));
        if (plugin.config.marketBroadcast && (System.currentTimeMillis() - lastBroadcast) > (plugin.config.marketBroadcastCooldown * 1000)) {
            lastBroadcast = System.currentTimeMillis();
            new Message("").append(item, I18n.get("user.market.broadcast")).broadcast();
        }
        updateAllGUI();
        return true;
    }

    public static boolean buy(Player player, int itemId, int amount) {
        Database.MarketItem item = getItem(itemId);
        if (item != null && item.getItemStack().getType() != Material.AIR && item.getAmount() > 0) {
            double price = item.getUnitPrice() * amount;
            if (plugin.eco.enoughMoney(player, price) || player.getUniqueId().equals(item.getPlayerId())) {
                Utils.giveItem(player, item.getItemStack(amount));
                plugin.logger.info(I18n.get("internal.info.market_bought", itemId, getItemName(item.getItemStack()), amount, price, player.getName(), item.getPlayerName()));
                if (!player.getUniqueId().equals(item.getPlayerId())) {
                    if (item.getPlayer().isOnline()) {
                        new Message("")
                                .append(item.getItemStack(amount), I18n.get("user.market.someone_bought", player.getName(), price))
                                .send((Player) item.getPlayer());
                    }
                    new Message("")
                            .append(item.getItemStack(amount), I18n.get("user.market.buy_success", item.getPlayerName(), price))
                            .send(player);
                    plugin.eco.withdraw(player, price);
                    plugin.eco.deposit(item.getPlayer(), price);
                }
                db.marketBuy(player, itemId, amount);
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                updateAllGUI();
                return true;
            } else {
                msg(player, "user.warn.no_enough_money");
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

    @Deprecated
    public static boolean addItemToMailbox(Player player, ItemStack item) {
        return db.addItemToMailbox(player, item);
    }

    public static Database.MarketItem getItem(int itemId) {
        return db.getMarketItem(itemId);
    }

    public static void openGUI(Player player, int page, UUID seller) {
        HashMap<Integer, Integer> list = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + I18n.get("user.market.title"));
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
        List<Database.MarketItem> marketItem = db.getMarketItems(offset, MarketManager.pageSize, seller);
        if (marketItem != null) {
            for (int i = 0; i < marketItem.size(); i++) {
                Database.MarketItem mItem = marketItem.get(i);
                list.put(i, mItem.getId());
                ItemMeta meta = mItem.getItemStack().getItemMeta();
                List<String> lore;
                if (meta.hasLore()) {
                    lore = meta.getLore();
                } else {
                    lore = new ArrayList<>();
                }
                lore.add(0,market_lore_code + ChatColor.GREEN + I18n.get("user.market.unit_price", ChatColor.WHITE + "" + mItem.getUnitPrice()));
                lore.add(1, ChatColor.GREEN + I18n.get("user.market.offered", ChatColor.WHITE + mItem.getPlayerName()));
                meta.setLore(lore);
                ItemStack itemStack = mItem.getItemStack();
                itemStack.setItemMeta(meta);
                inventory.setItem(i, itemStack);
            }
        }
        if (page > 1 || seller != null) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backItemMeta = back.getItemMeta();
            backItemMeta.setDisplayName(ChatColor.WHITE + I18n.get("user.info.back"));
            back.setItemMeta(backItemMeta);
            inventory.setItem(45, back);
        }
        if (page < pageCount) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.WHITE + I18n.get("user.info.next_page"));
            nextPage.setItemMeta(nextPageMeta);
            inventory.setItem(53, nextPage);
        }
        List<String> lore = new ArrayList<>();
        /*
        ItemStack mailbox = new ItemStack(Material.CHEST);
        ItemMeta mailboxMeta = mailbox.getItemMeta();
        mailboxMeta.setDisplayName(ChatColor.LIGHT_PURPLE + I18n.get("user.market.mailbox"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + I18n.get("user.market.open_mailbox"));
        mailboxMeta.setLore(lore);
        mailbox.setItemMeta(mailboxMeta);
        inventory.setItem(48, mailbox);
        */
        ItemStack myItem = new ItemStack(Material.PAPER);
        ItemMeta meta = myItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + I18n.get("user.market.my_items") +
                (String.format(" (%s/%s)", db.getMarketPlayerItemCount(player), getPlayerSlot(player))));
        lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + I18n.get("user.info.balance", plugin.eco.balance(player)));
        meta.setLore(lore);
        myItem.setItemMeta(meta);
        inventory.setItem(47, myItem);
        viewItem.put(player, list);
        player.openInventory(inventory);
    }

    public static void closeGUI(Player player) {
        if (player.isOnline() && player.getOpenInventory().getTitle().contains(I18n.get("user.market.title"))) {
            player.getOpenInventory().close();
        }
        viewPage.remove(player);
        viewItem.remove(player);
        viewSeller.remove(player);
    }

    @Deprecated
    public static void openMailbox(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, I18n.get("user.market.mailbox"));
        ItemStack[] mailbox = getMailbox(player);
        if (mailbox != null) {
            inventory.setContents(mailbox);
        }
        player.openInventory(inventory);
        viewMailbox.add(player);
    }

    public static void setMailbox(Player player, ItemStack[] item) {
        db.setMailbox(player, item);
    }

    public static ItemStack[] getMailbox(Player player) {
        return db.getMailbox(player);
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
            if (player.isOnline() && player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains(I18n.get("user.market.title"))) {
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
}

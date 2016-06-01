package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Database;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.msg;
import static org.bukkit.Bukkit.getServer;

public class Market {
    private static Database db;
    private static HamsterEcoHelper plugin;
    public static HashMap<Player, HashMap<Integer, Integer>> viewItem;
    public static HashMap<Player, Integer> viewPage;
    public static HashMap<Player, UUID> viewSeller;
    public static List<Player> viewMailbox;
    public static Economy eco = null;
    public static long lastBroadcast;
    public static int pageSize = 45;

    public static void init(HamsterEcoHelper pl) {
        plugin = pl;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        eco = rsp.getProvider();
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
        db.marketOffer(player, item, unit_price);
        if (plugin.config.marketBroadcast && (System.currentTimeMillis() - lastBroadcast) > (plugin.config.marketBroadcastCooldown * 1000)) {
            lastBroadcast = System.currentTimeMillis();
            Bukkit.broadcastMessage(I18n.get("user.market.broadcast"));
        }
        return true;
    }

    public static boolean buy(Player player, int itemId, int amount) {
        Database.MarketItem item = getItem(itemId);
        if (item != null && item.getItemStack().getType() != Material.AIR && item.getAmount() > 0) {
            double price = item.getUnitPrice() * amount;
            if (eco.has(player, price) || player.getUniqueId().equals(item.getPlayerId())) {
                if (!addItemToMailbox(player, item.getItemStack(amount))) {
                    msg(player, "user.warn.not_enough_space");
                    playSound(player, Sound.BLOCK_FENCE_GATE_OPEN);
                    return false;
                }
                if (!player.getUniqueId().equals(item.getPlayerId())) {
                    eco.withdrawPlayer(player, price);
                    eco.depositPlayer(item.getPlayer(), price);
                }
                db.marketBuy(player, itemId, amount);
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_TOUCH);
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
        if (plugin.config.marketSlot != null) {
            for (String key : plugin.config.marketSlot.getKeys(false)) {
                if (player.hasPermission("heh.offer." + key)) {
                    int tmp = plugin.config.marketSlot.getInt(key, 0);
                    if (tmp > slot) {
                        slot = tmp;
                    }
                }
            }
        }
        return slot;
    }

    public static boolean addItemToMailbox(Player player, ItemStack item) {
        return db.addItemToMailbox(player, item);
    }

    public static Database.MarketItem getItem(int itemId) {
        return db.getMarketItem(itemId);
    }

    public static void view(Player player, int page, UUID seller) {
        HashMap<Integer, Integer> list = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + I18n.get("user.market.title"));
        int pageCount;
        if (seller != null && page >= 1) {
            viewSeller.put(player, seller);
            pageCount = (db.getMarketPlayerItemCount(Bukkit.getOfflinePlayer(seller)) + Market.pageSize - 1) / Market.pageSize;
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
            offset = (page - 1) * (Market.pageSize);
        }
        viewPage.put(player, page);
        List<Database.MarketItem> marketItem = db.getMarketItems(offset, Market.pageSize, seller);
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
                lore.add(0, ChatColor.GREEN + I18n.get("user.market.unit_price", ChatColor.WHITE + "" + mItem.getUnitPrice()));
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
        ItemStack mailbox = new ItemStack(Material.CHEST);
        ItemMeta mailboxMeta = mailbox.getItemMeta();
        mailboxMeta.setDisplayName(ChatColor.LIGHT_PURPLE + I18n.get("user.market.mailbox"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + I18n.get("user.market.open_mailbox"));
        mailboxMeta.setLore(lore);
        mailbox.setItemMeta(mailboxMeta);
        inventory.setItem(48, mailbox);

        ItemStack myItem = new ItemStack(Material.PAPER);
        ItemMeta meta = myItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + I18n.get("user.market.my_items") +
                (String.format(" (%s/%s)", db.getMarketPlayerItemCount(player), getPlayerSlot(player))));
        lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + I18n.get("user.info.balance", ChatColor.WHITE + "" + eco.getBalance(player)));
        meta.setLore(lore);
        myItem.setItemMeta(meta);
        inventory.setItem(47, myItem);
        viewItem.put(player, list);
        player.openInventory(inventory);
    }

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
}

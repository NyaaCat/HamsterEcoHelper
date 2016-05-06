package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.I18n;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class Market {
    private static Database db;
    private static Plugin plugin;
    public static HashMap<Player, HashMap<Integer, Integer>> viewItem;
    public static HashMap<Player, Integer> viewPage;
    public static HashMap<Player, String> viewSeller;
    public static List<Player> viewMailbox;
    public static Economy eco = null;
    public static boolean enableSound;
    public static long broadcastCoolDown;
    public static long lastBroadcast;
    public static boolean enableBroadcast;

    public static void init(Plugin pl) {
        plugin = pl;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        eco = rsp.getProvider();
        db = new SQLite();
        db.init(pl);
        viewItem = new HashMap<>();
        viewPage = new HashMap<>();
        viewSeller = new HashMap<>();
        viewMailbox = new ArrayList<>();
        enableSound = plugin.getConfig().getBoolean("market.playsound", true);
        enableBroadcast = plugin.getConfig().getBoolean("market.broadcast.enable", true);
        broadcastCoolDown = plugin.getConfig().getInt("market.broadcast.cooldown", 120) * 1000;
    }

    public static boolean offer(Player player, ItemStack item, double unit_price) {
        if (getPlayerSlot(player) <= db.getPlayerItemCount(player)) {
            return false;
        }
        db.offer(player, item, unit_price, item.getAmount());
        if (enableBroadcast && System.currentTimeMillis() - lastBroadcast > broadcastCoolDown) {
            lastBroadcast = System.currentTimeMillis();
            Bukkit.broadcastMessage(I18n.get("user.market.broadcast"));
        }
        return true;
    }

    public static boolean buy(Player player, int itemId, int amount) {
        MarketItem item = getItem(itemId);
        if (item != null && item.getItemStack().getType() != Material.AIR && item.getAmount()>0) {
            double price = item.getUnit_price() * amount;
            if (eco.has(player, price) || player.getUniqueId().toString().equals(item.getPlayer_uuid())) {
                if (!addItemToMailbox(player, item.getItemStack(amount))) {
                    player.sendMessage(ChatColor.RED + I18n.get("user.warn.not_enough_space"));
                    playSound(player, Sound.BLOCK_FENCE_GATE_OPEN);
                    return false;
                }
                if (!player.getUniqueId().toString().equals(item.getPlayer_uuid())) {
                    eco.withdrawPlayer(player, price);
                    eco.depositPlayer(item.getPlayer(),price);
                }
                db.buy(player, itemId, amount);
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_TOUCH);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + I18n.get("user.warn.no_enough_money"));
                playSound(player, Sound.ENTITY_ITEM_BREAK);
                return false;
            }
        }
        return false;
    }

    public static int getPlayerSlot(Player player) {
        int slot = 0;
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("market.groups");
        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                if (player.hasPermission("heh.offer." + key)) {
                    int tmp = groups.getInt(key + ".slot", 0);
                    if (tmp > slot) {
                        slot = tmp;
                    }
                }
            }
        }
        return slot;
    }

    public static boolean addItemToMailbox(Player player, ItemStack item) {
        ItemStack[] mailbox = getMailbox(player);
        for (int slot = 0; slot < mailbox.length; slot++) {
            ItemStack tmp = mailbox[slot];
            if (tmp == null || tmp.getType() == Material.AIR) {
                mailbox[slot] = item;
                setMailbox(player, mailbox);
                return true;
            }
        }
        return false;
    }

    public static MarketItem getItem(int itemId) {
        return db.getMarketItem(itemId);
    }

    public static void view(Player player, int page, String seller) {
        HashMap<Integer, Integer> list = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + I18n.get("user.market.title"));
        int pageCount;
        if (seller.length() > 0 && page >= 1) {
            viewSeller.put(player, seller);
            pageCount = (db.getPlayerItemCount(Bukkit.getOfflinePlayer(UUID.fromString(seller))) + 45 - 1) / 45;
        } else {
            viewSeller.put(player, "");
            pageCount = db.getPageCount();
            seller = "";
        }
        int offset = 0;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        if (page > 1) {
            offset = (page - 1) * (45);
        }
        viewPage.put(player, page);
        List<MarketItem> marketItem = db.getItems(offset, 45, seller);
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
                lore.add(0, ChatColor.GREEN + I18n.get("user.market.price", ChatColor.WHITE + "" + (mItem.getUnit_price() * mItem.getAmount())));
                lore.add(1, ChatColor.GREEN + I18n.get("user.market.offered", ChatColor.WHITE + mItem.getPlayerName()));
                meta.setLore(lore);
                ItemStack itemStack = mItem.getItemStack();
                itemStack.setItemMeta(meta);
                inventory.setItem(i, itemStack);
            }
        }
        if (page > 1 || seller.length() > 0) {
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
                (String.format(" (%s/%s)", db.getPlayerItemCount(player), getPlayerSlot(player))));
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
        ItemStack[] mailbox = Market.getMailbox(player);
        if (mailbox != null) {
            inventory.setContents(Market.getMailbox(player));
        }
        player.openInventory(inventory);
        viewMailbox.add(player);
    }

    public static boolean setMailbox(Player player, ItemStack[] item) {
        return db.setMailbox(player, item);
    }

    public static ItemStack[] getMailbox(Player player) {
        return db.getMailbox(player);
    }

    public static void playSound(Player player, Sound sound) {
        if (Market.enableSound) {
            player.playSound(player.getLocation(), sound, 1, 2);
        }
        return;
    }
}

package cat.nyaa.HamsterEcoHelper.signshop;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.Sign;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.SignShopItem;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ShopGUI extends ShopInventoryHolder {
    public static String lore_code = ChatColor.translateAlternateColorCodes('&', "&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r");
    public UUID shopOwner;
    public Sign sign;
    public ShopMode mode;
    public int currentPage = 1;
    public HashMap<Integer, Long> itemsID = new HashMap<>();
    private HamsterEcoHelper plugin;
    private Player player;

    public ShopGUI(HamsterEcoHelper pl, Player player, Sign sign) {
        this.plugin = pl;
        this.player = player;
        shopOwner = sign.getOwner();
        this.sign = sign;
        mode = sign.shopMode;
    }

    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (event.getInventory().getSize() == 54 && slot >= 0 && slot < 54) {
            ItemStack item = event.getInventory().getItem(slot);
            if (slot >= 0 && slot < 45 && item != null && !item.getType().equals(Material.AIR)) {
                this.clickItem(player, event.getRawSlot(), event.isShiftClick());//) {
                return;
            }
            if (event.getRawSlot() == 45 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, this.getCurrentPage() - 1);
            } else if (event.getRawSlot() == 47 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, 1);
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, this.getCurrentPage() + 1);
            } else {
                this.closeGUI(player);
            }
        } else {
            this.closeGUI(player);
        }
    }

    public ItemStack addLore(ItemStack itemStack, double unitPrice) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        if (plugin.signShopManager.getTax() > 0 && mode.equals(ShopMode.SELL)) {
            double tax = (unitPrice / 100) * plugin.signShopManager.getTax();
            lore.add(0, lore_code + ChatColor.RESET + I18n.format("user.market.unit_price_with_tax",
                    unitPrice, tax, plugin.signShopManager.getTax()));
        } else {
            lore.add(0, lore_code + ChatColor.RESET +
                    I18n.format("user.market.unit_price", unitPrice));
        }
        if (isEditMode()) {
            lore.add(1, I18n.format("user.signshop.edit"));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void openGUI(Player player, int page) {
        HashMap<Integer, Long> list = new HashMap<>();
        String title = "";
        if (mode.equals(ShopMode.SELL)) {
            title = I18n.format("user.signshop.buy.title", sign.getPlayer().getName());
        } else if (mode.equals(ShopMode.BUY)) {
            title = I18n.format("user.signshop.sell.title", sign.getPlayer().getName());
        }
        Inventory inventory = plugin.getServer().createInventory(this, 54, title);
        int pageCount;
        pageCount = getPageCount(plugin.signShopManager.getItemCount(shopOwner, mode));
        int offset = 0;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        if (page > 1) {
            offset = (page - 1) * 45;
        }
        setCurrentPage(page);
        List<SignShopItem> shopItems = getShopItems(offset, 45);
        if (shopItems != null) {
            for (int i = 0; i < shopItems.size(); i++) {
                SignShopItem item = shopItems.get(i);
                itemsID.put(i, item.getId());
                inventory.setItem(i, addLore(item.getItem(), item.getUnitPrice()));
            }
        }
        if (page > 1) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backItemMeta = back.getItemMeta();
            backItemMeta.setDisplayName(I18n.format("user.info.back"));
            back.setItemMeta(backItemMeta);
            inventory.setItem(45, back);
        }
        if (page < pageCount) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(I18n.format("user.info.next_page"));
            nextPage.setItemMeta(nextPageMeta);
            inventory.setItem(53, nextPage);
        }
        player.openInventory(inventory);
    }

    public void closeGUI(Player player) {
        player.closeInventory();
    }

    public boolean clickItem(Player player, int slot, boolean shift) {
        long shopItemID = slot;
        int amount = 1;
        if (this.itemsID.containsKey(slot)) {
            shopItemID = this.itemsID.get(slot);
        }
        if (getCurrentPage() > 1) {
            shopItemID = ((getCurrentPage() - 1) * 45) + slot;
        }
        SignShopItem shopItem = plugin.database.getSignShopItem(shopItemID);
        if (shopItem == null || !(shopItem.getAmount() > 0)) {
            player.closeInventory();
            return false;
        }
        if (shift) {
            amount = shopItem.getAmount();
        }
        if (shopItem.getItem().getType() != Material.AIR) {
            if (mode.equals(ShopMode.BUY)) {
                plugin.database.removeSignShopItem(shopItemID);
                this.openGUI(player, this.getCurrentPage());
                return true;
            }
            double price = shopItem.getUnitPrice() * amount;
            double tax = 0.0D;
            if (plugin.signShopManager.getTax() > 0) {
                tax = (price / 100) * plugin.signShopManager.getTax();
            }
            if (plugin.eco.enoughMoney(player, price + tax) || isEditMode()) {
                ItemStack item = shopItem.getItem(amount);
                OfflinePlayer owner = shopItem.getPlayer();
                Optional<Utils.GiveStat> stat = plugin.eco.transaction(player, owner, item, price, tax);
                if (!stat.isPresent()) {
                    new Message("")
                            .append(I18n.format("user.market.buy_fail", owner.getName(), price), item)
                            .send(player);
                    return false;
                }
                shopItem.setAmount(shopItem.getAmount() - amount);
                plugin.database.updateSignShopItem(shopOwner, shopItem);
                plugin.signShopManager.updateGUI(shopOwner, mode);
                player.sendMessage(I18n.format("user.auc.item_given_" + stat.get().name()));
                if (!isEditMode()) {
                    if (owner.isOnline()) {
                        new Message("")
                                .append(I18n.format("user.signshop.buy.notice", player.getName(), price),
                                        item)
                                .send(Bukkit.getPlayer(shopOwner));
                    }
                    new Message("")
                            .append(I18n.format("user.signshop.buy.success", owner.getName(), price + tax),
                                    item).send(player);
                    plugin.logger.info(I18n.format("log.info.signshop_bought",
                            Utils.getItemName(item), amount,
                            price, player.getName(), shopItem.getPlayer().getName(), shopItem.itemID));
                }
                return true;
            } else {
                player.sendMessage(I18n.format("user.warn.no_enough_money"));
                return true;
            }
        }
        player.closeInventory();
        return false;
    }

    public int getPageCount(int itemCount) {
        return (itemCount + 45 - 1) / 45;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public List<SignShopItem> getShopItems(int offset, int limit) {
        List<SignShopItem> tmp = plugin.database.getSignShopItems(shopOwner, mode)
                .stream().skip(offset).limit(limit).collect(Collectors.toList());
        ;
        return tmp;
    }

    public boolean isEditMode() {
        return player.getUniqueId().equals(shopOwner);
    }
}

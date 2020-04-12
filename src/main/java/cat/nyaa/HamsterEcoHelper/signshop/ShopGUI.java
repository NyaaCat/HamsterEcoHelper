package cat.nyaa.HamsterEcoHelper.signshop;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Sign;
import cat.nyaa.HamsterEcoHelper.database.SignShop;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
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

public class ShopGUI extends ShopInventoryHolder {
    public static String lore_code = ChatColor.translateAlternateColorCodes('&', "&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r");
    public UUID shopOwner;
    public Sign sign;
    public ShopMode mode;
    public int currentPage = 1;
    private HamsterEcoHelper plugin;
    private Player player;


    public ShopGUI(HamsterEcoHelper pl, Player player, Sign sign) {
        this.plugin = pl;
        this.player = player;
        shopOwner = sign.owner;
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
        List<ShopItem> shopItems = getShopItems(offset, 45);
        if (shopItems != null) {
            for (int i = 0; i < shopItems.size(); i++) {
                ShopItem item = shopItems.get(i);
                ItemStack stack = addLore(item.getItemStack(item.amount), item.unitPrice);

                inventory.setItem(i, stack);
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
        int itemId = slot;
        int amount = 1;
        if (getCurrentPage() > 1) {
            itemId = ((getCurrentPage() - 1) * 45) + slot;
        }
        SignShop shop = plugin.database.getSignShop(shopOwner);
        List<ShopItem> shopItems = new ArrayList<>();
        shopItems = shop.getItems(mode);
        if (shopItems.size() > itemId) {
            ShopItem shopItem = shopItems.get(itemId);
            if (shopItem == null || !(shopItem.amount > 0)) {
                player.closeInventory();
                return false;
            }
            if (shift) {
                amount = shopItem.amount;
            }
            if (shopItem.getItemStack(1).getType() != Material.AIR) {
                if (mode.equals(ShopMode.BUY)) {
                    shopItem.amount = 0;
                    shopItems.set(itemId, shopItem);
                    shop.setItems(shopItems, ShopMode.BUY);
                    plugin.database.setSignShop(shopOwner, shop);
                    this.openGUI(player, this.getCurrentPage());
                    return true;
                }
                double price = shopItem.unitPrice * amount;
                double tax = 0.0D;
                if (plugin.signShopManager.getTax() > 0) {
                    tax = (price / 100) * plugin.signShopManager.getTax();
                }
                if (plugin.eco.enoughMoney(player, price + tax) || isEditMode()) {
                    OfflinePlayer owner = shop.getPlayer();
                    Optional<MiscUtils.GiveStat> stat = plugin.eco.transaction(player, owner, shopItem.getItemStack(amount), price, tax);
                    if (!stat.isPresent()) {
                        new Message("")
                                .append(I18n.format("user.market.buy_fail", owner.getName(), price), shopItem.getItemStack(amount))
                                .send(player);
                        return false;
                    }
                    shopItem.amount = shopItem.amount - amount;
                    shopItems.set(itemId, shopItem);
                    shop.setItems(shopItems, ShopMode.SELL);
                    plugin.database.setSignShop(shopOwner, shop);
                    plugin.signShopManager.updateGUI(shopOwner, mode);
                    player.sendMessage(I18n.format("user.auc.item_given_" + stat.get().name()));
                    if (!isEditMode()) {
                        new Message("")
                                .append(I18n.format("user.signshop.buy.notice", player.getName(), price),
                                        shopItem.getItemStack(amount))
                                .send(Bukkit.getOfflinePlayer(shopOwner));
                        new Message("")
                                .append(I18n.format("user.signshop.buy.success", owner.getName(), price + tax),
                                        shopItem.getItemStack(amount)).send(player);
                        plugin.logger.info(I18n.format("log.info.signshop_bought",
                                MiscUtils.getItemName(shopItem.getItemStack(amount)), amount,
                                price, player.getName(), shop.getPlayer().getName()));
                    }
                    return true;
                } else {
                    player.sendMessage(I18n.format("user.warn.no_enough_money"));
                    return true;
                }
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

    public List<ShopItem> getShopItems(int offset, int limit) {
        List<ShopItem> list = new ArrayList<>();
        List<ShopItem> tmp = new ArrayList<>();
        tmp = plugin.database.getSignShop(shopOwner).getItems(mode);
        for (int i = offset; i < tmp.size(); i++) {
            list.add(tmp.get(i));
            if (list.size() >= limit) {
                break;
            }
        }
        return list;
    }

    public boolean isEditMode() {
        return player.getUniqueId().equals(shopOwner);
    }
}

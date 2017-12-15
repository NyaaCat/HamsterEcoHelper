package cat.nyaa.HamsterEcoHelper.market;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.signshop.ShopInventoryHolder;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.MarketItem_v2;
import cat.nyaa.nyaacore.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketGUI extends ShopInventoryHolder {
    public int currentPage = 1;
    public UUID seller = null;
    public HashMap<Integer, Long> itemsID = new HashMap<>();
    private HamsterEcoHelper plugin;
    private Player player;

    public MarketGUI(HamsterEcoHelper pl, Player player, UUID seller) {
        this.plugin = pl;
        this.player = player;
        this.seller = seller;
        itemsID = new HashMap<>();
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
                plugin.marketManager.openGUI(player, 1, player.getUniqueId());
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, this.getCurrentPage() + 1);
            } else {
                this.closeGUI(player);
            }
        } else {
            this.closeGUI(player);
        }
    }

    public void openGUI(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(this, 54, I18n.format("user.market.title"));
        int pageCount;
        if (page < 1 && seller != null) {
            page = 1;
            seller = null;
        }
        if (seller != null && page >= 1) {
            pageCount = (plugin.database.getMarketPlayerItemCount(Bukkit.getOfflinePlayer(seller)) + 45 - 1) / 45;
        } else {
            pageCount = (plugin.database.getMarketItemCount() + 45 - 1) / 45;
        }
        int offset = 0;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        if (page > 1) {
            offset = (page - 1) * (45);
        }
        setCurrentPage(page);
        List<MarketItem_v2> items = plugin.database.getMarketItems(offset, 45, seller);
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                MarketItem_v2 mItem = items.get(i);
                itemsID.put(i, mItem.getId());
                ItemStack itemStack = mItem.getItem();
                addLore(mItem.getPlayer(), itemStack, mItem.getUnitPrice());
                inventory.setItem(i, itemStack);
            }
        }
        if (page > 1 || seller != null) {
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
        List<String> lore = new ArrayList<>();
        if (seller == null || !player.getUniqueId().equals(seller)) {
            ItemStack myItem = new ItemStack(Material.PAPER);
            ItemMeta meta = myItem.getItemMeta();
            meta.setDisplayName(I18n.format("user.market.my_items", plugin.database.getMarketPlayerItemCount(player),
                    plugin.marketManager.getPlayerSlot(player)));
            lore = new ArrayList<>();
            lore.add(I18n.format("user.info.balance", plugin.eco.balance(player)));
            meta.setLore(lore);
            myItem.setItemMeta(meta);
            inventory.setItem(47, myItem);
        }
        player.openInventory(inventory);
    }

    public void closeGUI(Player player) {
        player.closeInventory();
    }

    public boolean clickItem(Player player, int slot, boolean shift) {
        long marketItemID = slot;
        int amount = 1;
        if (this.itemsID.containsKey(slot)) {
            marketItemID = this.itemsID.get(slot);
        }
        MarketItem_v2 marketItem = plugin.marketManager.getItem(marketItemID);
        if (marketItem != null && marketItem.getItem().getType() != Material.AIR && marketItem.getAmount() > 0) {
            if (shift) {
                amount = marketItem.getAmount();
            }
            double price = marketItem.getUnitPrice() * amount;
            double tax = 0.0D;
            if (plugin.config.market_tax > 0) {
                tax = (price / 100) * plugin.config.market_tax;
            }
            if (plugin.eco.enoughMoney(player, price + tax) || player.getUniqueId().equals(marketItem.getPlayerId())) {
                ItemStack item = marketItem.getItem(amount);
                Optional<Utils.GiveStat> stat = plugin.eco.transaction(player, marketItem.getPlayer(), item, price, tax);
                if (!stat.isPresent()) {
                    new Message("")
                            .append(I18n.format("user.market.buy_fail", marketItem.getPlayer().getName(), price), item)
                            .send(player);
                    return false;
                }
                plugin.database.marketBuy(marketItemID, amount);
                plugin.marketManager.updateAllGUI();
                player.sendMessage(I18n.format("user.auc.item_given_" + stat.get().name()));
                plugin.logger.info(I18n.format("log.info.market_bought", marketItemID, Utils.getItemName(item),
                        amount, price, player.getName(), marketItem.getPlayer().getName(), marketItem.itemID));
                if (!player.getUniqueId().equals(marketItem.getPlayerId())) {
                    if (marketItem.getPlayer().isOnline()) {
                        new Message("")
                                .append(I18n.format("user.market.someone_bought",
                                        player.getName(), price + tax), item)
                                .send((Player) marketItem.getPlayer());
                    }
                    new Message("")
                            .append(I18n.format("user.market.buy_success", marketItem.getPlayer().getName(), price), item)
                            .send(player);
                }
                plugin.marketManager.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                return true;
            } else {
                player.sendMessage(I18n.format("user.warn.no_enough_money"));
                plugin.marketManager.playSound(player, Sound.ENTITY_ITEM_BREAK);
                return false;
            }
        }
        return false;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public ItemStack addLore(OfflinePlayer player, ItemStack itemStack, double unitPrice) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        if (plugin.config.market_tax > 0) {
            double tax = (unitPrice / 100) * plugin.config.market_tax;
            lore.add(0, MarketManager.market_lore_code + ChatColor.RESET +
                    I18n.format("user.market.unit_price_with_tax",
                            unitPrice, tax, plugin.config.market_tax));
        } else {
            lore.add(0, MarketManager.market_lore_code + ChatColor.RESET +
                    I18n.format("user.market.unit_price",
                            unitPrice));
        }
        lore.add(1, I18n.format("user.market.offered", player.getName()));
        if (this.player.getUniqueId().equals(player.getUniqueId())) {
            lore.add(2, I18n.format("user.signshop.edit"));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}

package cat.nyaa.HamsterEcoHelper.signshop;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.LottoStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.ShopStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.Sign;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.SignShop;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SignShopManager {
    public List<Sign> signLocations = new ArrayList<>();
    public HashMap<Location, Block> attachedBlocks = new HashMap<>();
    private HamsterEcoHelper plugin;

    public SignShopManager(HamsterEcoHelper pl) {
        plugin = pl;
        signLocations = plugin.database.getShopSigns();
        updateAttachedBlocks();
    }

    public static boolean isChest(Block block) {
        if (block != null &&
                (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))) {
            return true;
        }
        return false;
    }

    public static boolean isSign(Block block) {
        if (block != null &&
                (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST))) {
            return true;
        }
        return false;
    }

    public static Block getAttachedBlock(Block block) {
        if (isSign(block)) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign) block.getState().getData();
            return block.getRelative(sign.getAttachedFace());
        }
        return null;
    }

    public boolean isSignShopBlock(Block block) {
        for (Sign sign : signLocations) {
            if (block.getLocation().equals(sign.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAttachedBlock(Block block) {
        return attachedBlocks.containsValue(block);
    }

    public void updateAttachedBlocks() {
        if (signLocations.isEmpty()) {
            return;
        }
        attachedBlocks = new HashMap<>();
        for (Sign sign : signLocations) {
            if (sign.getLocation() != null && isSign(sign.getLocation().getBlock())) {
                attachedBlocks.put(sign.getLocation().clone(), getAttachedBlock(sign.getLocation().getBlock()));
            }
        }
    }

    public int getSlotLimit(Player player) {
        int slot = 0;
        for (String group : plugin.config.signshop_slot_limit.keySet()) {
            int tmp = plugin.config.signshop_slot_limit.get(group);
            if (player.hasPermission("heh.signshop_slot_limit." + group) && tmp > slot) {
                slot = tmp;
            }
        }
        return slot;
    }

    public int getItemCount(Player player) {
        return plugin.database.getSignShop(player.getUniqueId()).getItems(ShopMode.SELL).size() +
                plugin.database.getSignShop(player.getUniqueId()).getItems(ShopMode.BUY).size();
    }

    public int getItemCount(UUID owner, ShopMode mode) {
        List<ShopItem> list = plugin.database.getSignShop(owner).getItems(mode);
        return list.size();
    }

    public int getSignLimit(Player player) {
        int slot = 0;
        for (String group : plugin.config.signshop_sign_limit.keySet()) {
            int tmp = plugin.config.signshop_sign_limit.get(group);
            if (player.hasPermission("heh.signshop_sign_limit." + group) && tmp > slot) {
                slot = tmp;
            }
        }
        return slot;
    }

    public int getSignCount(Player player) {
        int signCount = 0;
        Iterator<Sign> it = signLocations.iterator();
        while (it.hasNext()) {
            Sign sign = it.next();
            if (player.getUniqueId().equals(sign.getOwner())) {
                if (sign.getLocation() != null && isSign(sign.getLocation().getBlock())) {
                    signCount++;
                } else {
                    plugin.logger.info(I18n.format("log.info.signshop_remove", sign.getPlayer().getName(),
                            sign.getShopMode(), sign.getWorld(), sign.getX(), sign.getY(), sign.getZ()));
                    plugin.database.removeShopSign(sign.getWorld(),
                            sign.getX().intValue(), sign.getY().intValue(), sign.getZ().intValue());
                    it.remove();
                }
            }
        }
        return signCount;
    }

    public Sign getSign(Block block) {
        for (Sign s : signLocations) {
            if (block.getLocation().equals(s.getLocation())) {
                return s;
            }
        }
        return null;
    }

    public boolean createShopSign(Player player, Block block, ShopMode mode) {
        if (getSign(block) != null) {
            removeSign(block, player);
        }
        plugin.logger.info(I18n.format("log.info.signshop_create", player.getName(), mode.name(),
                block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        Sign sign = plugin.database.createShopSign(player, block, mode);
        signLocations.add(sign);

        return true;
    }

    public boolean createLottoSign(Player player, Block block, ShopMode mode, double lottoPrice) {
        if (getSign(block) != null) {
            removeSign(block, player);
        }
        plugin.logger.info(I18n.format("log.info.signshop_create_lotto", player.getName(), lottoPrice,
                block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        Sign sign = plugin.database.createLottoSign(player, block, ShopMode.LOTTO, lottoPrice);
        signLocations.add(sign);
        Block attached = getAttachedBlock(block);
        if (attached != null) {
            attachedBlocks.put(block.getLocation().clone(), attached);
        }
        return true;
    }

    public boolean removeSign(Block block, Player player) {
        Iterator<Sign> it = signLocations.iterator();
        while (it.hasNext()) {
            Sign sign = it.next();
            if (block.getLocation().equals(sign.getLocation())) {
                if (plugin.database.removeShopSign(block)) {
                    if (attachedBlocks.containsKey(block.getLocation())) {
                        attachedBlocks.remove(block.getLocation());
                    }
                    OfflinePlayer p = sign.getPlayer();
                    plugin.logger.info(I18n.format("log.info.signshop_remove", p.getName(), sign.getShopMode(),
                            block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
                    if (p.getUniqueId() != player.getUniqueId()) {
                        plugin.logger.info(" by " + player.getName());
                    }
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public void openShopGUI(Player player, Sign sign, int page) {
        ShopGUI shopGUI = new ShopGUI(plugin, player, sign);
        shopGUI.openGUI(player, page);
    }

    public void printItemsList(Player player, Sign sign) {
        List<ShopItem> list = plugin.database.getSignShop(sign.getOwner()).getItems(ShopMode.BUY);
        if (list.isEmpty()) {
            printShopInfo(player, sign);
            player.sendMessage(I18n.format("user.signshop.empty"));
            return;
        }
        player.sendMessage(I18n.format("user.signshop.sell.title", sign.getPlayer().getName()));
        if (getTax() > 0) {
            for (ShopItem item : list) {
                new Message("").append(I18n.format("user.signshop.sell.unit_price_with_tax", item.getUnitPrice(),
                        ((item.getUnitPrice() / 100) * getTax())), item.getItemStack(1)).send(player);
            }
        } else {
            for (ShopItem item : list) {
                new Message("").append(I18n.format("user.signshop.sell.unit_price", item.getUnitPrice()),
                        item.getItemStack(1)).send(player);
            }
        }
    }

    public void addItemToSellList(UUID owner, ItemStack itemStack, double unitPrice) {
        SignShop shop = plugin.database.getSignShop(owner);
        List<ShopItem> list = shop.getItems(ShopMode.SELL);
        list.add(0, new ShopItem(itemStack, itemStack.getAmount(), unitPrice));
        shop.setItems(list, ShopMode.SELL);
        plugin.database.setSignShop(owner, shop);
    }

    public void addItemToBuyList(UUID uuid, ItemStack itemStack, double unitPrice) {
        SignShop shop = plugin.database.getSignShop(uuid);
        List<ShopItem> list = shop.getItems(ShopMode.BUY);
        list.add(0, new ShopItem(itemStack, 1, unitPrice));
        shop.setItems(list, ShopMode.BUY);
        plugin.database.setSignShop(uuid, shop);
    }

    public void addItemToShop(UUID uuid, ItemStack itemStack, int amount, double unitPrice, ShopMode mode) {
        SignShop shop = plugin.database.getSignShop(uuid);
        List<ShopItem> list = shop.getItems(mode);
        list.add(0, new ShopItem(itemStack.clone(), amount, unitPrice));
        shop.setItems(list, mode);
        plugin.database.setSignShop(uuid, shop);
    }

    public boolean sellItemToShop(Player player, ItemStack itemStack, Sign sign) {
        SignShop shop = plugin.database.getSignShop(sign.getOwner());
        List<ShopItem> list = shop.getItems(ShopMode.BUY);
        for (ShopItem shopItem : list) {
            if (shopItem.getItemStack(1).isSimilar(itemStack)) {
                OfflinePlayer shopOwner = shop.getPlayer();
                double price = itemStack.getAmount() * shopItem.getUnitPrice();
                double tax = 0.0D;
                if (getTax() > 0) {
                    tax = (price / 100) * getTax();
                }
                if (plugin.eco.enoughMoney(shopOwner, price)) {
                    ShopStorageLocation chestLoc = plugin.signShopManager.getChestLocation(shopOwner.getUniqueId());
                    Block block = chestLoc.getLocation().getBlock();
                    if (isChest(block) && block.getState() instanceof InventoryHolder) {
                        Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
                        if (InventoryUtils.hasEnoughSpace(inventory, itemStack) &&
                                InventoryUtils.addItem(inventory, itemStack)) {
                            plugin.eco.deposit(player, price - tax);
                            plugin.eco.withdraw(shopOwner, price);
                            if (tax > 0.0D) {
                                plugin.systemBalance.deposit(tax, plugin);
                            }
                            new Message("").append(I18n.format("user.signshop.sell.success",
                                    shopOwner.getName(), price - tax), itemStack).send(player);
                            if (shopOwner.isOnline()) {
                                new Message("").append(I18n.format("user.signshop.sell.notice",
                                        player.getName(), price), itemStack).send(Bukkit.getPlayer(shop.getOwner()));
                            }
                            plugin.logger.info(I18n.format("log.info.signshop_sell", Utils.getItemName(itemStack),
                                    itemStack.getAmount(), price, player.getName(), shopOwner.getName()));
                            return true;
                        }
                    }
                    player.sendMessage(I18n.format("user.signshop.sell.not_enough_space"));
                    return false;
                } else {
                    player.sendMessage(I18n.format("user.signshop.sell.not_enough_money"));
                    return false;
                }
            }
        }
        player.sendMessage(I18n.format("user.signshop.sell.invalid"));
        return false;
    }

    public ShopStorageLocation getChestLocation(UUID uuid) {
        return plugin.database.getChestLocation(uuid);
    }

    public void setChestLocation(UUID uuid, Location location) {
        plugin.database.setChestLocation(uuid, new ShopStorageLocation(uuid, location));
    }

    public int getTax() {
        return plugin.config.signshop_tax;
    }

    public void printShopInfo(Player player, Sign sign) {
        player.sendMessage(I18n.format("user.signshop.print_shop_info", sign.getPlayer().getName(), sign.getShopMode()));
    }

    public ItemStack getLottoItem(Player player, Sign sign) {
        LottoStorageLocation loc = plugin.database.getLottoStorageLocation(sign.getOwner());
        if (loc == null || loc.getLocation() == null || !isChest(loc.getLocation().getBlock())) {
            return null;
        }
        Block block = loc.getLocation().getBlock();
        if (block.getState() instanceof InventoryHolder) {
            Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
            ArrayList<Integer> slot_ids = new ArrayList<>();
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && !item.getType().equals(Material.AIR) && item.getAmount() > 0) {
                    slot_ids.add(i);
                }
            }
            if (!(slot_ids.size() > 0)) {
                return null;
            }
            Collections.shuffle(slot_ids);
            ItemStack item = inventory.getItem(slot_ids.get(0)).clone();
            inventory.setItem(slot_ids.get(0), new ItemStack(Material.AIR));
            return item;
        }
        return null;
    }

    public void closeAllGUI() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null &&
                    player.getOpenInventory().getTopInventory().getHolder() instanceof ShopGUI) {
                player.closeInventory();
            }
        }
    }

    public void updateGUI(UUID owner, ShopMode mode) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null &&
                    player.getOpenInventory().getTopInventory().getHolder() instanceof ShopGUI) {
                ShopGUI shopGUI = ((ShopGUI) player.getOpenInventory().getTopInventory().getHolder());
                if (owner.equals(shopGUI.shopOwner) && mode.equals(shopGUI.mode)) {
                    plugin.signShopManager.openShopGUI(player, shopGUI.sign, shopGUI.currentPage);
                }
            }
        }
    }
}

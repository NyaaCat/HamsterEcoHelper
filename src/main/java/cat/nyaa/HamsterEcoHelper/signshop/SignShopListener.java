package cat.nyaa.HamsterEcoHelper.signshop;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.LottoStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.Sign;
import cat.nyaa.nyaautils.api.events.HamsterEcoHelperTransactionApiEvent;
import cat.nyaa.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class SignShopListener implements Listener {
    public final HashMap<UUID, ShopMode> selectChest = new HashMap<>();
    private final HamsterEcoHelper plugin;
    public HashMap<UUID, Long> antiSpamClick = new HashMap<>();

    public SignShopListener(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ShopGUI) {
            event.setCancelled(true);
            if (event.getAction().equals(InventoryAction.PICKUP_ONE) ||
                    event.getAction().equals(InventoryAction.PICKUP_ALL) ||
                    event.getAction().equals(InventoryAction.PICKUP_HALF) ||
                    event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                Player player = (Player) event.getWhoClicked();
                ((ShopGUI) event.getInventory().getHolder()).onInventoryClick(event);
            } else {
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        if ("[shop]".equalsIgnoreCase(event.getLine(0))) {
            Player player = event.getPlayer();
            Block attached = SignShopManager.getAttachedBlock(event.getBlock());
            if (attached == null || attached.getType().equals(Material.AIR) || SignShopManager.isSign(attached)) {
                player.sendMessage(I18n._("user.signshop.invalid_location"));
                event.setCancelled(true);
                return;
            }
            if (plugin.signShopManager.getSignCount(player) >= plugin.signShopManager.getSignLimit(player)) {
                player.sendMessage(I18n._("user.signshop.create_fail"));
                event.setCancelled(true);
                return;
            }
            if ("BUY".equalsIgnoreCase(event.getLine(1)) && player.hasPermission("heh.signshop.buy")) {
                event.setLine(0, I18n._("user.signshop.sign.line_1"));
                event.setLine(1, I18n._("user.signshop.sign.line_2_buy"));
                plugin.signShopManager.attachedBlocks.put(event.getBlock().getLocation().clone(), attached);
                plugin.signShopManager.createShopSign(player, event.getBlock(), ShopMode.BUY);
                player.sendMessage(I18n._("user.signshop.create_success"));
            } else if ("SELL".equalsIgnoreCase(event.getLine(1)) && player.hasPermission("heh.signshop.sell")) {
                event.setLine(0, I18n._("user.signshop.sign.line_1"));
                event.setLine(1, I18n._("user.signshop.sign.line_2_sell"));
                plugin.signShopManager.attachedBlocks.put(event.getBlock().getLocation().clone(), attached);
                plugin.signShopManager.createShopSign(player, event.getBlock(), ShopMode.SELL);
                player.sendMessage(I18n._("user.signshop.create_success"));
            } else if ("LOTTO".equalsIgnoreCase(event.getLine(1)) && player.hasPermission("heh.signshop.lotto")) {
                String s = event.getLine(3);
                double price = 0.0;
                try {
                    price = Double.parseDouble(new DecimalFormat("#.##").format(Double.parseDouble(s)));
                } catch (IllegalArgumentException ex) {

                }
                if (!(price >= 0.01)) {
                    event.setCancelled(true);
                    player.sendMessage(I18n._("user.error.not_double"));
                    return;
                }
                event.setLine(0, I18n._("user.signshop.sign.line_1"));
                event.setLine(1, I18n._("user.signshop.sign.line_2_lotto"));
                plugin.signShopManager.attachedBlocks.put(event.getBlock().getLocation().clone(), attached);
                plugin.signShopManager.createLottoSign(player, event.getBlock(), ShopMode.LOTTO, price);
                player.sendMessage(I18n._("user.signshop.create_success"));
                LottoStorageLocation loc = plugin.database.getLottoStorageLocation(player.getUniqueId());
                if (loc == null || loc.getLocation() == null) {
                    player.sendMessage(I18n._("user.signshop.lotto.set_storage"));
                }
            } else {
                player.sendMessage(I18n._("user.signshop.sign_invalid"));
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.signShopManager.isSignShopBlock(block) || plugin.signShopManager.isAttachedBlock(block));
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.signShopManager.isSignShopBlock(block) || plugin.signShopManager.isAttachedBlock(block));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (SignShopManager.isSign(block)) {
            Sign sign = plugin.signShopManager.getSign(block);
            if (sign != null) {
                if (sign.getOwner().equals(player.getUniqueId()) || player.hasPermission("heh.admin")) {
                    event.setCancelled(false);
                    plugin.signShopManager.removeSign(block);
                    player.sendMessage(I18n._("user.signshop.break.success"));
                } else {
                    player.sendMessage(I18n._("user.signshop.break.no_permission"));
                    event.setCancelled(true);
                }
            }
        }
        if (plugin.signShopManager.isAttachedBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && selectChest.containsKey(event.getPlayer().getUniqueId())) {
            if (SignShopManager.isChest(block)) {
                Player p = event.getPlayer();
                event.setCancelled(true);
                ShopMode mode = selectChest.get(p.getUniqueId());
                if (mode.equals(ShopMode.LOTTO)) {
                    plugin.database.setLottoStorageLocation(p.getUniqueId(), new LottoStorageLocation(p.getUniqueId(), block.getLocation()));
                    event.getPlayer().sendMessage(I18n._("user.signshop.lotto.set_success"));
                } else {
                    plugin.signShopManager.setChestLocation(p.getUniqueId(), block.getLocation());
                    event.getPlayer().sendMessage(I18n._("user.signshop.storage.set_success"));
                }
            }
            selectChest.remove(event.getPlayer().getUniqueId());
            return;
        }
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) &&
                (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST))) {
            Player player = event.getPlayer();
            Sign sign = plugin.signShopManager.getSign(block);
            if (sign != null) {
                event.setCancelled(true);
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && player.getUniqueId().equals(sign.getOwner())) {
                    event.setCancelled(false);
                    return;
                }
                if (ShopMode.SELL.equals(sign.shopMode)) {
                    plugin.signShopManager.openShopGUI(player, sign, 1);
                } else if (ShopMode.BUY.equals(sign.shopMode)) {
                    if (sign.getOwner().equals(player.getUniqueId())) {
                        plugin.signShopManager.openShopGUI(player, sign, 1);
                        return;
                    } else {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item == null || item.getType().equals(Material.AIR) || !(item.getAmount() > 0)) {
                            plugin.signShopManager.printItemsList(player, sign);
                        } else {
                            ItemStack itemStack = item.clone();
                            int amount = 1;
                            if (player.isSneaking()) {
                                amount = item.getAmount();
                            }
                            itemStack.setAmount(amount);
                            if (plugin.signShopManager.sellItemToShop(player, itemStack, sign)) {
                                if (item.getAmount() - amount > 0) {
                                    item.setAmount(item.getAmount() - amount);
                                    player.getInventory().setItemInMainHand(item);
                                } else {
                                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                }
                            }
                        }
                    }
                } else if (ShopMode.LOTTO.equals(sign.shopMode)) {
                    if (sign.getOwner().equals(player.getUniqueId())) {
                        event.setCancelled(false);
                        return;
                    } else {
                        if (antiSpamClick.containsKey(player.getUniqueId()) &&
                                System.currentTimeMillis() - antiSpamClick.get(player.getUniqueId()) <= 1000) {
                            return;
                        }
                    }
                    antiSpamClick.put(player.getUniqueId(), System.currentTimeMillis());
                    double price = sign.getLotto_price();
                    if (price > 0.0D) {
                        if (plugin.eco.enoughMoney(player, price)) {
                            ItemStack item = plugin.signShopManager.getLottoItem(player, sign);
                            if (item == null) {
                                player.sendMessage(I18n._("user.signshop.empty"));
                                return;
                            } else {
                                double tax = 0.0D;
                                if (plugin.signShopManager.getTax() > 0) {
                                    tax = (price / 100) * plugin.signShopManager.getTax();
                                    HamsterEcoHelperTransactionApiEvent event1 = new HamsterEcoHelperTransactionApiEvent(tax);
                                    plugin.getServer().getPluginManager().callEvent(event1);
                                }

                                plugin.eco.withdraw(player, price);
                                plugin.eco.deposit(sign.getPlayer(), price - tax);
                                Utils.giveItem(player, item);
                                OfflinePlayer owner = sign.getPlayer();
                                new Message("").append(item, I18n._("user.signshop.lotto.success",
                                        price, owner.getName())).send(player);
                                plugin.logger.info(I18n._("log.signshop_lotto",Utils.getItemName(item),
                                        item.getAmount(),price,player.getName(),owner.getName()));
                                if (owner.isOnline()) {
                                    if (tax > 0.0D) {
                                        new Message("").append(item, I18n._("user.signshop.lotto.notice_with_tax",
                                                player.getName(), price - tax, tax)).send(Bukkit.getPlayer(sign.getOwner()));
                                    } else {
                                        new Message("").append(item, I18n._("user.signshop.lotto.notice",
                                                player.getName(), price)).send(Bukkit.getPlayer(sign.getOwner()));
                                    }
                                }
                            }
                        } else {
                            player.sendMessage(I18n._("user.warn.no_enough_money"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.signShopManager.isSignShopBlock(event.getBlock()) || plugin.signShopManager.isAttachedBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.signShopManager.isAttachedBlock(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.signShopManager.isAttachedBlock(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.getOpenInventory() != null &&
                player.getOpenInventory().getTopInventory() != null &&
                player.getOpenInventory().getTopInventory().getHolder() instanceof ShopGUI) {
            player.closeInventory();
        }
    }
}
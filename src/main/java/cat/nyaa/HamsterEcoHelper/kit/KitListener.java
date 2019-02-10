package cat.nyaa.HamsterEcoHelper.kit;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Kit;
import cat.nyaa.HamsterEcoHelper.database.KitRecord;
import cat.nyaa.HamsterEcoHelper.database.KitSign;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopManager;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class KitListener implements Listener {

    private final HamsterEcoHelper plugin;

    public KitListener(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignCreate(SignChangeEvent event) {
        String line0 = event.getLine(0);
        if (line0 != null && event.getPlayer().hasPermission("heh.kit.admin")) {
            KitSign.SignType type = null;
            if (line0.equalsIgnoreCase("[kit]")) {
                type = KitSign.SignType.BUY;
            } else if (line0.equalsIgnoreCase("[resetkit]")) {
                type = KitSign.SignType.RESET;
            } else {
                return;
            }
            Player player = event.getPlayer();
            String kitName = event.getLine(1);
            if (plugin.kitManager.getKit(kitName) == null) {
                player.sendMessage(I18n.format("user.kit.not_exist", kitName));
                return;
            }
            String desc = event.getLine(2);
            String priceStr = event.getLine(3);
            int price = -1;
            if (priceStr != null) {
                try {
                    price = Integer.parseInt(priceStr);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
            if (price < 0) {
                player.sendMessage(I18n.format("user.error.not_int"));
                return;
            }
            plugin.kitManager.createKitSign(event.getBlock(), event.getLine(1), price, type);
            event.setLine(0, I18n.format("user.kit.sign.line_0." + type.name()));
            event.setLine(1, ChatColor.translateAlternateColorCodes('&', kitName));
            event.setLine(2, ChatColor.translateAlternateColorCodes('&', desc));
            event.setLine(3, String.valueOf(price));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (SignShopManager.isSign(block)) {
            KitSign kitSign = plugin.kitManager.getKitSign(block);
            if (kitSign != null) {
                if (!player.hasPermission("heh.kit.admin")) {
                    event.setCancelled(true);
                } else {
                    plugin.kitManager.removeKitSign(block);
                    player.sendMessage(I18n.format("user.kit.sign.remove", kitSign.kitName));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasBlock() && SignShopManager.isSign(e.getClickedBlock())) {
            Player player = e.getPlayer();
            if (player.hasPermission("heh.kit.user")) {
                KitSign kitSign = plugin.kitManager.getKitSign(e.getClickedBlock());
                if (kitSign != null) {
                    Kit kit = plugin.kitManager.getKit(kitSign.kitName);
                    if (kit != null) {
                        boolean hasRecord = plugin.database.getKitRecord(kit.id, player) != null;
                        if (!hasRecord && kitSign.type == KitSign.SignType.RESET) {
                            player.sendMessage(I18n.format("user.kit.sign.reset.not_exist"));
                            return;
                        } else if (hasRecord && kitSign.type == KitSign.SignType.BUY) {
                            player.sendMessage(I18n.format("user.kit.sign.buy.error"));
                            return;
                        }
                        boolean isFree = kitSign.cost < 0.01D;
                        if (!isFree) {
                            if (!VaultUtils.enoughMoney(player, kitSign.cost)) {
                                player.sendMessage(I18n.format("user.warn.no_enough_money"));
                                return;
                            }
                        }
                        boolean success = false;
                        if (kitSign.type == KitSign.SignType.RESET) {
                            KitRecord r = plugin.database.getKitRecord(kitSign.kitName, player);
                            if (r == null) {
                                player.sendMessage(I18n.format("user.kit.sign.reset.not_exist"));
                            } else {
                                success = true;
                                plugin.database.removeKitRecord(kitSign.kitName, player);
                                player.sendMessage(I18n.format("user.kit.sign.reset.success"));
                            }
                        } else {
                            if (InventoryUtils.addItems(player.getInventory(), kit.getItems())) {
                                plugin.database.addKitRecord(kit.id, player);
                                player.sendMessage(I18n.format("user.kit.sign.buy.success", kit.id, kitSign.cost));
                                success = true;
                            } else {
                                player.sendMessage(I18n.format("user.kit.not_enough_space"));
                            }
                        }
                        if (success && !isFree) {
                            VaultUtils.withdraw(player, kitSign.cost);
                            if (plugin.systemBalance.isEnabled()) {
                                plugin.systemBalance.deposit(kitSign.cost, plugin);
                            }
                        }
                    }

                }
            }
        }
    }
}


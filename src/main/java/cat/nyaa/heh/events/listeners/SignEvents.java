package cat.nyaa.heh.events.listeners;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopManager;
import cat.nyaa.heh.business.signshop.SignShopSell;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SignEvents implements Listener {
    @EventHandler
    public void onClickSign(PlayerInteractEvent event){
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)){
            return;
        }
        if (!SignShopManager.getInstance().isSignShop((Sign) clickedBlock.getState())){
            return;
        }
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(clickedBlock.getLocation());
        if (shopAt instanceof SignShopBuy){
            UUID owner = shopAt.getOwner();
            String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(owner).getName();
            new Message(I18n.format("shop.buy.info.owner", name)).send(event.getPlayer());
            shopAt.getItems().stream().forEach(shopItem -> {
                ItemStack model = shopItem.getModel();
                new Message("").append(I18n.format("shop.buy.info.item"), model).send(event.getPlayer());
            });
            return;
        }
        SignShopGUI gui = shopAt.newGUI();
        gui.open(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if (!(event.getBlock().getState() instanceof Sign)){
            return;
        }
        Sign sign = (Sign) event.getBlock().getState();
        if (SignShopManager.getInstance().isSignShop(sign)){
            event.setCancelled(true);
            BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(sign.getLocation());
            if (event.getPlayer().getUniqueId().equals(shopAt.getOwner())){
                new Message(I18n.format("shop.sign.break")).send(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        if (!(event.getBlock().getState() instanceof Sign)){
            return;
        }
        Sign sign = (Sign) event.getBlock().getState();
        if (SignShopManager.getInstance().isSignShop(sign)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)){
            return;
        }

        Sign sign = ((Sign) clickedBlock.getState());

        BaseSignShop shop = SignShopManager.getInstance().getShopAt(sign.getLocation());
        if (!(shop instanceof SignShopBuy)){
            return;
        }
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(shop);
        signShopGUI.refreshGUI();
        signShopGUI.open(event.getPlayer());
    }
}

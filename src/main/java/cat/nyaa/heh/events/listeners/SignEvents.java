package cat.nyaa.heh.events.listeners;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopLotto;
import cat.nyaa.heh.business.signshop.SignShopManager;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.utils.ClickUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SignEvents implements Listener {

    @EventHandler
    public void onLeftClickSign(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)){
            return;
        }
        if (!SignShopManager.getInstance().isSignShop((Sign) clickedBlock.getState())){
            return;
        }
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(clickedBlock.getLocation());
        if (!shopAt.isSignExist()){
            shopAt.loadSign();
        }
        if (!shopAt.isSignExist()){
            new Message(I18n.format("sign.error.invalid_sign")).send(event.getPlayer());
            return;
        }
        if (!(shopAt instanceof SignShopBuy) || shopAt.getOwner().equals(event.getPlayer().getUniqueId())){
            return;
        }
        //todo cache this
        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if(itemInMainHand.getType().isAir()){
            return;
        }
        shopAt.loadItems();
        List<ShopItem> items = shopAt.getItems();
        ShopItem shopItem1 = items.stream().filter(shopItem -> isValidItem(shopItem, itemInMainHand)).findFirst().orElse(null);
        if (shopItem1 == null){
            return;
        }
        int sellAmount = 1;
        if (player.isSneaking()){
            sellAmount = itemInMainHand.getAmount();
        }
        boolean success = shopAt.doBusiness(player, shopItem1, sellAmount);
        if(success){
            itemInMainHand.setAmount(Math.max(0, itemInMainHand.getAmount() - sellAmount));
        }
    }

    public boolean isValidItem(ShopItem shopItem, ItemStack sellItem) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.itemTemplate = shopItem.getItemStack();
        return itemMatcher.matches(sellItem);
    }

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
        if (!shopAt.isSignExist()){
            shopAt.loadSign();
        }
        if (!shopAt.isSignExist()){
            new Message(I18n.format("sign.error.invalid_sign")).send(event.getPlayer());
            return;
        }
        if(shopAt instanceof SignShopLotto){
            SignShopLotto shopAt1 = (SignShopLotto) shopAt;
            String name = SystemAccountUtils.isSystemAccount(shopAt.getOwner()) ? SystemAccountUtils.getSystemName()
                    : Bukkit.getOfflinePlayer(shopAt.getOwner()).getName();

            ClickUtils clicker = ClickUtils.get("heh_lotto");
            UUID uniqueId = event.getPlayer().getUniqueId();
            boolean firstClick = !clicker.isMultiClick(uniqueId);
            clicker.click(uniqueId, 200);
            if (firstClick) {
                new Message(I18n.format("shop.sign.lotto.confirm", name, shopAt1.getPrice())).send(event.getPlayer());
                return;
            }
            new Message(I18n.format("shop.sign.lotto.started")).send(event.getPlayer());
            shopAt1.doBusiness(event.getPlayer(), null, 1);
            return;
        }
        if (shopAt instanceof SignShopBuy){
            UUID owner = shopAt.getOwner();
            String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(owner).getName();
            new Message(I18n.format("shop.buy.info.owner", name)).send(event.getPlayer());
            shopAt.loadItems();
            shopAt.getItems().stream().forEach(shopItem -> {
                ItemStack model = shopItem.getModel();
                new Message("").append(I18n.format("shop.buy.info.item", shopItem.getUnitPrice()), model).send(event.getPlayer());
            });
            return;
        }
        SignShopGUI gui = shopAt.newGUI();
        gui.open(event.getPlayer());
    }

    private static List<BlockFace> blockFaces = Arrays.asList(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.EAST,
            BlockFace.UP
    );

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        if (!(event.getBlock().getState() instanceof Sign)){
            boolean hasSign = blockFaces.stream()
                    .map(block::getRelative)
                    .filter(block1 -> block1.getState() instanceof Sign)
                    .filter(block1 -> {
                        Block relative = block1.getRelative(getFacing(block1).getOppositeFace());
                        return relative.equals(block);
                    })
                    .anyMatch(this::isShopSign);
            if (hasSign){
                event.setCancelled(true);
            }
            return;
        }
        if (isShopSign(block)){
            event.setCancelled(true);
            BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(block.getLocation());
            if (event.getPlayer().getUniqueId().equals(shopAt.getOwner())){
                new Message(I18n.format("shop.sign.break")).send(event.getPlayer());
                SignShopManager.getInstance().removeShopAt(shopAt);
                event.setCancelled(false);
            }
        }
    }

    public static BlockFace getFacing(Block block) {
        BlockData data = block.getBlockData();
        BlockFace f = null;
        if (data instanceof Directional && data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged()) {
            String str = ((Directional) data).toString();
            if (str.contains("facing=west")) {
                f = BlockFace.WEST;
            } else if (str.contains("facing=east")) {
                f = BlockFace.EAST;
            } else if (str.contains("facing=south")) {
                f = BlockFace.SOUTH;
            } else if (str.contains("facing=north")) {
                f = BlockFace.NORTH;
            }
        } else if (data instanceof Directional) {
            f = ((Directional) data).getFacing();
        }
        return f;
    }

    private boolean isShopSign(Block block1) {
        Sign sign = (Sign) block1.getState();
        return SignShopManager.getInstance().isSignShop(sign);
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

}

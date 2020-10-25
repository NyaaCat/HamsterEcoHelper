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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.*;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SignEvents implements Listener {

    Cache<Location, BaseSignShop> signShopCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @EventHandler
    public void onLeftClickSign(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)){
            return;
        }
        if (!SignShopManager.getInstance().isSignShop(clickedBlock)){
            return;
        }

        Location location = clickedBlock.getLocation();

        BaseSignShop shopAt = signShopCache.getIfPresent(location);
        if(shopAt == null){
            shopAt = SignShopManager.getInstance().getShopAt(location);
            signShopCache.put(location, shopAt);
        }
        if (!(shopAt instanceof SignShopBuy) || shopAt.getOwner().equals(event.getPlayer().getUniqueId())){
            return;
        }
        if (!shopAt.isSignExist()){
            shopAt.loadSign();
        }
        if (!shopAt.isSignExist()){
            new Message(I18n.format("sign.error.invalid_sign")).send(event.getPlayer());
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
        }else{
            new Message(I18n.format("shop.sign.sell.error_transaction_fail")).send(player);
        }
    }

    public boolean isValidItem(ShopItem shopItem, ItemStack sellItem) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.requireExact = true;
        ItemStack itemStack = shopItem.getItemStack();
        itemMatcher.itemTemplate = itemStack;
        boolean matches = itemMatcher.matches(sellItem);
        //exact match
        if (matches){
            return true;
        }
        itemMatcher.requireExact = false;
        matches = itemMatcher.matches(sellItem);
        //simple match
        if (!matches) {
            return false;
        }
        //painful meta match
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            ItemMeta itemMeta1 = sellItem.getItemMeta();
            if (itemMeta instanceof BlockStateMeta) {
                if (!(itemMeta1 instanceof BlockStateMeta)){
                    return false;
                }
                BlockState blockState = ((BlockStateMeta) itemMeta).getBlockState();
                BlockState blockState1 = ((BlockStateMeta) itemMeta1).getBlockState();
                if (blockState instanceof Container){
                    if (!(blockState1 instanceof Container)) {
                        matches = false;
                    } else {
                        matches = matchContainer(((Container) blockState), ((Container) blockState1));
                    }
                }else {
                    matches = itemMeta.equals(itemMeta1);
                }
            }
            else if (itemMeta instanceof BookMeta){
                if(!(itemMeta1 instanceof BookMeta)){
                    return false;
                }
                String title = ((BookMeta) itemMeta).getTitle();
                String title1 = ((BookMeta) itemMeta1).getTitle();
                if (!Objects.equals(title, title1)){
                    return false;
                }
                String author = ((BookMeta) itemMeta).getAuthor();
                String author1 = ((BookMeta) itemMeta1).getAuthor();
                if (!Objects.equals(author, author1)){
                    return false;
                }
                BookMeta.Generation generation = ((BookMeta) itemMeta).getGeneration();
                BookMeta.Generation generation1 = ((BookMeta) itemMeta).getGeneration();
                if (!Objects.equals(generation, generation1)){
                    return false;
                }
                int pageCount = ((BookMeta) itemMeta).getPageCount();
                int pageCount1 = ((BookMeta) itemMeta).getPageCount();
                if (pageCount != pageCount1){
                    return false;
                }
                List<String> pages = ((BookMeta) itemMeta).getPages();
                List<String> pages1 = ((BookMeta) itemMeta).getPages();
                matches = matchPage(pages, pages1);
            }
            else {
                matches = Objects.equals(itemMeta, itemMeta1);
            }
        }
        return matches;
    }

    private boolean matchPage(List<String> pages, List<String> pages1) {
        for (int i = 0; i < pages.size(); i++) {
            String s = pages.get(i);
            String s1 = pages1.get(i);
            if (!Objects.equals(s, s1)){
                return false;
            }
        }
        return true;
    }

    private boolean matchContainer(Container container, Container container1) {
        ItemStack[] contents = container.getInventory().getContents();
        ItemStack[] contents1 = container1.getInventory().getContents();
        boolean matches = true;
        if (contents.length != contents1.length) {
            return false;
        }
        BasicItemMatcher matcher = new BasicItemMatcher();
        for (int i = 0; i < contents.length; i++) {
            ItemStack content = contents[i];
            ItemStack content1 = contents1[i];
            matcher.itemTemplate = content;
            if (content == null || content1 == null){
                matches = content1 == null && content == null;
            }else {
                matches = matcher.matches(content1);
            }
            if (!matches)break;
        }
        return matches;
    }

    @EventHandler
    public void onClickSign(PlayerInteractEvent event){
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)){
            return;
        }
        if (!SignShopManager.getInstance().isSignShop(clickedBlock)){
            return;
        }
        Location location = clickedBlock.getLocation();
        BaseSignShop shopAt = signShopCache.getIfPresent(location);
        if(shopAt == null){
            shopAt = SignShopManager.getInstance().getShopAt(location);
            signShopCache.put(location, shopAt);
        }
        if (shopAt.getOwner().equals(event.getPlayer().getUniqueId())){
            return;
        }
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
        return SignShopManager.getInstance().isSignShop(block1);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        if (!(event.getBlock().getState() instanceof Sign)){
            return;
        }
        if (SignShopManager.getInstance().isSignShop(event.getBlock())){
            event.setCancelled(true);
        }
    }

}

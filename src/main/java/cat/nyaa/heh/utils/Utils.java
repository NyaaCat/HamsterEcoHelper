package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.nyaacore.BasicItemMatcher;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.crafter.mc.lockettepro.LockettePro;
import me.crafter.mc.lockettepro.LocketteProAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static TaskChainFactory factory;

    public static String colored(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static TaskChain<?> newChain(){
        if (factory == null) {
            factory = BukkitTaskChainFactory.create(HamsterEcoHelper.plugin);
        }
        return factory.newChain();
    }

    public static String replacePlaceHolder(Map<String, String> placeHolderMap, String format) {
        return new StrSubstitutor(placeHolderMap, "{", "}", '\\').replace(format);
    }

    public static <T> T randomSelect(Collection<T> collection){
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int size = collection.size();
        int rnd = ThreadLocalRandom.current().nextInt(size);
        return collection.stream().skip(rnd).findFirst().orElse(null);
    }

    public static TextComponent newMessageButton(String buttonText, HoverEvent hoverEvent, ClickEvent clickEvent){
        TextComponent button = new TextComponent(buttonText);
        button.setHoverEvent(hoverEvent);
        button.setClickEvent(clickEvent);
        return button;
    }

    public static BlockFace getRelativeChestFace(Block block) {
        Chest chest = (Chest) block.getBlockData();
        BlockFace face = getFacing(block);
        BlockFace relativeFace = null;
        if (chest.getType() == Chest.Type.LEFT) {
            if (face == BlockFace.NORTH) {
                relativeFace = BlockFace.EAST;
            } else if (face == BlockFace.SOUTH) {
                relativeFace = BlockFace.WEST;
            } else if (face == BlockFace.WEST) {
                relativeFace = BlockFace.NORTH;
            } else if (face == BlockFace.EAST) {
                relativeFace = BlockFace.SOUTH;
            }
        } else if (chest.getType() == Chest.Type.RIGHT) {
            if (face == BlockFace.NORTH) {
                relativeFace = BlockFace.WEST;
            } else if (face == BlockFace.SOUTH) {
                relativeFace = BlockFace.EAST;
            } else if (face == BlockFace.WEST) {
                relativeFace = BlockFace.SOUTH;
            } else if (face == BlockFace.EAST) {
                relativeFace = BlockFace.NORTH;
            }
        }
        return relativeFace;
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

    public static boolean isLocked(Block block){
        return LocketteProAPI.isLocked(block);
    }

    public static boolean isValidItem(ShopItem shopItem, ItemStack sellItem) {
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
        return matches;
    }

    private static boolean matchPage(List<String> pages, List<String> pages1) {
        for (int i = 0; i < pages.size(); i++) {
            String s = pages.get(i);
            String s1 = pages1.get(i);
            if (!Objects.equals(s, s1)){
                return false;
            }
        }
        return true;
    }

    private static boolean matchContainer(Container container, Container container1) {
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
}

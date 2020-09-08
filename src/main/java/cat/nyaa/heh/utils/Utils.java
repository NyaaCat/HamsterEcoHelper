package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Chest;

import java.util.Collection;
import java.util.Map;
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

    public TextComponent newMessageButton(String buttonText, HoverEvent hoverEvent, ClickEvent clickEvent){
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
}

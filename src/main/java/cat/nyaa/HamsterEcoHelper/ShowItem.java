package cat.nyaa.HamsterEcoHelper;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ShowItem {
    static ItemStack item = null;
    Plugin plugin = null;
    String reset = ((char) 167) + "r";
    String bold = ((char) 167) + "l";


    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String getJson() {
        JSONObject json = new JSONObject();
        HashMap hoverEvent = new HashMap();
        hoverEvent.put("action", "show_item");
        String value = "";
        value += "id:\"minecraft:" + item.getType().name().toLowerCase() + "\",";
        value += "Damage:" + item.getDurability() + "s";
        String tag = "";

        if (item.getEnchantments().size() > 0) {
            String enchant = "";
            for (Enchantment e : item.getEnchantments().keySet()) {
                enchant += enchant.length() > 0 ? "," : "";
                enchant += "{id:" + e.getId() + ",lvl:" + item.getEnchantmentLevel(e) + "}";
            }
            tag += "ench:[" + enchant + "]";

        }
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            tag += tag.length() > 0 ? "," : "";
            String enchant = "";
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            for (Enchantment e : meta.getStoredEnchants().keySet()) {
                enchant += enchant.length() > 0 ? "," : "";
                enchant += "{id:" + e.getId() + ",lvl:" + meta.getStoredEnchantLevel(e) + "}";
            }
            tag += "StoredEnchantments:[" + enchant + "]";
        }

        if (item.getType().equals(org.bukkit.Material.WRITTEN_BOOK)) {
            BookMeta book = (BookMeta) item.getItemMeta();
            tag += tag.length() > 0 ? "," : "";
            tag += "author:\"" + book.getAuthor() + "\",title:" + JSONValue.toJSONString(book.getTitle());
        }

        if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore()) {
            String display = "";
            tag += tag.length() > 0 ? "," : "";
            if (item.getItemMeta().hasDisplayName()) {
                display += "Name:" + JSONValue.toJSONString(item.getItemMeta().getDisplayName());
            }
            if (item.getItemMeta().hasLore()) {
                display += display.length() > 0 ? "," : "";
                String lore = "";
                for (String line : item.getItemMeta().getLore()) {
                    lore += lore.length() > 0 ? "," : "";
                    lore += JSONValue.toJSONString(line);
                }
                display += "Lore:[" + lore + "]";

            }
            tag += "display:{" + display + "}";
        }
        value += tag.length() > 0 ? ",tag:{" + tag + "}" : "";
        hoverEvent.put("value", "{" + value + "}");
        if (item.getItemMeta().hasDisplayName()) {
            json.put("text", item.getItemMeta().getDisplayName());
        } else {
            json.put("text", item.getData().getItemType().name());
        }
        json.put("hoverEvent", hoverEvent);
        return "[{\"text\":\"" + bold + "item: \"}," + json.toString() + ",{\"text\":\"(" + item.getType().name() + ")\"}]";
    }

    public boolean sendToPlayer(Player player, String message) {
        if (item == null || item.equals(Material.AIR)) {
            return false;
        }
        //player.sendMessage(message);
        PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
        chat.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean sendToPlayer(Player player) {
        return sendToPlayer(player, getJson());
    }

}

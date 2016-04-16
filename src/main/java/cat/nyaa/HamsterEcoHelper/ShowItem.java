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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.bukkit.Bukkit.getOnlinePlayers;

public class ShowItem {
    private ItemStack item = null;
    String reset = ((char) 167) + "r";
    String bold = ((char) 167) + "l";
    private String message = "{item}";

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
    }

    private String getJSONMessage() {
        JSONObject json = new JSONObject();
        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", "show_item");
        Tag value = new Tag();
        value.put("id", "minecraft:" + item.getType().name().toLowerCase(), true);
        value.put("Damage", item.getDurability() + "s", false);
        Tag tag = new Tag();
        if (item.getEnchantments().size() > 0) {
            ArrayList<String> enchant = new ArrayList();
            for (Enchantment e : item.getEnchantments().keySet()) {
                enchant.add("{id:" + e.getId() + ",lvl:" + item.getEnchantmentLevel(e) + "}");
            }
            tag.put("ench", enchant, false);
        }

        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            ArrayList<String> enchant = new ArrayList();
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            for (Enchantment e : meta.getStoredEnchants().keySet()) {
                enchant.add("{id:" + e.getId() + ",lvl:" + meta.getStoredEnchantLevel(e) + "}");
            }
            tag.put("StoredEnchantments", enchant, false);
        }

        if (item.getType().equals(org.bukkit.Material.WRITTEN_BOOK)) {
            BookMeta book = (BookMeta) item.getItemMeta();
            if (book.hasAuthor()) {
                tag.put("author", book.getAuthor(), true);
            }
            if (book.hasTitle()) {
                tag.put("title", book.getTitle(), true);
            }
        }

        if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore()) {
            Tag display = new Tag();

            if (item.getItemMeta().hasDisplayName()) {
                display.put("Name", item.getItemMeta().getDisplayName(), true);
            }

            if (item.getItemMeta().hasLore()) {
                display.put("Lore", item.getItemMeta().getLore(), true);
            }
            tag.put("display", display.toString(), false);
        }
        value.put("tag", tag.toString(), false);
        long time = System.currentTimeMillis();
        hoverEvent.put("value", time);
        json.put("hoverEvent", hoverEvent);
        if (item.getItemMeta().hasDisplayName()) {
            json.put("text", message.replace("{amount}", String.valueOf(
                    item.getAmount())).replace("{item}", reset + item.getItemMeta().getDisplayName() +
                    reset + "(" + item.getData().getItemType().name() + ")"));
        } else {
            json.put("text", message.replace("{amount}", String.valueOf(
                    item.getAmount())).replace("{item}", reset + item.getData().getItemType().name() + reset));
        }
        return json.toString().replace(String.valueOf(time), JSONValue.toJSONString(value.toString()));
    }

    private boolean sendJSONMessage(Player player, String JSONMessage) {
        if (item == null || item.equals(Material.AIR)) {
            return false;
        }
        PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
        chat.getChatComponents().write(0, WrappedChatComponent.fromJson(JSONMessage));
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean sendToPlayer(Player player) {
        return sendJSONMessage(player, getJSONMessage());
    }

    public void broadcast() {
        String JSONMessage = getJSONMessage();
        for (Player p : getOnlinePlayers()) {
            sendJSONMessage(p, JSONMessage);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

class Tag {
    private HashMap<String, String> map = new HashMap();

    public void put(String key, String value, boolean escape) {
        if (escape) {
            map.put(key, JSONValue.toJSONString(value));
        } else {
            map.put(key, value);
        }
    }

    public void put(String key, List<String> value, boolean escape) {
        String str = "";
        Iterator<String> iterator = value.iterator();
        while (iterator.hasNext()) {
            String v = iterator.next();
            str += str.length() > 0 ? "," : "";
            if (escape) {
                str += JSONValue.toJSONString(v);
            } else {
                str += v;
            }
        }
        map.put(key, "[" + str + "]");
    }

    public String toString() {
        String JSON = "";
        for (String key : map.keySet()) {
            JSON += JSON.length() > 0 ? "," : "";
            System.out.println(key + map.get(key));
            JSON += key + ":" + map.get(key);
        }
        return "{" + JSON + "}";
    }

}






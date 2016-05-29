package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.I18n;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Message {
    public final BaseComponent inner;

    public Message(String text) {
        inner = new TextComponent(text);
    }

    public Message append(String text) {
        inner.addExtra(text);
        return this;
    }

    public Message appendFormat(String template, Object... obj) {
        return append(I18n.get(template, obj));
    }

    public Message append(ItemStack item) {
        return append(item, "{itemName} *{amount}");
    }

    public Message append(ItemStack item, String display) {
        boolean rawName = !(item.hasItemMeta() && item.getItemMeta().hasDisplayName());
        String name = rawName? item.getType().name(): item.getItemMeta().getDisplayName();
        BaseComponent result;
        String itemJson = ReflectionUtil.convertItemStackToJson(item);
        HoverEvent ev = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)});
        /* TODO: need to know the untranslated name for given item.
        if (display.contains("{itemName}") && rawName) {
            display = display.replace("{amount}", Integer.toString(item.getAmount()));
            String[] plain = display.split("\\{itemName\\}");

            TranslatableComponent trans = new TranslatableComponent(name);
            trans.setHoverEvent(ev);
            result = new TextComponent(plain[0]);
            result.setHoverEvent(ev);
            for (int i = 1; i < plain.length; i++) {
                TextComponent tmp = new TextComponent(plain[i]);
                tmp.setHoverEvent(ev);
                result.addExtra(trans);
                result.addExtra(tmp);
            }
        } else {
            result = new TextComponent(display.replace("{itemName}", name)
                    .replace("{amount}", Integer.toString(item.getAmount())));
            result.setHoverEvent(ev);
        }
        */
        result = new TextComponent(display.replace("{itemName}", name)
                .replace("{amount}", Integer.toString(item.getAmount())));
        result.setHoverEvent(ev);
        inner.addExtra(result);
        return this;
    }

    public Message send(Player p) {
        p.spigot().sendMessage(inner);
        return this;
    }

    public Message broadcast() {
        Bukkit.getServer().spigot().broadcast(inner);
        return this;
    }
}

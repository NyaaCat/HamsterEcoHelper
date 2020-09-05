package cat.nyaa.heh.business.item;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ModelableItem<T> {
    ItemStack getModel();

    default ItemStack buildModel(ItemStack itemStack) {
        ItemStack clone = new ItemStack(itemStack.getType());

        ItemMeta itemMeta = clone.getItemMeta();
        ItemMeta originMeta = itemStack.getItemMeta();
        if (originMeta == null) {
            return clone;
        }
        List<String> lore;
        if (originMeta.hasLore()){
            lore = originMeta.getLore();
        }else {
            lore = new ArrayList<>();
        }
        lore.addAll(buildLore());

        if (originMeta.hasCustomModelData()){
            itemMeta.setCustomModelData(originMeta.getCustomModelData());
        }

        Map<Enchantment, Integer> enchants = originMeta.getEnchants();
        if (!enchants.isEmpty()) {
            enchants.forEach((enchantment, integer) -> {
                itemMeta.addEnchant(enchantment, integer, true);
            });
        }

        if (originMeta instanceof LeatherArmorMeta && itemMeta instanceof LeatherArmorMeta){
            ((LeatherArmorMeta) itemMeta).setColor(((LeatherArmorMeta) itemMeta).getColor());
        }

        String displayName = originMeta.getDisplayName();
        if (!displayName.equals("")){
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+ displayName));
        }
        itemMeta.setLore(lore);
        markSample(itemMeta, this);

        clone.setItemMeta(itemMeta);
        return clone;
    }

    Collection<? extends String> buildLore();

    void markSample(ItemMeta itemMeta, ModelableItem<T> tModelableItem);

    T getImpl();
}

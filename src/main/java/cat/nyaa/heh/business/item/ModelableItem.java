package cat.nyaa.heh.business.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ModelableItem<T> {
    ItemStack getModel();

    default ItemStack buildModel(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();

        ItemMeta itemMeta;
        ItemMeta originMeta = itemStack.getItemMeta();
        if (originMeta == null) {
            return clone;
        }
        itemMeta = originMeta.clone();
        List<String> lore = itemMeta.getLore();
        if (lore == null){
            lore = new ArrayList<>();
        }
        Collection<? extends String> lore1 = buildLore();
        lore.addAll(lore1);
        itemMeta.setLore(lore);
        markSample(itemMeta, this);

        clone.setItemMeta(itemMeta);
        return clone;
    }

    Collection<? extends String> buildLore();

    void markSample(ItemMeta itemMeta, ModelableItem<T> tModelableItem);

    T getImpl();
}

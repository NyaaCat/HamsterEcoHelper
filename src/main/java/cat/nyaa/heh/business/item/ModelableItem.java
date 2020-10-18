package cat.nyaa.heh.business.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public interface ModelableItem<T> {
    ItemStack getModel();

    default ItemStack buildModel(ItemStack itemStack) {
        ItemStack clone = new ItemStack(itemStack.getType());

        ItemMeta itemMeta;
        ItemMeta originMeta = itemStack.getItemMeta();
        if (originMeta == null) {
            return clone;
        }
        itemMeta = originMeta.clone();
        markSample(itemMeta, this);

        clone.setItemMeta(itemMeta);
        return clone;
    }

    Collection<? extends String> buildLore();

    void markSample(ItemMeta itemMeta, ModelableItem<T> tModelableItem);

    T getImpl();
}

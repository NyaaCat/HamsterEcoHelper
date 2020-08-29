package cat.nyaa.heh.business.item;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.db.model.StorageDbModel;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StorageItem implements ModelableItem<StorageItem>{
    private ItemStack itemStack;
    private double fee;

    public StorageItem(StorageDbModel model){
        ItemStack itemStack = ItemStackUtils.itemFromBase64(model.getNbt());
        itemStack.setAmount(model.getAmount());
        init(itemStack, model.getStorageFee());
    }

    public StorageItem(ItemStack itemStack, double fee){
        init(itemStack, fee);
    }

    private void init(ItemStack itemStack, double fee) {
        this.itemStack = itemStack;
        this.fee = fee;
    }

    public ItemStack getItemStack(){
        return itemStack.clone();
    }

    @Override
    public ItemStack getModel() {
        return buildModel(itemStack);
    }

    @Override
    public Collection<? extends String> buildLore() {
        return Arrays.asList(buildFeeLore());
    }

    private String buildFeeLore() {
        HashMap<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("tax", String.format("%.2f", Tax.calcTax(ShopItemType.STORAGE, BigDecimal.valueOf(getFee())).doubleValue()));
        placeHolderMap.put("fee", String.format("%.2f", getFee()));
        return newSubstitutor(placeHolderMap, I18n.format("shop_item.lore.price"));
    }

    private String newSubstitutor(Map<String, String> placeHolderMap, String format) {
        return new StrSubstitutor(placeHolderMap, "{", "}", '\\').replace(format);
    }

    @Override
    public void markSample(ItemMeta itemMeta, ModelableItem<StorageItem> storageItemModelableItem) {

    }

    @Override
    public StorageItem getImpl() {
        return this;
    }

    public double getFee() {
        return fee;
    }
}

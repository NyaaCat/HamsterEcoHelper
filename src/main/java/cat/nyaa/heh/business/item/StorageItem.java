package cat.nyaa.heh.business.item;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.db.model.StorageDbModel;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.*;

public class StorageItem implements ModelableItem<StorageItem>{
    private ItemStack itemStack;
    private double fee;
    private UUID owner;
    private long uid;

    public StorageItem(StorageDbModel model){
        ItemStack itemStack = ItemStackUtils.itemFromBase64(model.getNbt());
        itemStack.setAmount(model.getAmount());
        init(model.getOwner(), itemStack, model.getStorageFee());
    }

    public StorageItem(UUID owner, ItemStack itemStack, double fee){
        init(owner, itemStack, fee);
    }

    private void init(UUID owner, ItemStack itemStack, double fee) {
        this.owner = owner;
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
        placeHolderMap.put("tax", String.format("%.2f", Tax.calcTax(BigDecimal.valueOf(getFee()), ShopItemType.STORAGE).doubleValue()));
        placeHolderMap.put("fee", String.format("%.2f", getFee()));
        return newSubstitutor(placeHolderMap, I18n.format("storage.lore.price"));
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

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}

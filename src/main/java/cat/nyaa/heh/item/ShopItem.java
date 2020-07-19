package cat.nyaa.heh.item;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.enums.ShopItemType;
import cat.nyaa.heh.transaction.Tax;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.ItemTagUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.*;

public class ShopItem {
    ShopItemType shopItemType = ShopItemType.MARKET;
    private long uid = -1;
    private UUID owner;

    private ItemStack itemStack;
    private WeakReference<ItemStack> model = new WeakReference<>(null);

    private int amount;
    private int sold = 0;

    private double unitPrice;
    private boolean available = true;
    private long time;

    public ShopItem(ShopItemDbModel shopItemDbModel) {
        this.uid = shopItemDbModel.uid;
        this.owner = UUID.fromString(shopItemDbModel.owner);
        this.amount = shopItemDbModel.amount;
        this.sold = shopItemDbModel.sold;
        this.itemStack = ItemStackUtils.itemFromBase64(shopItemDbModel.nbt);
        this.unitPrice = shopItemDbModel.price;
        this.shopItemType = shopItemDbModel.type;
        this.time = shopItemDbModel.time;
        this.available = shopItemDbModel.available;
    }

    public ShopItem(UUID from, ShopItemType type, ItemStack itemStack, double unitPrice){
        this.owner = from;
        this.amount = itemStack.getAmount();
        this.sold = 0;
        this.itemStack = itemStack;
        this.unitPrice = unitPrice;
        this.shopItemType = type;
        this.time = System.currentTimeMillis();
        this.available = true;
    }

    public ShopItemType getShopItemType() {
        return shopItemType;
    }

    public long getUid() {
        return uid;
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public int getAmount() {
        return amount;
    }

    public int getSoldAmount() {
        return sold;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setShopItemType(ShopItemType shopItemType) {
        this.shopItemType = shopItemType;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ItemStack getModel(){
        ItemStack itemStack = model.get();
        if (itemStack == null){
            itemStack = buildModel(this.itemStack);
            model = new WeakReference<>(itemStack);
        }
        itemStack.setAmount(amount - sold);
        return itemStack;
    }

    private ItemStack buildModel(ItemStack itemStack) {
        ItemStack clone = new ItemStack(itemStack.getType());
        clone.setAmount(Math.max(amount - sold, 1));
        addModelTag(clone);
        ItemMeta itemMeta = clone.getItemMeta();
        ItemMeta originMeta = itemStack.getItemMeta();
        if (originMeta != null){
            List<String> lore;
            if (originMeta.hasLore()){
                lore = originMeta.getLore();
            }else {
                lore = new ArrayList<>();
            }
            lore.add(buildPriceLore());
            lore.add(buildOwnerLore());
            itemMeta.setLore(lore);
            clone.setItemMeta(itemMeta);
        }
        return clone;
    }

    private String buildOwnerLore() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        String name = offlinePlayer.getName();
        if (name == null){
            name = owner.toString();
        }
        HashMap<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("{name}", name);
        return newSubstitutor(placeHolderMap, I18n.format("shop_item.lore.owner"));
    }

    public String buildPriceLore() {
        HashMap<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("tax", String.format("%.2f", Tax.calcTax(this, BigDecimal.valueOf(getUnitPrice())).doubleValue()));
        placeHolderMap.put("unitPrice", String.format("%.2f", getUnitPrice()));
        placeHolderMap.put("totalPrice", String.format("%.2f", getTotalPrice()));
        return newSubstitutor(placeHolderMap, I18n.format("shop_item.lore.price"));
    }

    private String newSubstitutor(Map<String, String> placeHolderMap, String format) {
        return new StrSubstitutor(placeHolderMap, "{", "}", '\\').replace(format);
    }

    private double getTotalPrice() {
        return Math.max((amount - sold) * unitPrice, 0);
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    private static void addModelTag(ItemStack clone) {
        try {
            ItemTagUtils.setBoolean(clone, "isModel", true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

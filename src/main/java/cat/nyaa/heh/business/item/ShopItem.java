package cat.nyaa.heh.business.item;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.db.model.ShopItemDbModel;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

public class ShopItem implements ModelableItem<ShopItem>{
    private static final String KEY_MODEL = "heh_model";
    private static final NamespacedKey NAMESPACED_KEY_MODEL = new NamespacedKey(HamsterEcoHelper.plugin, KEY_MODEL);

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
    private String itemMeta = null;

    public ShopItem(ShopItemDbModel shopItemDbModel) {
        this.uid = shopItemDbModel.getUid();
        this.owner = shopItemDbModel.getOwner();
        this.amount = shopItemDbModel.getAmount();
        this.sold = shopItemDbModel.getSold();
        try {
            this.itemStack = ItemStackUtils.itemFromBase64(shopItemDbModel.getNbt());
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, String.format("invalid shop item: uid: %d, nbt:%s", uid, shopItemDbModel.getNbt()));
        }
        this.unitPrice = shopItemDbModel.getPrice();
        this.shopItemType = shopItemDbModel.getType();
        this.time = shopItemDbModel.getTime();
        this.available = shopItemDbModel.isAvailable();
        this.itemMeta = shopItemDbModel.getMeta();
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
        String owner = isOwnedBySystem() ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(getOwner()).getName();
        this.itemMeta = itemStack.toString() + "owner:" + owner;
    }

    public ShopItem(long uid, UUID owner, int amount, int sold, String itemStack, double unitPrice, ShopItemType shopItemType, long time, boolean available, String itemMeta) {
        this.uid = uid;
        this.owner = owner;
        this.amount = amount;
        this.sold = sold;
        try {
            this.itemStack = ItemStackUtils.itemFromBase64(itemStack);
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, String.format("invalid shop item: uid: %d, nbt:%s", uid, itemStack));
        }
        this.unitPrice = unitPrice;
        this.shopItemType = shopItemType;
        this.time = time;
        this.available = available;
        this.itemMeta = itemMeta;
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

    @Override
    public Collection<? extends String> buildLore() {
        return Arrays.asList(buildPriceLore(),
                buildOwnerLore());
    }

    @Override
    public void markSample(ItemMeta itemMeta, ModelableItem<ShopItem> shopItemModelableItem) {
        markSample(itemMeta, shopItemModelableItem.getImpl());
    }

    @Override
    public ShopItem getImpl() {
        return this;
    }

    public static void markSample(ItemMeta stack, ShopItem shopItem) {
        stack.getPersistentDataContainer().set(NAMESPACED_KEY_MODEL, PersistentDataType.LONG, shopItem.getUid());
    }

    public static boolean isSample(ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null){
            return false;
        }
        return itemMeta.getPersistentDataContainer().get(NAMESPACED_KEY_MODEL, PersistentDataType.LONG)!=null;
    }

    public static ShopItem getFromSample(ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null){
            return null;
        }
        Long uid = itemMeta.getPersistentDataContainer().get(NAMESPACED_KEY_MODEL, PersistentDataType.LONG);
        if (uid == null){
            return null;
        }
        return ShopItemManager.getInstance().getShopItem(uid);
    }

    private String buildOwnerLore() {
        String owner = isOwnedBySystem() ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(getOwner()).getName();
        HashMap<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("name", owner);
        return newSubstitutor(placeHolderMap, I18n.format("shop_item.lore.owner"));
    }

    public String buildPriceLore() {
        HashMap<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("tax", String.format("%.2f", Tax.calcTax(BigDecimal.valueOf(getUnitPrice()), this.getShopItemType()).doubleValue()));
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

    public boolean isOwnedBySystem() {
        return SystemAccountUtils.isSystemAccount(owner);
    }

    public String getMeta() {
        return itemMeta;
    }

    public boolean isReadyToSell() {
        return isAvailable() && getAmount() - getSoldAmount() > 0;
    }
}

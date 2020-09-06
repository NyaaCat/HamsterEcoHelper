package cat.nyaa.heh.db.model;

import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;

import java.util.UUID;

@Table("items")
public class ShopItemDbModel {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "owner")
    UUID owner;
    @Column(name = "type")
    ShopItemType type;
    @Column(name = "nbt")
    String nbt;
    @Column(name = "price")
    double price;
    @Column(name = "amount")
    int amount;
    @Column(name = "sold")
    int sold;
    @Column(name = "available")
    boolean available;
    @Column(name = "time")
    long time;
    @Column(name = "item_meta")
    String itemMeta;

    public ShopItemDbModel() {
    }

    public static ShopItem toShopItem(ShopItemDbModel shopItemDbModel) {
        if (shopItemDbModel == null)return null;
        else return shopItemDbModel.toShopItem();
    }

    public ShopItem toShopItem(){
        return new ShopItem(this);
    }

    public static ShopItemDbModel fromShopItem(ShopItem item){
        ShopItemDbModel md = new ShopItemDbModel();
        md.uid = item.getUid();
        md.owner = item.getOwner();
        md.amount = item.getAmount();
        md.sold = item.getSoldAmount();
        md.nbt = ItemStackUtils.itemToBase64(item.getItemStack());
        md.price = item.getUnitPrice();
        md.type = item.getShopItemType();
        md.time = item.getTime();
        md.available = item.isAvailable();
        md.itemMeta = item.getMeta();
        return md;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ShopItemType getType() {
        return type;
    }

    public void setType(ShopItemType type) {
        this.type = type;
    }

    public String getNbt() {
        return nbt;
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public boolean isAvailable() {
        return available;
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

    public String getItemMeta() {
        return itemMeta;
    }

    public void setItemMeta(String itemMeta) {
        this.itemMeta = itemMeta;
    }
}

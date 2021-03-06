package heh8_0.db.model;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import heh8_0.db.ShopItemType;

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
    @Column(name = "meta")
    String meta;

    public ShopItemDbModel() {
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

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}

package heh8_0.db.model;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Table("storage")
public class StorageDbModel {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "owner")
    UUID owner;
    @Column(name = "nbt")
    String nbt;
    @Column(name = "amount")
    int amount;
    @Column(name = "storage_fee")
    double storageFee;
    @Column(name = "time")
    long time;

    public StorageDbModel(){
    }

    public StorageDbModel(ShopItemDbModel model){
        this.uid = -1;
        this.owner = model.getOwner();
        this.nbt = model.getNbt();
        this.amount = model.getAmount();
        this.storageFee = 0;
        this.time = System.currentTimeMillis();
    }

    public StorageDbModel(long uid, UUID owner, ItemStack item, double fee){
        this.uid = -1;
        this.owner = owner;
        this.nbt = ItemStackUtils.itemToBase64(item);
        this.amount = item.getAmount();
        this.storageFee = fee;
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getNbt() {
        return nbt;
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getStorageFee() {
        return storageFee;
    }

    public void setStorageFee(double storageFee) {
        this.storageFee = storageFee;
    }
}

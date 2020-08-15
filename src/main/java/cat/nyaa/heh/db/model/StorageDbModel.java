package cat.nyaa.heh.db.model;

import cat.nyaa.heh.enums.ShopItemType;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

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
    @Column(name = "time")
    long time;

    public StorageDbModel(){

    }

    public StorageDbModel(ShopItemDbModel model){

    }
}

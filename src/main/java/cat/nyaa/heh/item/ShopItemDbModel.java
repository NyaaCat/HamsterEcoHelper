package cat.nyaa.heh.item;

import cat.nyaa.heh.enums.ShopItemType;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;

@Table("shop_item")
public class ShopItemDbModel {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "owner")
    String owner;
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
        md.owner = item.getOwner().toString();
        md.amount = item.getAmount();
        md.sold = item.getSoldAmount();
        md.nbt = ItemStackUtils.itemToBase64(item.getItemStack());
        md.price = item.getUnitPrice();
        md.type = item.shopItemType;
        md.time = item.getTime();
        md.available = item.isAvailable();
        return md;
    }
}

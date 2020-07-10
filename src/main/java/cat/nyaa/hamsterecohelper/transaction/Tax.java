package cat.nyaa.hamsterecohelper.transaction;

import cat.nyaa.hamsterecohelper.HamsterEcoHelper;
import cat.nyaa.hamsterecohelper.item.ShopItem;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Table("tax")
public class Tax {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "from")
    String from;
    @Column(name = "tax")
    double tax;
    @Column(name = "time")
    long time;

    public static double calcTax(ShopItem shopItem, double price){
        Double taxRate = HamsterEcoHelper.plugin.config.taxRateMap.getOrDefault(shopItem.getShopItemType().name().toLowerCase(), 0d);
        return new BigDecimal(price).multiply(BigDecimal.valueOf(taxRate)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP).doubleValue();
    }
}

package cat.nyaa.heh.business.transaction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Table("tax")
public class Tax {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "payer")
    UUID payer;
    @Column(name = "tax")
    double tax;
    @Column(name = "fee")
    double fee;
    @Column(name = "time")
    long time;

    public Tax() {
    }

    public Tax(long uid, UUID from, double tax, double fee, long time) {
        this.uid = uid;
        this.payer = from;
        this.tax = tax;
        this.fee = fee;
        this.time = time;
    }

    public static BigDecimal calcTax(ShopItemType shopItemType, BigDecimal price){
        double taxRate = HamsterEcoHelper.plugin.config.taxRateMap.getOrDefault(shopItemType.name().toLowerCase(), 0d).doubleValue();
        return price.multiply(BigDecimal.valueOf(taxRate)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }
}

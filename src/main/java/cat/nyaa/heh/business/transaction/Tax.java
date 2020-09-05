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
    @Column(name = "reason")
    String reason;

    public Tax() {
    }

    public Tax(long uid, UUID from, double tax, double fee, long time, String reason) {
        this.uid = uid;
        this.payer = from;
        this.tax = tax;
        this.fee = fee;
        this.time = time;
        this.reason = reason;
    }

    public static BigDecimal calcTax(ShopItemType shopItemType, BigDecimal price){
        double taxRate = HamsterEcoHelper.plugin.config.taxRateMap.getOrDefault(shopItemType.name().toLowerCase(), 0d).doubleValue();
        return price.multiply(BigDecimal.valueOf(taxRate)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setPayer(UUID payer) {
        this.payer = payer;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getUid() {
        return uid;
    }

    public UUID getPayer() {
        return payer;
    }

    public double getTax() {
        return tax;
    }

    public double getFee() {
        return fee;
    }

    public long getTime() {
        return time;
    }

    public String getReason() {
        return reason;
    }
}

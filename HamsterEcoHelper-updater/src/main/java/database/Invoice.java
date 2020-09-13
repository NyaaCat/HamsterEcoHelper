package database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

@Table("invoice")
public class Invoice {

    public static final int DRAFT = 0;
    public static final int COMPLETED = 1;
    public static final int CANCELED = -1;

    @Column(name = "id")
    private long id;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "updated_time")
    private long updatedTime;

    @Column(name = "buyer_id")
    private String buyerId;

    @Column(name = "seller_id")
    private String sellerId;

    @Column(name = "drawee_id")
    private String draweeId;

    @Column(name = "item", columnDefinition = "MEDIUMTEXT")
    private String item;

    @Column(name = "amount")
    private int amount;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "tax")
    private double tax;

    @Column(name = "state")
    private int state = DRAFT;

    private Invoice() {
        updatedTime = System.currentTimeMillis();
        createdTime = System.currentTimeMillis();
    }

    Invoice(long id, UUID buyerId, UUID sellerId, ItemStack item, double totalPrice, double tax) {
        this();
        this.id = id;
        this.buyerId = buyerId.toString();
        this.sellerId = sellerId.toString();
        this.totalPrice = totalPrice;
        this.tax = tax;
        this.item = ItemStackUtils.itemToBase64(item);
        this.amount = item.getAmount();
    }

    public long getId() {
        return id;
    }

    public UUID getBuyerId() {
        return UUID.fromString(buyerId);
    }

    public UUID getSellerId() {
        return UUID.fromString(sellerId);
    }

    public UUID getDraweeId() {
        return draweeId == null ? null : UUID.fromString(draweeId);
    }

    public void setDraweeId(UUID draweeId) {
        this.draweeId = draweeId.toString();
    }

    public OfflinePlayer getBuyer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(buyerId));
    }

    public OfflinePlayer getSeller() {
        return Bukkit.getOfflinePlayer(UUID.fromString(sellerId));
    }

    public OfflinePlayer getDrawee() {
        return Bukkit.getOfflinePlayer(UUID.fromString(draweeId));
    }

    public ItemStack getItemStack() {
        return getItemStack(amount);
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = ItemStackUtils.itemFromBase64(this.item);
        item.setAmount(amount);
        return item;
    }

    public long getAmount() {
        return amount;
    }

    public int getState() {
        return state;
    }

    public void setCompleted() {
        if (state != DRAFT) throw new IllegalStateException();
        this.state = COMPLETED;
        this.updatedTime = System.currentTimeMillis();
    }

    public void setCanceled() {
        if (state != DRAFT) throw new IllegalStateException();
        this.state = CANCELED;
        this.updatedTime = System.currentTimeMillis();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getTax() {
        return tax;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
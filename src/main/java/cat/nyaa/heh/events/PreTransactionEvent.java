package cat.nyaa.heh.events;

import cat.nyaa.heh.item.ShopItem;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PreTransactionEvent extends CancelableEvent {
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList(){ return handlerList;}

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private ShopItem shopItem;
    private int amount;
    private double totalFee;
    private UUID buyer;
    private UUID seller;

    public PreTransactionEvent(ShopItem shopItem, int amount, double totalFee, UUID buyer, UUID seller) {
        this.shopItem = shopItem;
        this.amount = amount;
        this.totalFee = totalFee;
        this.buyer = buyer;
        this.seller = seller;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public UUID getBuyer() {
        return buyer;
    }

    public void setBuyer(UUID buyer) {
        this.buyer = buyer;
    }

    public UUID getSeller() {
        return seller;
    }

    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    public ShopItem getShopItem() {
        return shopItem;
    }

    public void setShopItem(ShopItem shopItem) {
        this.shopItem = shopItem;
    }
}

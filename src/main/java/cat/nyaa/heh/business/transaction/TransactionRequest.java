package cat.nyaa.heh.business.transaction;

import cat.nyaa.heh.business.item.ShopItem;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class TransactionRequest {
    //must-have fields
    private UUID buyer;
    private String reason;
    private ShopItem item;

    //fields have default val
    private UUID seller;
    private UUID payer;
    private double fee = 0;
    private Integer amount = null;
    private Double priceOverride = null;
    private Inventory receiveInv = null;
    private Inventory returnInv = null;
    private Double taxRate = null;
    private TaxMode taxMode = TaxMode.ADDITION;
    private boolean forceStorage;
    private boolean recheck = true;

    private TransactionRequest() {
    }

    public UUID getSeller() {
        return seller == null ? item.getOwner() : seller;
    }
    public UUID getPayer() {
        return payer == null ? buyer : payer;
    }
    public int getAmount() {
        return amount == null ? item.getAmount() - item.getSoldAmount() : amount;
    }

    //<editor-fold>
    public UUID getBuyer() {
        return buyer;
    }
    public void setBuyer(UUID buyer) {
        this.buyer = buyer;
    }
    public void setPayer(UUID payer) {
        this.payer = payer;
    }
    public void setSeller(UUID seller) {
        this.seller = seller;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public ShopItem getItem() {
        return item;
    }
    public void setItem(ShopItem item) {
        this.item = item;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public double getFee() {
        return fee;
    }
    public void setFee(double fee) {
        this.fee = fee;
    }
    public Double getPriceOverride() {
        return priceOverride;
    }
    public void setPriceOverride(Double priceOverride) {
        this.priceOverride = priceOverride;
    }
    public Inventory getReceiveInv() {
        return receiveInv;
    }
    public void setReceiveInv(Inventory receiveInv) {
        this.receiveInv = receiveInv;
    }
    public Inventory getReturnInv() {
        return returnInv;
    }
    public void setReturnInv(Inventory returnInv) {
        this.returnInv = returnInv;
    }
    public Double getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
    public TaxMode getTaxMode() {
        return taxMode;
    }
    public void setTaxMode(TaxMode taxMode) {
        this.taxMode = taxMode;
    }

    public void setForceStorage(boolean forceStorage) {
        this.forceStorage = forceStorage;
    }

    public boolean isForceStorage() {
        return forceStorage;
    }

    public boolean isRecheck() {
        return recheck;
    }
    //</editor-fold>


    public static class TransactionBuilder {
        private TransactionRequest request = new TransactionRequest();

        public TransactionBuilder() {
        }

        public TransactionBuilder buyer(UUID buyer) {
            this.request.buyer = buyer;
            return this;
        }

        public TransactionBuilder payer(UUID payer) {
            this.request.payer = payer;
            return this;
        }

        public TransactionBuilder seller(UUID seller) {
            this.request.seller = seller;
            return this;
        }

        public TransactionBuilder item(ShopItem item) {
            this.request.item = item;
            return this;
        }

        public TransactionBuilder priceOverride(Double priceOverride) {
            this.request.priceOverride = priceOverride;
            return this;
        }

        public TransactionBuilder amount(int amount) {
            this.request.amount = amount;
            return this;
        }

        public TransactionBuilder fee(double fee) {
            this.request.fee = fee;
            return this;
        }

        public TransactionBuilder receiveInv(Inventory receiveInv) {
            this.request.receiveInv = receiveInv;
            return this;
        }

        public TransactionBuilder returnInv(Inventory returnInv) {
            this.request.returnInv = returnInv;
            return this;
        }

        public TransactionBuilder reason(String reason) {
            this.request.reason = reason;
            return this;
        }

        public TransactionBuilder taxRate(Double taxRate) {
            this.request.taxRate = taxRate;
            return this;
        }

        public TransactionBuilder taxMode(TaxMode taxMode) {
            this.request.taxMode = taxMode;
            return this;
        }
        public TransactionBuilder recheck(boolean recheck){
            this.request.recheck = recheck;
            return this;
        }

        public TransactionRequest build() {
            if (request.getItem() == null){
                throw new IllegalStateException("null item detected.");
            }
            if (request.getBuyer() == null){
                throw new IllegalStateException("no buyer specified.");
            }
            if (request.getReason() == null){
                throw new IllegalStateException("no reason detected.");
            }
            return request;
        }

        public TransactionBuilder forceStorage(boolean b) {
            request.setForceStorage(b);
            return this;
        }
    }
}

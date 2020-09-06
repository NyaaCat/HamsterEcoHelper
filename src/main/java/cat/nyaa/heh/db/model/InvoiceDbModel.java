package cat.nyaa.heh.db.model;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Table("invoice")
public class InvoiceDbModel {
    @Column(name = "uid", primary = true)
    private long uid;
    @Column(name = "customer")
    private UUID customer;
    @Column(name = "payer", nullable = true)
    private UUID payer;

    public InvoiceDbModel() {
    }

    public InvoiceDbModel(OfflinePlayer player, ShopItem shopItem, UUID customer) {
        this.uid = shopItem.getUid();
        this.payer = player.getUniqueId();
        this.customer = customer;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public UUID getCustomer() {
        return customer;
    }

    public void setCustomer(UUID customer) {
        this.customer = customer;
    }

    public UUID getPayer() {
        return payer;
    }

    public void setPayer(UUID payer) {
        this.payer = payer;
    }
}

package cat.nyaa.heh.db;

import cat.nyaa.heh.db.model.InvoiceDbModel;
import cat.nyaa.heh.business.item.ShopItem;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class DirectInvoiceConnection {
    private static DirectInvoiceConnection INSTANCE;

    private DirectInvoiceConnection() {
    }

    public static DirectInvoiceConnection getInstance() {
        if (INSTANCE == null) {
            synchronized (DirectInvoiceConnection.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DirectInvoiceConnection();
                }
            }
        }
        return INSTANCE;
    }

    public List<String> getAvailableInvoices(){
        return DatabaseManager.getInstance().getAvailableInvoiceIds();
    }

    public ShopItem getInvoice(long uid) {
        return DatabaseManager.getInstance().getItem(uid);
    }

    public UUID getCustomer(long uid) {
        return DatabaseManager.getInstance().getInvoiceCustomer(uid);
    }

    public UUID getFrom(long uid){return DatabaseManager.getInstance().getInvoiceFrom(uid);}

    public UUID getPayer(long uid){
        return DatabaseManager.getInstance().getInvoicePayer(uid);
    }

    public void newInvoice(OfflinePlayer player, ShopItem shopItem, UUID customer) {
        InvoiceDbModel invoiceDbModel = new InvoiceDbModel(player, shopItem, customer);
        DatabaseManager.getInstance().insertInvoice(invoiceDbModel);
    }

    public void cancelInvoice(ShopItem item) {
        item.setAvailable(false);
        DatabaseManager.getInstance().updateShopItem(item);
    }
}

package cat.nyaa.heh.db;

import cat.nyaa.heh.db.model.InvoiceDbModel;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

    public List<ShopItem> getAvailableInvoices(){
        return DatabaseManager.getInstance().getAvailableInvoices();
    }

    public ShopItem getInvoice(long uid) {
        return DatabaseManager.getInstance().getItem(uid);
    }

    public UUID getCustomer(long uid) {
        return DatabaseManager.getInstance().getInvoiceCustomer(uid);
    }

    public UUID getPayer(long uid){
        return DatabaseManager.getInstance().getInvoicePayer(uid);
    }

    public void newInvoice(OfflinePlayer player, ShopItem shopItem) {
        InvoiceDbModel invoiceDbModel = new InvoiceDbModel(player, shopItem);
        DatabaseManager.getInstance().insertInvoice(invoiceDbModel);
    }
}

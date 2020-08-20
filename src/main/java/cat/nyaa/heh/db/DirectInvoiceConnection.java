package cat.nyaa.heh.db;

import cat.nyaa.heh.item.ShopItem;

import java.util.List;

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
}

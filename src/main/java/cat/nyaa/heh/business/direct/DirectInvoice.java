package cat.nyaa.heh.business.direct;

import cat.nyaa.heh.db.DirectInvoiceConnection;
import cat.nyaa.heh.item.ShopItem;

import java.util.List;
import java.util.stream.Collectors;

public class DirectInvoice {
    private static DirectInvoice INSTANCE;

    private DirectInvoice() {
    }

    public static DirectInvoice getInstance() {
        if (INSTANCE == null) {
            synchronized (DirectInvoice.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DirectInvoice();
                }
            }
        }
        return INSTANCE;
    }

    public List<String> getDirectInvoiceIds(){
        return DirectInvoiceConnection.getInstance().getAvailableInvoices().stream()
                .map(item -> String.valueOf(item.getUid()))
                .collect(Collectors.toList());
    }

    public ShopItem getInvoice(long uid) {
        return DirectInvoiceConnection.getInstance().getInvoice(uid);
    }
}

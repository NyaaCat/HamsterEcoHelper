package cat.nyaa.heh.business.direct;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.db.DirectInvoiceConnection;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
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

    public boolean payInvoice(Player payer, ShopItem shopItem){
        UUID customer = getCustomer(shopItem.getUid());
        double fee = HamsterEcoHelper.plugin.config.directFeeBase;
        int amount = shopItem.getAmount() - shopItem.getSoldAmount();
        return TransactionController.getInstance().makeTransaction(customer, payer.getUniqueId(), shopItem.getOwner(), shopItem, amount, fee, null, null);
    }

    public List<String> getDirectInvoiceIds(){
        return DirectInvoiceConnection.getInstance().getAvailableInvoices().stream()
                .map(item -> String.valueOf(item.getUid()))
                .collect(Collectors.toList());
    }

    public ShopItem getInvoice(long uid) {
        return DirectInvoiceConnection.getInstance().getInvoice(uid);
    }

    public UUID getCustomer(long uid){
        return DirectInvoiceConnection.getInstance().getCustomer(uid);
    }

    public void newInvoice(OfflinePlayer player, ShopItem shopItem) {
        DirectInvoiceConnection.getInstance().newInvoice(player, shopItem);
    }

    public void cancelInvoice(ShopItem item) {
        DirectInvoiceConnection.getInstance().cancelInvoice(item);
    }
}

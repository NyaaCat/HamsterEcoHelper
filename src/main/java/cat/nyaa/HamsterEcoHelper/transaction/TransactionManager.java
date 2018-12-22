package cat.nyaa.HamsterEcoHelper.transaction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Invoice;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionManager {
    private HamsterEcoHelper plugin;

    Multimap<UUID, Invoice> activeSell = MultimapBuilder.hashKeys().hashSetValues().build();
    Multimap<UUID, Invoice> activeBuy = MultimapBuilder.hashKeys().hashSetValues().build();

    public TransactionManager(HamsterEcoHelper pl) {
        plugin = pl;
    }

    Invoice sellTo(Player seller, OfflinePlayer buyer, ItemStack item, double totalPrice, double tax) {
        UUID sellerId = seller.getUniqueId();
        UUID buyerId = buyer.getUniqueId();
        Invoice invoice = plugin.database.draftInvoice(buyerId, sellerId, item, totalPrice, tax);
        plugin.logger.info(I18n.format("log.info.invoice_drafted", invoice.getId(), MiscUtils.getItemName(item), item.getAmount(), totalPrice, tax, buyerId.toString(), sellerId.toString()));
        activeSell.put(sellerId, invoice);
        activeBuy.put(buyer.getUniqueId(), invoice);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeSell.remove(sellerId, invoice);
            activeBuy.remove(buyerId, invoice);
        }, plugin.config.transaction_active_time_out_tick);
        return invoice;
    }

    Invoice pay(Player drawee, Invoice baseInvoice) {
        UUID draweeId = drawee.getUniqueId();
        Invoice invoice = plugin.database.payInvoice(baseInvoice.getId(), drawee);
        activeSell.remove(invoice.getSellerId(), invoice);
        activeBuy.remove(invoice.getBuyerId(), invoice);
        plugin.logger.info(I18n.format("log.info.invoice_paid", invoice.getId(), draweeId.toString()));
        return invoice;
    }

    Invoice cancel(Invoice baseInvoice) {
        Invoice invoice = plugin.database.cancelInvoice(baseInvoice.getId());
        // Left cancelled active for 10 sec to prevent sellto-close-sellto collision on buyside
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeSell.remove(invoice.getSellerId(), invoice);
            activeBuy.remove(invoice.getBuyerId(), invoice);
        }, 10 * 20);
        plugin.logger.info(I18n.format("log.info.invoice_cancelled", invoice.getId()));
        return invoice;
    }

    Invoice query(long id) {
        return plugin.database.queryInvoice(id);
    }

    List<Invoice> querySellerOpen(OfflinePlayer seller) {
        UUID sellerId = seller.getUniqueId();
        List<Invoice> invoices = plugin.database.querySellerInvoice(sellerId);
        return invoices.stream()
                       .filter(i -> i.getState() == Invoice.DRAFT)
                       .sorted(Comparator.comparing(Invoice::getCreatedTime))
                       .collect(Collectors.toList());
    }

    List<Invoice> queryBuyerOpen(OfflinePlayer buyer) {
        UUID buyerId = buyer.getUniqueId();
        List<Invoice> invoices = plugin.database.queryBuyerInvoice(buyerId);
        return invoices.stream()
                       .filter(i -> i.getState() == Invoice.DRAFT)
                       .sorted(Comparator.comparing(Invoice::getCreatedTime))
                       .collect(Collectors.toList());
    }

    List<Invoice> querySellerClosed(OfflinePlayer seller) {
        UUID sellerId = seller.getUniqueId();
        List<Invoice> invoices = plugin.database.querySellerInvoice(sellerId);
        return invoices.stream()
                       .filter(i -> i.getState() != Invoice.DRAFT)
                       .sorted(Comparator.comparing(Invoice::getUpdatedTime))
                       .collect(Collectors.toList());
    }

    List<Invoice> queryBuyerClosed(OfflinePlayer buyer) {
        UUID buyerId = buyer.getUniqueId();
        List<Invoice> invoices = plugin.database.queryBuyerInvoice(buyerId);
        return invoices.stream()
                       .filter(i -> i.getState() != Invoice.DRAFT)
                       .sorted(Comparator.comparing(Invoice::getUpdatedTime))
                       .collect(Collectors.toList());
    }

    List<Invoice> queryDraweeClosed(OfflinePlayer drawee) {
        UUID draweeId = drawee.getUniqueId();
        List<Invoice> invoices = plugin.database.queryDraweeInvoice(draweeId);
        return invoices.stream()
                       .filter(i -> i.getState() != Invoice.DRAFT)
                       .sorted(Comparator.comparing(Invoice::getUpdatedTime))
                       .collect(Collectors.toList());
    }
}

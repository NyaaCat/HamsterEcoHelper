package cat.nyaa.HamsterEcoHelper.transaction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Invoice;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import com.google.common.collect.Iterables;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.librazy.nclangchecker.LangKey;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cat.nyaa.nyaacore.Message.getItemJsonStripped;

public class TransactionCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public TransactionCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "transaction";
    }

    @SubCommand(value = "sellto", permission = "heh.transaction.sellto")
    public void sellTo(CommandSender sender, Arguments args) {
        if (args.length() == 4) {
            Player seller = asPlayer(sender);
            OfflinePlayer buyer = args.nextOfflinePlayer();
            if (seller.getUniqueId().equals(buyer.getUniqueId())) {
                msg(sender, "user.error.no_self");
                return;
            }
            double price = args.nextDouble("#.##");
            if (!(price >= 0.01)) {
                msg(sender, "user.error.not_double");
                return;
            }
            List<Invoice> invoices = plugin.transactionManager.querySellerOpen(seller);
            if (invoices.size() >= plugin.config.transaction_max_open_sellside && !seller.hasPermission("heh.admin")) {
                msg(sender, "user.transaction.max_open_sellside_limit", plugin.config.transaction_max_open_sellside);
                return;
            }
            ItemStack item = getItemInHand(sender);
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                if (MarketManager.containsBook(item)) {
                    msg(sender, "user.error.shulker_box_contains_book");
                    return;
                }
                double tax = 0.0;
                if (plugin.config.transaction_tax > 0) {
                    tax = (price / 100) * plugin.config.transaction_tax;
                }
                Invoice invoice = plugin.transactionManager.sellTo(seller, buyer, item, price, tax);
                seller.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                Map<String, BaseComponent> componentMap = invoiceComponent(invoice);
                String hoverText = I18n.format("user.transaction.command_hover_text", invoice.getId());
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent(hoverText)});
                String payCmd = I18n.format("user.transaction.pay_command", invoice.getId());
                BaseComponent payCommand = new TextComponent(payCmd);
                payCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, payCmd));
                payCommand.setHoverEvent(hover);
                String cancelCmd = I18n.format("user.transaction.cancel_command", invoice.getId());
                BaseComponent cancelCommand = new TextComponent(cancelCmd);
                cancelCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cancelCmd));
                cancelCommand.setHoverEvent(hover);
                componentMap.put("{payCommand}", payCommand);
                componentMap.put("{cancelCommand}", cancelCommand);
                invoiceMessage(componentMap, "user.transaction.drafted_seller", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                        .send(seller);
                invoiceMessage(componentMap, "user.transaction.drafted_buyer", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                        .send(buyer, isAfk(buyer));
            } else {
                msg(sender, "user.info.not_item_hand");
            }
        } else {
            msg(sender, "manual.transaction.sellto.description");
            msg(sender, "manual.transaction.sellto.usage");
        }
    }

    @SubCommand(value = "pay", permission = "heh.transaction.pay")
    public void pay(CommandSender sender, Arguments args) {
        Player drawee = asPlayer(sender);
        Invoice invoice;
        if (args.length() == 2) {
            Collection<Invoice> invoices = plugin.transactionManager.activeBuy.get(drawee.getUniqueId());
            Iterator<Invoice> invoiceIterator = invoices.iterator();
            if (!invoiceIterator.hasNext()) {
                msg(sender, "user.transaction.no_active_buy");
                return;
            }
            invoice = invoiceIterator.next();
            if (invoiceIterator.hasNext()) {
                msg(sender, "user.transaction.multiple_active_buy");
                listActiveInvoice(drawee, invoice, invoiceIterator);
                return;
            }
            invoice = plugin.transactionManager.query(invoice.getId());
        } else {
            invoice = getInvoice(sender, args);
            if (invoice == null) return;
        }
        if (notDraft(sender, drawee, invoice)) return;
        if (!drawee.getUniqueId().equals(invoice.getBuyerId()) && plugin.config.transaction_require_confirm_seller) {
            if (args.top() == null) {
                msg(sender, "user.transaction.pay_others");
                return;
            } else {
                OfflinePlayer expectedSeller = args.nextOfflinePlayer();
                if (!expectedSeller.getUniqueId().equals(invoice.getSellerId())) {
                    msg(sender, "user.transaction.wrong_seller");
                    return;
                }
            }
        }
        double totalPrice = invoice.getTotalPrice();
        double tax = invoice.getTax();
        if (plugin.eco.enoughMoney(drawee, totalPrice + tax)) {
            Optional<MiscUtils.GiveStat> stat = plugin.eco.transaction(invoice.getBuyer(), invoice.getSeller(), drawee, invoice.getItemStack(), totalPrice, tax);
            if (!stat.isPresent()) {
                new Message("")
                        .append(
                                I18n.format("user.transaction.pay_fail", invoice.getId(), invoice.getSeller().getName(), totalPrice, tax),
                                invoice.getItemStack())
                        .send(drawee);
                return;
            }
            invoice = plugin.transactionManager.pay(drawee, invoice);
            Map<String, BaseComponent> componentMap = invoiceComponent(invoice);
            if (!drawee.getUniqueId().equals(invoice.getBuyerId())) {
                invoiceMessage(componentMap, "user.transaction.paid", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                        .send(drawee);
            }
            if (!drawee.getUniqueId().equals(invoice.getSellerId())) {
                invoiceMessage(componentMap, "user.transaction.paid_seller", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                        .send(invoice.getSeller(), isAfk(invoice.getSeller()));
            }
            invoiceMessage(componentMap, "user.transaction.paid_buyer", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                    .append(I18n.format("user.auc.item_given_" + stat.get().name()))
                    .send(invoice.getBuyer(), isAfk(invoice.getBuyer()));
        } else {
            msg(sender, "user.warn.no_enough_money");
        }
    }

    @SubCommand(value = "cancel", permission = "heh.transaction.cancel")
    public void cancel(CommandSender sender, Arguments args) {
        Invoice invoice;
        Player player = null;
        if (args.length() == 2) {
            player = asPlayer(sender);
            Collection<Invoice> invoicesBuy = plugin.transactionManager.activeBuy.get(player.getUniqueId());
            Collection<Invoice> invoicesSell = plugin.transactionManager.activeSell.get(player.getUniqueId());
            Iterator<Invoice> invoiceIterator = Iterables.concat(invoicesBuy, invoicesSell).iterator();
            if (!invoiceIterator.hasNext()) {
                msg(sender, "user.transaction.no_active");
                return;
            }
            invoice = invoiceIterator.next();
            if (invoiceIterator.hasNext()) {
                msg(sender, "user.transaction.multiple_active");
                listActiveInvoice(player, invoice, invoiceIterator);
                return;
            }
            invoice = plugin.transactionManager.query(invoice.getId());
        } else {
            if (sender instanceof Player) {
                player = asPlayer(sender);
            }
            invoice = getInvoice(sender, args);
            if (invoice == null) return;
        }
        if (!(
                sender.hasPermission("heh.admin") ||
                        (player != null &&
                                 Arrays.asList(invoice.getBuyerId(), invoice.getSellerId(), invoice.getDraweeId()).contains(player.getUniqueId())
                        )
        )) {
            msg(sender, "user.transaction.no_permission");
            return;
        }
        if (notDraft(sender, player, invoice)) return;
        invoice = plugin.transactionManager.cancel(invoice);
        MiscUtils.GiveStat stat = MiscUtils.giveItem(invoice.getSeller(), invoice.getItemStack());
        Map<String, BaseComponent> componentMap = invoiceComponent(invoice);
        invoiceMessage(componentMap, "user.transaction.cancelled_seller", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                .append(I18n.format("user.auc.item_given_" + stat.name()))
                .send(invoice.getSeller(), isAfk(invoice.getSeller()));
        invoiceMessage(componentMap, "user.transaction.cancelled_buyer", invoice.getId(), invoice.getTotalPrice(), invoice.getTax())
                .send(invoice.getBuyer(), isAfk(invoice.getBuyer()));
    }

    @SubCommand(value = "buyside", permission = "heh.transaction.buyside")
    public void queryBuyside(CommandSender sender, Arguments args) {
        OfflinePlayer player;
        if (args.top() != null && !args.top().matches("\\d+")) {
            if (!sender.hasPermission("heh.admin")) {
                msg(sender, "user.transaction.no_permission");
                return;
            }
            player = args.nextOfflinePlayer();
        } else {
            player = asPlayer(sender);
        }
        List<Invoice> buyerOpen = plugin.transactionManager.queryBuyerOpen(player);
        if (buyerOpen.isEmpty()) {
            msg(sender, "user.transaction.no_open_buy");
        } else {
            msg(sender, "user.transaction.open_buy");
            for (Invoice invoice : buyerOpen) {
                invoiceMessage(invoiceComponent(invoice), "user.transaction.open_invoice", invoice.getId(), invoice.getTotalPrice(), invoice.getTax()).send(sender);
            }
        }
        List<Invoice> buyerClosed = plugin.transactionManager.queryBuyerClosed(player);
        int page = 1;
        long perPage = plugin.config.transaction_list_closed_per_page;
        long total = (long) Math.ceil(buyerClosed.size() / (double) perPage);
        if (args.top() != null) {
            page = args.nextInt();
            if (page <= 0 || page > total) {
                page = 1;
            }
        }
        if (buyerClosed.isEmpty()) {
            msg(sender, "user.transaction.no_closed");
        } else {
            msg(sender, "user.transaction.closed_buy", page, total);
            listClosed(sender, buyerClosed, page, perPage);
        }
    }

    @SubCommand(value = "sellside", permission = "heh.transaction.sellside")
    public void querySellside(CommandSender sender, Arguments args) {
        OfflinePlayer player;
        if (args.top() != null && !args.top().matches("\\d+")) {
            if (!sender.hasPermission("heh.admin")) {
                msg(sender, "user.transaction.no_permission");
                return;
            }
            player = args.nextOfflinePlayer();
        } else {
            player = asPlayer(sender);
        }
        List<Invoice> sellerOpen = plugin.transactionManager.querySellerOpen(player);
        if (sellerOpen.isEmpty()) {
            msg(sender, "user.transaction.no_open_sell");
        } else {
            msg(sender, "user.transaction.open_sell");
            for (Invoice invoice : sellerOpen) {
                invoiceMessage(invoiceComponent(invoice), "user.transaction.open_invoice", invoice.getId(), invoice.getTotalPrice(), invoice.getTax()).send(sender);
            }
        }
        List<Invoice> sellerClosed = plugin.transactionManager.querySellerClosed(player);
        int page = 1;
        long perPage = plugin.config.transaction_list_closed_per_page;
        long total = (long) Math.ceil(sellerClosed.size() / (double) perPage);
        if (args.top() != null) {
            page = args.nextInt();
            if (page <= 0 || page > total) {
                page = 1;
            }
        }
        if (sellerClosed.isEmpty()) {
            msg(sender, "user.transaction.no_closed");
        } else {
            msg(sender, "user.transaction.closed_sell", page, total);
            listClosed(sender, sellerClosed, page, perPage);
        }
    }

    @SubCommand(value = "drawee", permission = "heh.transaction.drawee")
    public void queryDrawee(CommandSender sender, Arguments args) {
        OfflinePlayer player;
        if (args.top() != null && !args.top().matches("\\d+")) {
            if (!sender.hasPermission("heh.admin")) {
                msg(sender, "user.transaction.no_permission");
                return;
            }
            player = args.nextOfflinePlayer();
        } else {
            player = asPlayer(sender);
        }
        List<Invoice> draweeClosed = plugin.transactionManager.queryDraweeClosed(player);
        int page = 1;
        long perPage = plugin.config.transaction_list_closed_per_page;
        long total = (long) Math.ceil(draweeClosed.size() / (double) perPage);
        if (args.top() != null) {
            page = args.nextInt();
            if (page <= 0 || page > total) {
                page = 1;
            }
        }
        if (draweeClosed.isEmpty()) {
            msg(sender, "user.transaction.no_closed");
        } else {
            msg(sender, "user.transaction.closed_drawee", page, total);
            listClosed(sender, draweeClosed, page, perPage);
        }
    }

    private void listClosed(CommandSender sender, List<Invoice> buyerClosed, int page, long perPage) {
        for (Invoice invoice : buyerClosed.stream().skip((page - 1) * perPage).limit(perPage).collect(Collectors.toList())) {
            invoiceMessage(
                    invoiceComponent(invoice),
                    invoice.getState() == Invoice.COMPLETED ? "user.transaction.completed_invoice" : "user.transaction.cancelled_invoice",
                    invoice.getId(),
                    invoice.getTotalPrice(),
                    invoice.getTax()
            ).send(sender);
        }
    }

    private boolean notDraft(CommandSender sender, Player drawee, Invoice invoice) {
        if (invoice.getState() != Invoice.DRAFT) {
            if (drawee != null && Stream.of(invoice.getBuyerId(), invoice.getSellerId(), invoice.getDraweeId()).noneMatch(drawee.getUniqueId()::equals)) {
                msg(sender, "user.transaction.no_permission");
                return true;
            }
            invoiceMessage(invoiceComponent(invoice), invoice.getState() == Invoice.CANCELED ? "user.transaction.was_cancelled" : "user.transaction.was_completed", invoice.getId(), invoice.getTotalPrice(), invoice.getTax()).send(sender);
            return true;
        }
        return false;
    }

    private boolean isAfk(OfflinePlayer offlinePlayer) {
        if (!offlinePlayer.isOnline()) {
            return false;
        }
        if (plugin.ess == null) {
            return false;
        }
        return plugin.ess.getUser(offlinePlayer.getPlayer()).isAfk();
    }

    private void listActiveInvoice(Player player, Invoice invoice, Iterator<Invoice> invoiceIterator) {
        while (invoice != null) {
            invoiceMessage(invoiceComponent(invoice), "user.transaction.open_invoice", invoice.getId(), invoice.getTotalPrice(), invoice.getTax()).send(player);
            invoice = invoiceIterator.hasNext() ? invoiceIterator.next() : null;
        }
    }

    private Invoice getInvoice(CommandSender sender, Arguments args) {
        Invoice invoice;
        long id = args.nextLong();
        invoice = plugin.transactionManager.query(id);
        if (invoice == null) {
            msg(sender, "user.transaction.not_found", id);
            return null;
        }
        return invoice;
    }

    private Map<String, BaseComponent> invoiceComponent(Invoice invoice) {
        Map<String, BaseComponent> componentMap = new HashMap<>();
        ItemStack clone = invoice.getItemStack().clone();
        boolean hasCustomName = clone.hasItemMeta() && clone.getItemMeta().hasDisplayName();
        BaseComponent itemCom = hasCustomName ? new TextComponent(clone.getItemMeta().getDisplayName()) : LocaleUtils.getNameComponent(clone);
        itemCom.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(getItemJsonStripped(clone))}));
        componentMap.put("{amount}", new TextComponent(Integer.toString(clone.getAmount())));
        componentMap.put("{itemName}", itemCom);
        long updatedTime = invoice.getUpdatedTime();
        componentMap.put("{updatedTime}", new TextComponent(new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.forLanguageTag(plugin.config.language.replace('_', '-'))).format(updatedTime)));
        long createdTime = invoice.getCreatedTime();
        componentMap.put("{createdTime}", new TextComponent(new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.forLanguageTag(plugin.config.language.replace('_', '-'))).format(createdTime)));

        TextComponent buyerCom = new TextComponent(invoice.getBuyer().getName());
        buyerCom.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
                new ComponentBuilder(Message.getPlayerJson(invoice.getBuyer())).create()
        ));
        componentMap.put("{buyer}", buyerCom);

        TextComponent sellerCom = new TextComponent(invoice.getSeller().getName());
        sellerCom.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
                new ComponentBuilder(Message.getPlayerJson(invoice.getSeller())).create()
        ));
        componentMap.put("{seller}", sellerCom);

        if (invoice.getDraweeId() != null) {
            TextComponent draweeCom = new TextComponent(invoice.getDrawee().getName());
            draweeCom.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
                    new ComponentBuilder(Message.getPlayerJson(invoice.getDrawee())).create()
            ));
            componentMap.put("{drawee}", draweeCom);
        }
        return componentMap;
    }

    private Message invoiceMessage(Map<String, BaseComponent> componentMap, @LangKey String key, Object... args) {
        return new Message("").append(I18n.format(key, args), componentMap);
    }
}

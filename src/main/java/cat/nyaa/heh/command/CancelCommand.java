package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.direct.DirectInvoice;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class CancelCommand extends CommandReceiver implements ShortcutCommand{

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public CancelCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    private static final String PERMISSION_CANCEL = "heh.business.cancel";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_CANCEL, tabCompleter = "cancelCompleter")
    public void onCancel(CommandSender sender, Arguments arguments){
        long uid = arguments.nextLong();
        Player player = asPlayer(sender);
        UUID customer = DirectInvoice.getInstance().getCustomer(uid);
        if (!player.getUniqueId().equals(customer)){
            //parse customerName
            String customerName = customer == null ? null :
                    SystemAccountUtils.isSystemAccount(customer) ? SystemAccountUtils.getSystemName()
                            : Bukkit.getOfflinePlayer(customer).getName();
            new Message(I18n.format("command.cancel.invalid_invoice", uid, customerName)).send(sender);
            return;
        }
        ShopItem invoice = DirectInvoice.getInstance().getInvoice(uid);
        if (!invoice.getShopItemType().equals(ShopItemType.DIRECT)){
            new Message(I18n.format("command.cancel.not_invoice", uid)).send(sender);
            return;
        }
        if (!invoice.isAvailable()){
            new Message(I18n.format("command.cancel.canceled_invoice", uid)).send(sender);
            return;
        }

        DirectInvoice.getInstance().cancelInvoice(invoice);
        new Message(I18n.format("command.cancel.success", uid)).send(sender);
    }

    public List<String> cancelCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getShortcutName() {
        return "hc";
    }
}

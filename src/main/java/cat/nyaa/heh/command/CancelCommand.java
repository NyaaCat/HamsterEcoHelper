package cat.nyaa.heh.command;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.direct.DirectInvoice;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

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
        ShopItem invoice = DirectInvoice.getInstance().getInvoice(uid);
        UUID uniqueId = player.getUniqueId();
        if(invoice == null || !invoice.getShopItemType().equals(ShopItemType.DIRECT)){
            new Message(I18n.format("command.cancel.not_invoice", uid)).send(sender);
            return;
        }
        if (!player.isOp() && !uniqueId.equals(customer) && !uniqueId.equals(invoice.getOwner())){
            //parse customerName
            String customerName = customer == null ? null :
                    SystemAccountUtils.isSystemAccount(customer) ? SystemAccountUtils.getSystemName()
                            : Bukkit.getOfflinePlayer(customer).getName();
            new Message(I18n.format("command.cancel.invalid_invoice", uid, customerName)).send(sender);
            return;
        }
        if (!invoice.isAvailable()){
            new Message(I18n.format("command.cancel.canceled_invoice", uid)).send(sender);
            return;
        }
        try{
            ItemStack itemStack = invoice.getItemStack();
            itemStack.setAmount(invoice.getAmount() - invoice.getSoldAmount());
            UUID owner = invoice.getOwner();
            giveTo(itemStack, owner);
            DirectInvoice.getInstance().cancelInvoice(invoice);
            new Message(I18n.format("command.cancel.success", uid)).send(sender);
        } catch (Exception e) {
            HamsterEcoHelper.plugin.getLogger().log(Level.SEVERE, "error canceling invoice", e);
            new Message(I18n.format("command.cancel.failed", uid)).send(sender);
        }

    }

    private void giveTo(ItemStack itemStack, UUID owner) {
        Inventory targetInventory = null;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            if (player == null){
                giveToTempStorage(itemStack, offlinePlayer);
            }
            targetInventory = player.getInventory();
            if (giveTo(targetInventory, itemStack)) {
                new Message(I18n.format("item.give.inventory")).send(offlinePlayer);
            }else{
                giveToTempStorage(itemStack, offlinePlayer);
            }
        }else {
            giveToTempStorage(itemStack, offlinePlayer);
        }
    }

    private boolean giveToTempStorage(ItemStack itemStack, OfflinePlayer offlinePlayer) {
        try{
            StorageConnection.getInstance().getPlayerStorage(offlinePlayer.getUniqueId()).addItem(itemStack, 0);
            new Message(I18n.format("item.give.temp_storage")).send(offlinePlayer);
            return true;
        }catch (Exception e){
            HamsterEcoHelper.plugin.getLogger().log(Level.WARNING, "exception during giving item to temp storage", e);
            return false;
        }
    }

    private boolean giveTo(Inventory inventory, ItemStack itemStack) {
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (InventoryUtils.addItem(inventory, itemStack)){
                return true;
            }
        }
        return false;
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

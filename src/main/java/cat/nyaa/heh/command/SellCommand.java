package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.auction.Requisition;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class SellCommand extends CommandReceiver implements ShortcutCommand{
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public SellCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    private static final String PERMISSION_SELL = "heh.business.sell";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_SELL, tabCompleter = "sellCompleter")
    public void onSell(CommandSender sender, Arguments arguments){
        if (!Requisition.hasRequisition()) {
            new Message(I18n.format("command.sell.no_requisition")).send(sender);
            return;
        }
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        Requisition requisition = Requisition.currentRequisition();
        if (!requisition.isValidItem(itemInMainHand)) {
            new Message(I18n.format("command.sell.invalid_item")).send(sender);
            return;
        }
        String input = arguments.top();
        int amountToSell = 0;
        if (input.equals("all")){
            arguments.next();
            amountToSell = itemInMainHand.getAmount();
        }else {
            amountToSell = arguments.nextInt();
        }
        if (itemInMainHand.getAmount() < amountToSell){
            new Message(I18n.format("command.sell.insufficient_amount")).send(sender);
            return;
        }
        if (amountToSell<=0){
            new Message(I18n.format("command.sell.invalid_amount"));
            return;
        }
        int remains = requisition.remains();
        if (amountToSell > remains){
            new Message(I18n.format("command.sell.oversold", amountToSell, remains)).send(sender);
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack clone = itemInMainHand.clone();
        itemInMainHand.setAmount(Math.max(itemInMainHand.getAmount() - amountToSell, 0));
        inventory.setItemInMainHand(itemInMainHand);
        try{
            ItemStack clone1 = clone.clone();
            clone1.setAmount(amountToSell);
            requisition.onSell(player, clone1);
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "error selling item: ", e);
            inventory.setItemInMainHand(clone);
        }
    }

    public List<String> sellCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<amount>");
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getHelpPrefix() {
        return "sell";
    }

    @Override
    public String getShortcutName() {
        return "hsell";
    }
}

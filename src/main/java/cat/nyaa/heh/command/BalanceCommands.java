package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class BalanceCommands extends CommandReceiver {

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public BalanceCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    private static final String PERMISSION_BALANCE = "heh.admin.balance";

    @SubCommand(value = "view", permission = PERMISSION_BALANCE, tabCompleter = "playerCompleter")
    public void onView(CommandSender sender, Arguments arguments) {
        double balance;
        String name;
        if (arguments.top() == null){
            name = SystemAccountUtils.getSystemName();
            balance = SystemAccountUtils.getSystemBalance();
        }else {
            OfflinePlayer offlinePlayer = arguments.nextOfflinePlayer();
            name = offlinePlayer.getName();
            balance = EcoUtils.getInstance().getEco().getBalance(offlinePlayer);
        }
        new Message(I18n.format("command.balance.view", name, balance)).send(sender);
    }

    @SubCommand(value = "pay", permission = PERMISSION_BALANCE, tabCompleter = "playerCompleter")
    public void onPay(CommandSender sender, Arguments arguments) {
        OfflinePlayer offlinePlayer = arguments.nextOfflinePlayer();
        double amount = arguments.nextDouble();
        if (SystemAccountUtils.deposit(offlinePlayer, amount)) {
            new Message(I18n.format("command.balance.pay.success", offlinePlayer.getName(), amount)).send(sender);
        }else {
            new Message(I18n.format("command.balance.pay.failed", offlinePlayer.getName(), amount)).send(sender);
        }
    }

    @SubCommand(value = "take", permission = PERMISSION_BALANCE, tabCompleter = "playerCompleter")
    public void onTake(CommandSender sender, Arguments arguments) {
        OfflinePlayer offlinePlayer = arguments.nextOfflinePlayer();
        double amount = arguments.nextDouble();
        if (SystemAccountUtils.take(offlinePlayer, amount)) {
            new Message(I18n.format("command.balance.pay.success", offlinePlayer.getName(), amount)).send(sender);
        }else {
            new Message(I18n.format("command.balance.pay.failed", offlinePlayer.getName(), amount)).send(sender);
        }
    }

    public List<String> playerCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.addAll(CommandUtils.getOnlinePlayers());
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
}

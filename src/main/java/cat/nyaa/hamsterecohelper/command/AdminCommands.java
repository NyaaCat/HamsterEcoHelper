package cat.nyaa.hamsterecohelper.command;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cat.nyaa.hamsterecohelper.command.CommandUtils.filtered;

public class AdminCommands extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public AdminCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "balance", permission = "heh.admin.balance", tabCompleter = "balanceCompleter")
    public void onBalance(CommandSender sender, Arguments arguments){
        String action = arguments.next();
        switch (action){
            case "view":
                break;
            case "pay":
                Player payTo = arguments.nextPlayer();
                //todo
                break;
            case "take":
                Player takeFrom = arguments.nextPlayer();
                //todo
                break;
        }
    }

    public List<String> balanceCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        String top = arguments.top();
        if (top==null){
            completeStr.addAll(Arrays.asList("view", "pay", "take"));
        }else {
            switch (top){
                case "view":
                    break;
                case "pay":
                case "take":
                    if (arguments.remains() == 1){
                        completeStr.addAll(CommandUtils.getOnlinePlayers());
                    }else {
                        completeStr.add("<amount>");
                    }
                    break;
            }
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "remove", permission = "heh.admin.remove", tabCompleter = "removeCompleter")
    public void onRemove(CommandSender sender, Arguments arguments){
        String action = arguments.next();
        switch (action){
            case "item":
                String toDelete = arguments.nextString();

                break;
            case "shop":
                Player shopOwner= arguments.nextPlayer();

                //todo
                break;
            case "player":
                Player toRemovePlayer = arguments.nextPlayer();
                //todo
                break;
        }
    }

    public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
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
    public String getHelpPrefix() {
        return "";
    }
}

package cat.nyaa.heh.command;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class ShopCommands extends CommandReceiver {

    private static final String PERMISSION_SHOP = "heh.business.signshop";

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public ShopCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    @SubCommand(value = "sell", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onSell(CommandSender sender, Arguments arguments){
        double unitPrice = arguments.nextDouble();

    }

    @SubCommand(value = "buy", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onBuy(CommandSender sender, Arguments arguments){
        double unitPrice = arguments.nextDouble();

    }

    @SubCommand(value = "create", permission = PERMISSION_SHOP, tabCompleter = "createCompleter")
    public void onCreate(CommandSender sender, Arguments arguments){

    }

    public List<String> createCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("buy");
                completeStr.add("sell");
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> sellCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<unit price>");
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

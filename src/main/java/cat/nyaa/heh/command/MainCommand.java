package cat.nyaa.heh.command;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class MainCommand extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public MainCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        initSubCommands(plugin, _i18n);
    }

    private void initSubCommands(Plugin plugin, ILocalizer i18n) {
          auctionCommand = new AuctionCommand(plugin, i18n);
          balanceCommands = new BalanceCommands(plugin, i18n);
          bidCommand = new BidCommand(plugin, i18n);
//          businessCommands = new BusinessCommands(plugin, i18n);
          cancelCommand = new CancelCommand(plugin, i18n);
          chestCommands = new ChestCommands(plugin, i18n);
          marketCommands = new MarketCommands(plugin, i18n);
          payCommand = new PayCommand(plugin, i18n);
          requisitionCommand = new RequisitionCommand(plugin, i18n);
          sellCommand = new SellCommand(plugin, i18n);
          shopCommands = new ShopCommands(plugin, i18n);
          frameCommands = new FrameCommands(plugin, i18n);
          sellToCommand = new SellToCommand(plugin, i18n);
          searchCommand = new SearchCommand(plugin, i18n);
          if (HamsterEcoHelper.plugin.config.commandShortcutEnabled){
              CommandReceiver[] receivers = new CommandReceiver[]{
                      auctionCommand,
                      balanceCommands,
                      bidCommand,
//                      businessCommands,
                      cancelCommand,
                      chestCommands,
                      marketCommands,
                      payCommand,
                      requisitionCommand,
                      sellCommand,
                      sellToCommand,
                      shopCommands,
                      frameCommands,
                      searchCommand
              };
              registerShortcuts(receivers);
          }
    }

    private void registerShortcuts(CommandReceiver[] commandReceiver) {
        for (CommandReceiver receiver : commandReceiver) {
            if (receiver instanceof ShortcutCommand) {
                PluginCommand pluginCommand = Bukkit.getPluginCommand(((ShortcutCommand) receiver).getShortcutName());
                pluginCommand.setExecutor(receiver);
            }
        }
    }

    @SubCommand(value = "auc")
    AuctionCommand auctionCommand;
    @SubCommand(value = "bal")
    BalanceCommands balanceCommands;
    @SubCommand(value = "bid")
    BidCommand bidCommand;
//    @SubCommand(value = "auc")
//    BusinessCommands businessCommands;
    @SubCommand(value = "cancel")
    CancelCommand cancelCommand;
    @SubCommand(value = "chest")
    ChestCommands chestCommands;
    @SubCommand(value = "m")
    MarketCommands marketCommands;
    @SubCommand(value = "pay")
    PayCommand payCommand;
    @SubCommand(value = "req")
    RequisitionCommand requisitionCommand;
    @SubCommand(value = "sell")
    SellCommand sellCommand;
    @SubCommand(value = "sellto")
    SellToCommand sellToCommand;
    @SubCommand(value = "shop")
    ShopCommands shopCommands;
    @SubCommand(value = "frame")
    FrameCommands frameCommands;
    @SubCommand(value = "search")
    SearchCommand searchCommand;

    @SubCommand(value = "reload")
    public void onReload(CommandSender sender, Arguments arguments){
        HamsterEcoHelper.plugin.onReload();
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

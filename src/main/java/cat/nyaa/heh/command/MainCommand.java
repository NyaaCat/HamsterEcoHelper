package cat.nyaa.heh.command;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.market.Market;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class MainCommand extends CommandReceiver {
    private final String PERMISSION_MARKET = "heh.business.market";

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public MainCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "m", permission = PERMISSION_MARKET, tabCompleter = "marketCompleter")
    public void mShortcut(CommandSender sender, Arguments arguments) {
        onMarket(sender, arguments);
    }

    @SubCommand(value = "market", permission = PERMISSION_MARKET, tabCompleter = "marketCompleter")
    public void onMarket(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        if (arguments.top()==null){
            openMarketGUI(player);
            return;
        }
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("market.offer.no_item")).send(sender);
            return;
        }
        double unitPrice = arguments.nextDouble();
        if (unitPrice < 0 || !Double.isFinite(unitPrice)){
            new Message(I18n.format("market.offer.invalid_number", unitPrice)).send(sender);
            return;
        }
        double marketFeeBase = HamsterEcoHelper.plugin.config.marketFeeBase;
        Economy eco = EcoUtils.getInstance().getEco();
        if (!eco.has(player, marketFeeBase)) {
            new Message("").append(I18n.format("market.offer.insufficient_balance", marketFeeBase), itemInMainHand)
                    .send(sender);
            return;
        }
        eco.withdrawPlayer(player, marketFeeBase);
        new Message("").append(I18n.format("market.offer.withdraw", marketFeeBase), itemInMainHand).send(sender);

        Market.getInstance().offer(player, itemInMainHand, unitPrice);
        new Message("").append(I18n.format("market.offer.success", unitPrice), itemInMainHand).send(sender);
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
    }

    private List<String> marketCompleter(CommandSender sender, Arguments arguments){
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

    private void openMarketGUI(Player player) {
        Market.getInstance().openGUI(player);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}

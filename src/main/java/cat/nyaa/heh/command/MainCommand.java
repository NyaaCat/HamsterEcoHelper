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
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public MainCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
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

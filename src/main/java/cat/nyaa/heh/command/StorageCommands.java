package cat.nyaa.heh.command;

import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.ui.StorageGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class StorageCommands extends CommandReceiver implements ShortcutCommand{

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public StorageCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }
    private static final String PERMISSION_STORAGE = "heh.storage";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_STORAGE)
    public void onStorage(CommandSender sender, Arguments arguments){
        UUID owner;
        Player player = asPlayer(sender);
        if (sender.isOp()){
            Player player1 = arguments.nextPlayer();
            owner = player1.getUniqueId();
        }else{
            owner = player.getUniqueId();
        }
        StorageGUI g = UiManager.getInstance().newStorageGUI(owner);
        g.open(player);
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
        return "hstorage";
    }
}

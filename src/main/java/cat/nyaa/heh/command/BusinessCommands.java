package cat.nyaa.heh.command;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import org.bukkit.plugin.Plugin;

public class BusinessCommands extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public BusinessCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }



    @Override
    public String getHelpPrefix() {
        return null;
    }
}

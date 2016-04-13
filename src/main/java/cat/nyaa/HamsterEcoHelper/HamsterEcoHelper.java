package cat.nyaa.HamsterEcoHelper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HamsterEcoHelper extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        return;
    }

    @Override
    public void onDisable() {
        return;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = (Player) sender;

        if (commandLabel.equalsIgnoreCase("heh") && args.length > 0 && player.hasPermission("heh.user")) {
            return true;
        }

        if (commandLabel.equalsIgnoreCase("hehadmin") && args.length > 0 && player.hasPermission("heh.admin")) {
            if (args[0].equalsIgnoreCase("debug") && args.length > 1) {
                if (args[1].equalsIgnoreCase("showitem")) {
                    ShowItem t = new ShowItem();
                    t.setItem(player.getInventory().getItemInMainHand());
                    t.sendToPlayer(player);
                    return true;
                }
            }
        }
        return true;
    }
}





package cat.nyaa.HamsterEcoHelper;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HamsterEcoHelper extends JavaPlugin {
    public Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        getCommand("hemsterecohelper").setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        return;
    }
}





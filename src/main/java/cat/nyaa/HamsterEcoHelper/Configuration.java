package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends PluginConfigure {

    @Override
    protected JavaPlugin getPlugin() {
        return HamsterEcoHelper.instance;
    }
}

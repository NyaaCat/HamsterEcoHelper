package cat.nyaa.HamsterEcoHelper.balance;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.utils.FileConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class VariablesConfig extends FileConfigure {
    private final HamsterEcoHelper plugin;
    @Serializable
    public double balance = 0;
    @Serializable
    public long market_placement_fee_timestamp = 0;

    public VariablesConfig(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "variables.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }
    
}

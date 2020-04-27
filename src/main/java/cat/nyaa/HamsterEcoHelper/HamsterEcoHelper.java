package cat.nyaa.HamsterEcoHelper;

import org.bukkit.plugin.java.JavaPlugin;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
    }

    public void reload() {

    }
}





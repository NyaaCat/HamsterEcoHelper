package cat.nyaa.heh;

import org.bukkit.event.Listener;

public class Events implements Listener {
    private final HamsterEcoHelper plugin;

    public Events(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}

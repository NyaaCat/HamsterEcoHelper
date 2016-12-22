package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.utils.Internationalization;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends Internationalization {
    public static I18n instance = null;
    private final HamsterEcoHelper plugin;
    private String lang = null;

    public I18n(HamsterEcoHelper plugin, String lang) {
        instance = this;
        this.plugin = plugin;
        this.lang = lang;
        load();
    }

    public static String _(String key, Object... args) {
        return instance.get(key, args);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getLanguage() {
        return lang;
    }
}

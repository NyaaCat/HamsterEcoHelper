package cat.nyaa.HamsterEcoHelper;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class I18n {
    private static final String DEFAULT_LANGUAGE = "en_US";
    private static final Map<String, String> map = new HashMap<>();
    private static String lang = null;
    private static HamsterEcoHelper plugin;

    private static void appendStrings(JavaPlugin plugin, ConfigurationSection section) {
        appendStrings(plugin, section, "");
    }

    private static void appendStrings(JavaPlugin plugin, ConfigurationSection section, String prefix) {
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                if (map.containsKey(path)) {
                    plugin.getLogger().warning("Duplicated language key: " + key);
                    plugin.getLogger().warning("Overridden value: " + map.get(path));
                }
                map.put(path, ChatColor.translateAlternateColorCodes('&', section.getString(key)));
            } else if (section.isConfigurationSection(key)) {
                appendStrings(plugin, section.getConfigurationSection(key), path + ".");
            } else {
                plugin.getLogger().warning("Skipped language section: " + path);
            }
        }

    }

    public static void load(HamsterEcoHelper plugin, String language) {
        I18n.plugin = plugin;
        File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
        if (!localLangFile.exists()) {
            InputStream stream = plugin.getResource("lang/" + language + ".yml");
            if (stream != null) {
                lang = language;
                appendStrings(plugin, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
                try {
                    stream = plugin.getResource("lang/" + language + ".yml");
                    Files.copy(stream, localLangFile.toPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    plugin.getLogger().warning(I18n.get("internal.warn.unable_save_lang"));
                }
                plugin.getLogger().info(get("internal.info.using_language", lang));
            } else if ((stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml")) != null) {
                lang = DEFAULT_LANGUAGE;
                appendStrings(plugin, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
                plugin.getLogger().warning(get("internal.warn.lang_not_found", language));
                plugin.getLogger().info(get("internal.info.using_language", lang));
            } else {
                plugin.getLogger().severe(String.format("Language %s not found. Default Language %s not found. Failed to load.", lang, DEFAULT_LANGUAGE));
                throw new RuntimeException("No language file available.");
            }
        } else {
            lang = language;
            appendStrings(plugin, YamlConfiguration.loadConfiguration(localLangFile));
            plugin.getLogger().info(get("internal.info.using_language", lang));
        }

    }

    public static String get(String key, Object... para) {
        String val = map.get(key);
        if (val == null) {
            I18n.plugin.getLogger().warning("Missing language key: " + key);
            key = "MISSING_LANG<" + key + ">";
            for (Object obj : para) {
                key += "#<" + obj.toString() + ">";
            }
            return key;
        } else {
            return String.format(val, para);
        }
    }

    public static void reset() {
        map.clear();
        lang = null;
        plugin = null;
    }
}

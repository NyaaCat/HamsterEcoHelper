package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionItemTemplate;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionSpecification;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Configuration {
    private final HamsterEcoHelper plugin;

    @Serializable
    public String language = "en_US";
    @Serializable
    public int auctionIntervalTicks = 60 * 60 * 20; // 1 hour
    @Serializable
    public int requisitionIntervalTicks = 60 * 60 * 20; // 1 hour

    public List<AuctionItemTemplate> itemsForAuction;
    public List<RequisitionSpecification> itemsForReq;

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
    }

    public void loadFromPlugin() {
        boolean firstRun = !(new File(plugin.getDataFolder(),"config.yml").exists());
        plugin.saveDefaultConfig();
        deserialize(plugin.getConfig(), this);

        itemsForAuction = new ArrayList<>();
        YamlConfiguration tmp = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"auction-items.yml"));
        if(tmp!=null) {
            for (String idx : tmp.getKeys(false)) {
                itemsForAuction.add(AuctionItemTemplate.fromConfig(tmp.getConfigurationSection(idx)));
            }
        }

        itemsForReq = new ArrayList<>();
        tmp = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"requisition-items.yml"));
        if(tmp!=null) {
            for (String idx : tmp.getKeys(false)) {
                itemsForReq.add(RequisitionSpecification.fromConfig(tmp.getConfigurationSection(idx)));
            }
        }

        if (firstRun) saveToPlugin();
    }

    public void saveToPlugin() {
        serialize(plugin.getConfig(), this);

        YamlConfiguration aucTmp = new YamlConfiguration();
        for (int i = 0; i < itemsForAuction.size(); i++) {
            itemsForAuction.get(i).dumpTo(aucTmp.createSection(Integer.toString(i)));
        }
        YamlConfiguration reqTmp = new YamlConfiguration();
        for (int i = 0; i < itemsForReq.size(); i++) {
            itemsForReq.get(i).dumpTo(reqTmp.createSection(Integer.toString(i)));
        }

        try {
            aucTmp.save(new File(plugin.getDataFolder(),"auction-items.yml"));
            reqTmp.save(new File(plugin.getDataFolder(),"requisition-items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        serialize(plugin.getConfig(), this);
        plugin.saveConfig();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Serializable {
        String name() default "";
    }

    public static void deserialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        for (Field f : clz.getDeclaredFields()) {
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("")? f.getName(): anno.name();
            try {
                Object origValue = f.get(obj);
                Object newValue;
                if (f.getType().isEnum()) {
                    try {
                        newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), config.getString(cfgName));
                    } catch (Exception ex) {
                        newValue = origValue;
                    }
                } else {
                    newValue = config.get(cfgName, origValue);
                }
                f.set(obj, newValue);
            } catch (ReflectiveOperationException ex) {
                HamsterEcoHelper.instance.logger.log(Level.SEVERE, "Failed to deserialize object", ex);
            }
        }
    }

    public static void serialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        for (Field f : clz.getDeclaredFields()) {
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("")? f.getName(): anno.name();
            try {
                if (f.getType().isEnum()) {
                    Enum e = (Enum)f.get(obj);
                    config.set(cfgName, e.name());
                } else {
                    Object origValue = f.get(obj);
                    config.set(cfgName, origValue);
                }
            } catch (ReflectiveOperationException ex) {
                HamsterEcoHelper.instance.logger.log(Level.SEVERE, "Failed to serialize object", ex);
            }
        }
    }
}

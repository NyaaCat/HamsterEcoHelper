package cat.nyaa.HamsterEcoHelper.ads;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdsConfig extends FileConfigure {
    public HashMap<Integer, AdsData> adsDataList = new HashMap<>();
    @Serializable
    public List<String> muteList = new ArrayList<>();
    @Serializable
    public int pos = 0;
    private HamsterEcoHelper plugin;

    public AdsConfig(HamsterEcoHelper pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "ads.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        adsDataList.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("ads")) {
            ConfigurationSection ads = config.getConfigurationSection("ads");
            for (String idx : ads.getKeys(false)) {
                AdsData tmp = new AdsData();
                tmp.deserialize(ads.getConfigurationSection(idx));
                adsDataList.put(tmp.id, tmp);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        for (String k : config.getKeys(false)) {
            config.set(k, null);
        }
        ISerializable.serialize(config, this);
        config.set("ads", null);
        ConfigurationSection ads = config.createSection("ads");
        for (AdsData ad : adsDataList.values()) {
            ad.serialize(ads.createSection(Integer.toString(ad.id)));
        }
    }
}

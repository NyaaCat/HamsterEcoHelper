package cat.nyaa.HamsterEcoHelper.data;

import org.bukkit.configuration.ConfigurationSection;

public interface ISerializable {
    void loadFrom(ConfigurationSection s);

    void dumpTo(ConfigurationSection s);
}

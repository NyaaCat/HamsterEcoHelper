package cat.nyaa.hamsterecohelper.db;

import cat.nyaa.hamsterecohelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.orm.backends.BackendConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseConfig extends FileConfigure {
    @Serializable
    public BackendConfig backendConfig = BackendConfig.sqliteBackend(HamsterEcoHelper.plugin.getName()+".db");

    @Override
    protected String getFileName() {
        return "database.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return HamsterEcoHelper.plugin;
    }
}

package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.validation.NotNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import java.util.*;

public class Database {
    private final HamsterEcoHelper plugin;
    private final EbeanServer db;

    public Database(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        db = plugin.getDatabase();
        try {
            db.find(TempStorageRepo.class).findRowCount();
        } catch (PersistenceException ex) {
            plugin.logger.info(I18n.get("internal.info.installing_db"));
            plugin.installDDL();
        }
    }

    public EbeanServer getDB() {
        return db;
    }

    public List<ItemStack> getTemporaryStorage(OfflinePlayer player) {
        TempStorageRepo result = db.find(TempStorageRepo.class, player.getUniqueId());
        if (result == null) return Collections.emptyList();
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(result.yaml);
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
        List<ItemStack> ret = new ArrayList<>();
        for (String key : cfg.getKeys(false)) {
            ret.add(cfg.getItemStack(key));
        }
        return ret;
    }

    public void addTemporaryStorage(OfflinePlayer player, ItemStack item) {
        TempStorageRepo result = db.find(TempStorageRepo.class, player.getUniqueId());
        YamlConfiguration cfg = new YamlConfiguration();
        boolean update;
        if (result == null) {
            update = false;
            cfg.set("0", item);
        } else {
            update = true;
            YamlConfiguration tmp = new YamlConfiguration();
            try {
                tmp.loadFromString(result.yaml);
            } catch (InvalidConfigurationException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            List<ItemStack> items = new ArrayList<>();
            for (String key : tmp.getKeys(false)) {
                items.add(tmp.getItemStack(key));
            }
            items.add(item);

            for (int i = 0; i < items.size(); i++) {
                cfg.set(Integer.toString(i), items.get(i));
            }
        }

        TempStorageRepo bean = new TempStorageRepo();
        bean.playerId = player.getUniqueId();
        bean.yaml = cfg.saveToString();
        if (update) {
            db.update(bean);
        } else {
            db.insert(bean);
        }
    }

    public void clearTemporaryStorage(OfflinePlayer player) {
        db.delete(TempStorageRepo.class, player.getUniqueId());
    }

    public static List<Class<?>> getDatabaseClasses() {
        return new ArrayList<Class<?>>(){{
            add(TempStorageRepo.class);
        }};
    }

    @Entity
    @Table(name = "temporary_storage")
    public static class TempStorageRepo {
        @Id
        @NotNull
        public UUID playerId = UUID.randomUUID();
        @NotNull
        public String yaml = "";

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID playerId) {
            this.playerId = playerId;
        }

        public String getYaml() {
            return Base64.getEncoder().encodeToString(yaml.getBytes());
        }

        public void setYaml(String yaml) {
            this.yaml = new String(Base64.getDecoder().decode(yaml));
        }
    }
}

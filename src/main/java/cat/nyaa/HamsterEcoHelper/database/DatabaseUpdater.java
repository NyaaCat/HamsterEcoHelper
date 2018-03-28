package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class DatabaseUpdater {
    public static int updateDatabase(Database db, int currentVersion) {
        if (currentVersion == 0) {
            System.out.println("Updating database, this may take some time");
            updateVersion0To1(db);
            currentVersion = 1;
        }
        return currentVersion;
    }

    public static String convertYamlItemStackToNbtItemStack(String base64dYaml) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(new String(Base64.getDecoder().decode(base64dYaml)));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        ItemStack itemStack = yaml.getItemStack("item");
        return ItemStackUtils.itemToBase64(itemStack);
    }

    /**
     * Convert all yaml based item to NBT based
     */
    public static void updateVersion0To1(Database db) {
        int total, counter = 0;
        total = db.database.query(ItemLog.class).count();
        for (ItemLog record : db.database.query(ItemLog.class).select()) {
            if (((++counter) % 100) == 0)
                System.out.println(String.format("Processing ItemLog %d/%d", counter, total));
            String base64dItemNbt = convertYamlItemStackToNbtItemStack(record.item);
            if (base64dItemNbt == null) {
                db.database.query(ItemLog.class).whereEq("id", record.id).delete();
            } else {
                record.item = base64dItemNbt;
                db.database.query(ItemLog.class).whereEq("id", record.id).update(record, "item");
            }
        }

        counter = 0;
        total = db.database.query(MarketItem.class).count();
        for (MarketItem record : db.database.query(MarketItem.class).select()) {
            if (((++counter) % 100) == 0)
                System.out.println(String.format("Processing MarketItem %d/%d", counter, total));
            String base64dItemNbt = convertYamlItemStackToNbtItemStack(record.item);
            if (base64dItemNbt == null) {
                db.database.query(MarketItem.class).whereEq("id", record.id).delete();
            } else {
                record.item = base64dItemNbt;
                db.database.query(MarketItem.class).whereEq("id", record.id).update(record, "item");
            }
        }
    }
}

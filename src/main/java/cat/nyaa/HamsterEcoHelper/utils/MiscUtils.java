package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class MiscUtils {
    public static final Random random = new Random();

    public static <T> int randomIdWithWeight(List<T> items, Function<T, Double> weightOperator) {
        if (items.size() <= 0) return -1;

        double[] weightList = new double[items.size()];
        weightList[0] = weightOperator.apply(items.get(0));
        for (int i = 1; i < items.size(); i++) {
            weightList[i] = weightList[i - 1] + weightOperator.apply(items.get(i));
        }

        double rnd = random.nextDouble() * weightList[weightList.length - 1];
        for (int i = 0; i < weightList.length; i++) {
            if (weightList[i] > rnd) {
                return i;
            }
        }

        throw new RuntimeException("No item selected: Please report this BUG");
    }

    public static <T> T randomWithWeight(List<T> items, Function<T, Double> weightOperator) {
        if (items.size() <= 0) return null;
        return items.get(randomIdWithWeight(items, weightOperator));
    }

    public static int inclusiveRandomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Put Item into player's inventory/ender chest/temporary-storage-zone
     *
     * @return 1: put into player's inventory
     * 2: put into ender chest
     * 3: put into temporary storage
     */
    public static GiveStat giveItem(OfflinePlayer player, ItemStack item) {
        if (player.isOnline()) {
            Player p = Bukkit.getPlayer(player.getUniqueId());  // Refresh the Player object to ensure
            // we hold latest Player object associated to
            if (InventoryUtils.addItem(p, item)) {
                return GiveStat.INVENTORY;
            }
            if (InventoryUtils.addItem(p.getEnderChest(), item)) {
                return GiveStat.ENDER_CHEST;
            }
        }

        HamsterEcoHelper.instance.database.addTemporaryStorage(player, item);
        return GiveStat.TEMPORARY_STORAGE;
    }

    public static String uid(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    public static String getItemName(ItemStack item) {
        String itemName = "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().getDisplayName();
        }
        if (itemName.length() == 0) {
            itemName = item.getType().name() + ":" + item.getDurability();
        } else {
            itemName += "(" + item.getType().name() + ":" + item.getDurability() + ")";
        }
        return itemName;
    }

    public static Material getMaterial(String name) {
        return Material.matchMaterial(name);
    }

    public static Material getMaterial(String name, CommandSender sender) {
        Material m = Material.matchMaterial(name, false);
        if (m == null) {
            m = Material.matchMaterial(name, true);
            if (m != null) {
                //noinspection deprecation
                m = Bukkit.getUnsafe().fromLegacy(m);
                sender.sendMessage(I18n.instance.getFormatted("user.warn.legacy_name", name, m.toString()));
            }
        }
        return m;
    }

    @LangKey(type = LangKeyType.SUFFIX)
    public enum GiveStat {
        INVENTORY,
        ENDER_CHEST,
        TEMPORARY_STORAGE
    }
}

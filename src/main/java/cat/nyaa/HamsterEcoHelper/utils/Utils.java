package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Utils {
    public static final Random random = new Random();

    public static <T> int randomIdWithWeight(List<T> items, Function<T, Double> weightOperator) {
        if (items.size() <= 0) return -1;

        double[] weightList = new double[items.size()];
        weightList[0] = weightOperator.apply(items.get(0));
        for (int i = 1; i< items.size(); i++) {
            weightList[i] = weightList[i-1] + weightOperator.apply(items.get(i));
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
     * @return 1: put into player's inventory
     *         2: put into ender chest
     *         3: put into temporary storage
     */
    public static int giveItem(OfflinePlayer player, ItemStack item) {
        if (player.isOnline() && player instanceof Player) {
            Player p = (Player) player;
            int emptyId = p.getInventory().firstEmpty();
            if (emptyId >= 0) {
                p.getInventory().setItem(emptyId, item);
                return 1;
            }

            emptyId = p.getEnderChest().firstEmpty();
            if (emptyId >= 0) {
                p.getEnderChest().setItem(emptyId, item);
                return 2;
            }
        }

        HamsterEcoHelper.instance.database.addTemporaryStorage(player, item);
        return 3;
    }
}

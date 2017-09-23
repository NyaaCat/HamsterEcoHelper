package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.ReflectionUtils;
import com.google.common.io.BaseEncoding;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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

    @LangKey(type = LangKeyType.SUFFIX) public enum GiveStat {
        INVENTORY,
        ENDER_CHEST,
        TEMPORARY_STORAGE
    }

    /**
     * Put Item into player's inventory/ender chest/temporary-storage-zone
     * @return 1: put into player's inventory
     *         2: put into ender chest
     *         3: put into temporary storage
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

    public static String encodeItemStack(ItemStack item) {
        byte[] nbt = ItemStackUtils.toBinaryNbt(item);
        byte[] compressedNbt = null;
        try {
            Deflater compresser = new Deflater();
            compresser.setInput(nbt);
            compresser.finish();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (!compresser.finished()) {
                byte[] buf = new byte[1024];
                int len = compresser.deflate(buf);
                bos.write(buf, 0, len);
            }
            compresser.end();
            bos.close();
            compressedNbt = bos.toByteArray();
            return BaseEncoding.base64().encode(compressedNbt);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ItemStack decodeItemStack(String item) {
        byte[] nbt;
        byte[] compressedNbt = BaseEncoding.base64().decode(item);

        try {
            Inflater decompresser = new Inflater();
            decompresser.setInput(compressedNbt);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (!decompresser.finished()) {
                byte[] buf = new byte[1024];
                int len = decompresser.inflate(buf);
                bos.write(buf, 0, len);
            }
            decompresser.end();
            bos.close();
            nbt = bos.toByteArray();
            return ItemStackUtils.fromBinaryNbt(nbt);
        } catch (DataFormatException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encodeItemStacks(List<ItemStack> items) {
        if (items.size() <= 0) return "";
        StringBuilder b = new StringBuilder();
        b.append(encodeItemStack(items.get(0)));
        for (int i=1;i<items.size();i++) {
            b.append(",");
            b.append(encodeItemStack(items.get(i)));
        }
        return b.toString();
    }

    public static List<ItemStack> decodeItemStacks(String items) {
        if (items.length() <= 0) return new ArrayList<>();
        String[] a = items.split(",");
        List<ItemStack> r = new ArrayList<>();
        for (String str : a) r.add(decodeItemStack(str));
        return r;
    }
}

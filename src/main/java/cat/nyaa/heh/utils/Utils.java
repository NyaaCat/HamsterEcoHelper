package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static TaskChainFactory factory;

    public static String colored(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static TaskChain<?> newChain(){
        if (factory == null) {
            factory = BukkitTaskChainFactory.create(HamsterEcoHelper.plugin);
        }
        return factory.newChain();
    }

    public static String replacePlaceHolder(Map<String, String> placeHolderMap, String format) {
        return new StrSubstitutor(placeHolderMap, "{", "}", '\\').replace(format);
    }

    public static <T> T randomSelect(Collection<T> collection){
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int size = collection.size();
        int rnd = ThreadLocalRandom.current().nextInt(size);
        return collection.stream().skip(rnd).findFirst().orElse(null);
    }
}

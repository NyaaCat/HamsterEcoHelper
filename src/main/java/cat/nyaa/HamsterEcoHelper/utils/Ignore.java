package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ignore {
    private static List<UUID> ignoreList;

    public static void init() {
        ignoreList = new ArrayList<>();
    }

    public static void add(Player player) {
        if (!ignoreList.contains(player.getUniqueId())) {
            ignoreList.add(player.getUniqueId());
        }
    }

    public static void remove(Player player) {
        if (ignoreList.contains(player.getUniqueId())) {
            ignoreList.remove(player.getUniqueId());
        }
    }

    public static List<UUID> getList() {
        List<UUID> tmp = new ArrayList<>();
        for (UUID p : ignoreList) {
            if (Bukkit.getOfflinePlayer(p).isOnline()) {
                tmp.add(p);
            }
        }
        return tmp;
    }
}

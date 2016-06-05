package cat.nyaa.HamsterEcoHelper.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mute {
    private static List<UUID> list;

    public static void init() {
        list = new ArrayList<>();
    }

    public static void add(Player player) {
        if (!list.contains(player.getUniqueId())) {
            list.add(player.getUniqueId());
        }
    }

    public static void remove(Player player) {
        if (list.contains(player.getUniqueId())) {
            list.remove(player.getUniqueId());
        }
    }

    public static List<UUID> getList() {
        List<UUID> tmp = new ArrayList<>();
        for (UUID p : list) {
            if (Bukkit.getOfflinePlayer(p).isOnline()) {
                tmp.add(p);
            }
        }
        return tmp;
    }
}

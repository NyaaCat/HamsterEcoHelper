package cat.nyaa.HamsterEcoHelper.ads;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

public class AdsManager extends BukkitRunnable {
    public static String formatCode = "";
    private HamsterEcoHelper plugin;

    public AdsManager(HamsterEcoHelper pl) {
        this.plugin = pl;
        formatCode = "";
        if (plugin.config.ads_color) {
            formatCode += "0123456789AaBbCcDdEeFf";
        }
        for (String s : plugin.config.ads_formatting) {
            formatCode += s.toUpperCase();
            formatCode += s.toLowerCase();
        }
        runTaskTimer(plugin, 1, plugin.config.ads_interval);
    }

    public static String getMessage(String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && formatCode.indexOf(b[i + 1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public boolean isAFK(Player player) {
        if (plugin.ess == null) {
            return false;
        }
        return plugin.ess.getUser(player).isAfk();
    }

    public int getLimit(Player player) {
        int tmp = 0;
        for (String k : plugin.config.ads_limit_group.keySet()) {
            if (player.hasPermission("heh.ads.limit_group." + k)) {
                if (plugin.config.ads_limit_group.get(k) > tmp) {
                    tmp = plugin.config.ads_limit_group.get(k);
                }
            }
        }
        return tmp;
    }

    public int getPlayerAdsCount(Player player) {
        int count = 0;
        for (AdsData ad : plugin.config.adsConfig.adsDataList.values()) {
            if (player.getUniqueId().equals(ad.getUUID())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void run() {
        if (plugin.config.adsConfig.adsDataList.isEmpty() || plugin.getServer().getOnlinePlayers().isEmpty()) {
            return;
        }
        ArrayList<UUID> players = new ArrayList<UUID>();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.config.adsConfig.muteList.contains(p.getUniqueId().toString())) {
                continue;
            }
            players.add(p.getUniqueId());
        }
        Iterator<UUID> it = players.iterator();
        while (it.hasNext()) {
            ArrayList<Integer> tmp = new ArrayList<>();
            for (AdsData ad : plugin.config.adsConfig.adsDataList.values()) {
                if (!plugin.config.adsConfig.muteList.contains(ad.playerUUID)) {
                    tmp.add(ad.id);
                }
            }
            Collections.shuffle(tmp);
            if (tmp.isEmpty() || players.isEmpty()) {
                break;
            }
            AdsData ads = plugin.config.adsConfig.adsDataList.get(tmp.get(0));
            OfflinePlayer adOwner = plugin.getServer().getOfflinePlayer(ads.getUUID());
            int displayCount = 0;
            while (it.hasNext()) {
                UUID uuid = it.next();
                Player player = plugin.getServer().getPlayer(uuid);
                player.sendMessage(I18n.format("user.ads.message", adOwner.getName(), getMessage(ads.text)));
                it.remove();
                if ((!plugin.config.ads_count_afk && isAFK(player)) ||
                        (!plugin.config.ads_count_self && player.getUniqueId().equals(ads.getUUID()))) {
                } else {
                    ads.displayed++;
                    displayCount++;
                }
                if (ads.displayed >= ads.display_total) {
                    break;
                }
            }
            if (displayCount > 0) {
                plugin.getServer().getConsoleSender().
                        sendMessage(I18n.format("user.ads.message", adOwner.getName(), getMessage(ads.text)));
            }
            if (ads.displayed >= ads.display_total) {
                plugin.config.adsConfig.adsDataList.remove(tmp.get(0));
                plugin.logger.info(I18n.format("log.info.ads_remove", plugin.config.adsConfig.pos, ads.text,
                        plugin.getServer().getOfflinePlayer(ads.getUUID()).getName()));
                plugin.config.adsConfig.save();
                continue;
            }

        }
    }
}

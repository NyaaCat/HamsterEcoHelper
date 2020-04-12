package cat.nyaa.HamsterEcoHelper.market;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.UUID;

public class MarketCache {
    public static LoadingCache<UUID, String> playerName = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .build(new CacheLoader<UUID, String>() {
                public String load(UUID key) {
                    return Optional.ofNullable(Bukkit.getOfflinePlayer(key).getName()).orElse(key.toString());
                }
            });

    public static LoadingCache<UUID, Integer> playerItemCount = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .build(new CacheLoader<UUID, Integer>() {
                public Integer load(UUID key) {
                    return HamsterEcoHelper.instance.database.getMarketPlayerItemCount(key);
                }
            });
    public static boolean needUpdateItemCount = true;
    private static int itemCount = 0;

    public static int getMarketItemCount() {
        if (needUpdateItemCount) {
            itemCount = HamsterEcoHelper.instance.database.getMarketItemCount();
            needUpdateItemCount = false;
        }
        return itemCount;
    }
}

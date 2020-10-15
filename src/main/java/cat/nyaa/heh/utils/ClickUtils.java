package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClickUtils {
    private static Map<String, ClickUtils> clickMap = new HashMap<>();

    public static ClickUtils get(String business){
        return clickMap.computeIfAbsent(business, s -> new ClickUtils());
    }

    private Cache<UUID, ClickTask> clickTaskMap = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public boolean isMultiClick(UUID uuid){
        ClickTask ifPresent = clickTaskMap.getIfPresent(uuid);
        return ifPresent != null && ifPresent.isValidClick();
    }

    public void click(UUID uuid, int interval){
        try {
            ClickTask clickTask = clickTaskMap.get(uuid, () -> new ClickTask(uuid, System.currentTimeMillis(), interval));
            clickTask.resubmit();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    final class ClickTask extends BukkitRunnable {
        private UUID uuid;
        private long startTime;
        private int mUserClickInterval;

        public ClickTask(UUID uuid, long currentTimeMillis, int interval) {
            this.uuid = uuid;
            this.startTime = currentTimeMillis;
            this.mUserClickInterval = interval;
        }

        public void resubmit(){
            try{
                cancel();
            }catch (IllegalStateException e){}

            ClickTask task = new ClickTask(uuid, startTime, mUserClickInterval);
            task.runLater();
        }

        private void runLater() {
            this.runTaskLater(HamsterEcoHelper.plugin, mUserClickInterval);
        }

        public boolean isValidClick(){
            long now = System.currentTimeMillis();
            long userClickInterval = (now - startTime) / 50;
            return userClickInterval <= mUserClickInterval;
        }

        @Override
        public void run() {
            clickTaskMap.invalidate(uuid);
        }
    }
}

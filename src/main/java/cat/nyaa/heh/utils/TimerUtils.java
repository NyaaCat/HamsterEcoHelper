package cat.nyaa.heh.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerUtils {
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    public static ScheduledFuture<?> submitTimerTask(Runnable runnable, long firstTime, long period){
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable, firstTime, period, TimeUnit.MILLISECONDS);
        return scheduledFuture;
    }

    public static ScheduledFuture<?> submitDailyTask(Runnable runnable){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long period = TimeUnit.DAYS.toMillis(1);
        Date now = Date.from(Instant.now());
        Date todayAtZero = calendar.getTime();
        long delay = period - (todayAtZero.toInstant().until(now.toInstant(), ChronoUnit.MILLIS));
        return submitTimerTask(runnable, delay, period);
    }
}

package cat.nyaa.HamsterEcoHelper.balance;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.utils.IPCUtils;
import org.bukkit.scheduler.BukkitRunnable;

public class BalanceAPI extends BukkitRunnable {

    public BalanceAPI(HamsterEcoHelper pl) {
        try {
            IPCUtils.registerMethod("heh_balance_enabled", BalanceAPI.class.getMethod("isEnabled"));
            IPCUtils.registerMethod("heh_balance_get", BalanceAPI.class.getMethod("getBalance"));
            IPCUtils.registerMethod("heh_balance_set", BalanceAPI.class.getMethod("setBalance", double.class));
            IPCUtils.registerMethod("heh_balance_deposit", BalanceAPI.class.getMethod("deposit", double.class));
            IPCUtils.registerMethod("heh_balance_withdraw", BalanceAPI.class.getMethod("withdraw", double.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        runTaskTimer(pl, 20, pl.config.balance_SaveIntervalTicks);
    }

    public static boolean isEnabled() {
        return HamsterEcoHelper.instance.config.enable_balance;
    }

    public static double getBalance() {
        return HamsterEcoHelper.instance.config.variablesConfig.balance;
    }

    public static double setBalance(double money) {
        return HamsterEcoHelper.instance.config.variablesConfig.balance = money;
    }

    public static void withdraw(double money) {
        if (isEnabled() && money > 0.0D) {
            HamsterEcoHelper.instance.config.variablesConfig.balance -= money;
        }
    }

    public static void deposit(double money) {
        if (isEnabled() && money > 0.0D) {
            HamsterEcoHelper.instance.config.variablesConfig.balance += money;
        }
    }

    @Override
    public void run() {
        HamsterEcoHelper.instance.config.variablesConfig.save();
    }
}

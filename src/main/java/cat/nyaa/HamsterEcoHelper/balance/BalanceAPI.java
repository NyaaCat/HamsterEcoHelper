package cat.nyaa.HamsterEcoHelper.balance;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.scheduler.BukkitRunnable;

public class BalanceAPI extends BukkitRunnable {
    private final HamsterEcoHelper plugin;

    public BalanceAPI(HamsterEcoHelper pl) {
        plugin = pl;
        runTaskTimer(plugin, 20, plugin.config.balance_SaveIntervalTicks);
    }
  
    public boolean isEnabled() {
        return plugin.config.enable_balance;
    }

    public double getBalance() {
        return plugin.config.variablesConfig.balance;
    }

    public double setBalance(double money) {
        return plugin.config.variablesConfig.balance = money;
    }

    public void withdraw(double money) {
        plugin.config.variablesConfig.balance -= money;
    }

    public void deposit(double money) {
        plugin.config.variablesConfig.balance += money;
    }

    @Override
    public void run() {
        plugin.config.variablesConfig.save();
    }
}

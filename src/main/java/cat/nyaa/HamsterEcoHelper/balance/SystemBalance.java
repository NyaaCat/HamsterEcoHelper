package cat.nyaa.HamsterEcoHelper.balance;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.nyaacore.component.ISystemBalance;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SystemBalance extends BukkitRunnable implements ISystemBalance {

    private final HamsterEcoHelper plugin;

    public SystemBalance(HamsterEcoHelper pl) {
        plugin = pl;
        runTaskTimer(pl, 20, pl.config.balance_SaveIntervalTicks);
    }

    @Override
    public double getBalance() {
        return plugin.config.variablesConfig.balance;
    }

    @Override
    public void setBalance(double v, JavaPlugin javaPlugin) {
        plugin.config.variablesConfig.balance = v;
        log("set", v, javaPlugin);
    }

    @Override
    public double withdrawAllowDebt(double v, JavaPlugin javaPlugin) {
        if (isEnabled()) {
            plugin.config.variablesConfig.balance -= v;
            log("withdraw", v, javaPlugin);
        }
        return plugin.config.variablesConfig.balance;
    }

    @Override
    public double deposit(double v, JavaPlugin javaPlugin) {
        if (isEnabled()) {
            plugin.config.variablesConfig.balance += v;
            log("deposit", v, javaPlugin);
        }
        return plugin.config.variablesConfig.balance;
    }

    public boolean isEnabled() {
        return plugin.config.enable_balance;
    }

    @Override
    public void run() {
        plugin.config.variablesConfig.save();
    }

    private void log(String type, double v, JavaPlugin p) {
        if (plugin.config.system_balance_log) {
            plugin.logger.info(I18n.format("log.info.system_balance",
                    type, v, getBalance(), (p == null ? "unknown" : p.getName())));
        }
    }
}

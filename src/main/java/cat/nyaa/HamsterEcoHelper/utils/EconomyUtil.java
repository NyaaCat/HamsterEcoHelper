package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyUtil {
    private final HamsterEcoHelper plugin;
    public final Economy eco;

    public EconomyUtil(HamsterEcoHelper p) {
        plugin = p;
        RegisteredServiceProvider<Economy> provider = p.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            eco = provider.getProvider();
        } else {
            throw new RuntimeException("Vault Error: No EconomyProvider found");
        }
    }

    public double balance(OfflinePlayer p) {
        return eco.getBalance(p);
    }

    public boolean enoughMoney(OfflinePlayer p, long money) {
        return money < balance(p);
    }

    public boolean withdraw(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.depositPlayer(p, money);
        return rsp.transactionSuccess();
    }
}

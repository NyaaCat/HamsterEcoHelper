package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

public class SystemAccountUtils {
    private static SystemAccount account;

    public static void init(){
        account = new SystemAccount();
        account.load();
    }

    public static boolean isSystemAccount(UUID uuid){
        return account.getUUID().equals(uuid);
    }

    public static double getSystemBalance(){
        OfflinePlayer fakeAccount = getFakePlayer();
        return EcoUtils.getInstance().getEco().getBalance(fakeAccount);
    }

    private static OfflinePlayer getFakePlayer() {
        return Bukkit.getOfflinePlayer(account.getUUID());
    }

    public static String getSystemName(){
        if (account.isPlayer){
            return getFakePlayer().getName();
        }
        return I18n.format("system.name");
    }

    public static UUID getSystemUuid() {
        return account.uuid;
    }

    public static boolean deposit(double amount) {
        Economy eco = EcoUtils.getInstance().getEco();
        OfflinePlayer system = getFakePlayer();
        EconomyResponse economyResponse = eco.depositPlayer(system, amount);
        return economyResponse.transactionSuccess();
    }

    public static boolean withdraw(double amount) {
        Economy eco = EcoUtils.getInstance().getEco();
        OfflinePlayer system = getFakePlayer();
        EconomyResponse economyResponse = eco.withdrawPlayer(system, amount);
        return economyResponse.transactionSuccess();
    }

    public static boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        OfflinePlayer system = getFakePlayer();
        return makeTransaction(offlinePlayer, system, amount);
    }

    public static boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        OfflinePlayer system = getFakePlayer();
        return makeTransaction(system, offlinePlayer, amount);
    }

    private static boolean makeTransaction(OfflinePlayer toDeposit, OfflinePlayer toWithdraw, double amount) {
        Economy eco = EcoUtils.getInstance().getEco();
        double balanceToWithdraw = eco.getBalance(toWithdraw);
        double balanceToDeposit = eco.getBalance(toDeposit);
        try{
            eco.withdrawPlayer(toWithdraw, amount);
            eco.depositPlayer(toDeposit, amount);
        } catch (Exception e){
            double balanceToDepositAft = eco.getBalance(toWithdraw);
            double balanceToWithdrawAft = eco.getBalance(toDeposit);
            eco.depositPlayer(toWithdraw, balanceToWithdraw - balanceToWithdrawAft);
            eco.withdrawPlayer(toDeposit, balanceToDepositAft - balanceToDeposit);
            Bukkit.getLogger().log(Level.SEVERE, "error depositing player, rolling back: ", e);
            return false;
        }
        return true;
    }

    public static class SystemAccount extends FileConfigure{
        @Serializable
        UUID uuid = UUID.randomUUID();
        @Serializable
        boolean isPlayer = false;

        @Override
        protected String getFileName() {
            return "system_account.yml";
        }

        @Override
        protected JavaPlugin getPlugin() {
            return HamsterEcoHelper.plugin;
        }

        public UUID getUUID() {
            return uuid;
        }
    }
}

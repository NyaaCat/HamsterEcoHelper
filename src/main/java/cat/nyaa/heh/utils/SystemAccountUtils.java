package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.model.AccountDbModel;
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
    private static final String TABLE_NAME = "accounts";
    private static UidUtils uidManager = UidUtils.create(TABLE_NAME);

    public static void init(){
        uidManager.loadUid();
        account = new SystemAccount();
        account.load();
        if (account.uid == -1){
            long nextUid = uidManager.getNextUid();
            account.uid = nextUid;
            DatabaseManager.getInstance().addAccount(account.toDbModel());
        }
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

    public static boolean depositSystem(double amount) {
        if(account.isPlayer){
            Economy eco = EcoUtils.getInstance().getEco();
            EconomyResponse economyResponse = eco.depositPlayer(Bukkit.getOfflinePlayer(account.getUUID()), amount);
            return economyResponse.transactionSuccess();
        }
        try{
            AccountDbModel account = DatabaseManager.getInstance().getAccount(SystemAccountUtils.account.uid);
            double balance = account.getBalance();
            account.setBalance(balance + amount);
            DatabaseManager.getInstance().updateAccount(account);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean withdrawSystem(double amount) {
        if(account.isPlayer){
            Economy eco = EcoUtils.getInstance().getEco();
            EconomyResponse economyResponse = eco.withdrawPlayer(Bukkit.getOfflinePlayer(account.getUUID()), amount);
            return economyResponse.transactionSuccess();
        }
        try{
            AccountDbModel account = DatabaseManager.getInstance().getAccount(SystemAccountUtils.account.uid);
            double balance = account.getBalance();
            account.setBalance(balance - amount);
            DatabaseManager.getInstance().updateAccount(account);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        if (depositPlayer(offlinePlayer, amount)) {
            withdrawSystem(amount);
            return true;
        }
        return false;
    }

    public static boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        if (withDrawPlayer(offlinePlayer, amount)) {
            depositSystem(amount);
            return true;
        }
        return false;
    }

    private static boolean depositPlayer(OfflinePlayer toDeposit, double amount) {
        Economy eco = EcoUtils.getInstance().getEco();
        double balanceToDeposit = eco.getBalance(toDeposit);
        try{
            eco.depositPlayer(toDeposit, amount);
        } catch (Exception e){
            double balanceToDepositAft = eco.getBalance(toDeposit);
            eco.withdrawPlayer(toDeposit, balanceToDepositAft - balanceToDeposit);
            Bukkit.getLogger().log(Level.SEVERE, "error depositing player, rolling back: ", e);
            return false;
        }
        return true;
    }

    private static boolean withDrawPlayer(OfflinePlayer toWithdraw, double amount) {
        Economy eco = EcoUtils.getInstance().getEco();
        double balanceToWithdraw = eco.getBalance(toWithdraw);
        try{
            eco.withdrawPlayer(toWithdraw, amount);
        } catch (Exception e){
            double balanceToWithdrawAft = eco.getBalance(toWithdraw);
            eco.depositPlayer(toWithdraw, balanceToWithdraw - balanceToWithdrawAft);
            Bukkit.getLogger().log(Level.SEVERE, "error withdraw player, rolling back: ", e);
            return false;
        }
        return true;
    }

    public static class SystemAccount extends FileConfigure{
        @Serializable
        UUID uuid = UUID.randomUUID();
        @Serializable
        boolean isPlayer = false;
        @Serializable
        long uid = -1;

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

        public AccountDbModel toDbModel() {
            AccountDbModel dbModel = new AccountDbModel();
            dbModel.setBalance(0);
            dbModel.setUid(uid);
            dbModel.setUuid(uuid);
            return dbModel;
        }
    }
}

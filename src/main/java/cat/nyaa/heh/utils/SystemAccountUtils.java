package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

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

package cat.nyaa.hamsterecohelper.utils;

import cat.nyaa.hamsterecohelper.HamsterEcoHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EcoUtils {
    private static EcoUtils INSTANCE;
    private final Economy eco;

    public EcoUtils() {
        RegisteredServiceProvider<Economy> provider = HamsterEcoHelper.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            eco = provider.getProvider();
        } else {
            throw new RuntimeException("Vault Error: No EconomyProvider found");
        }
    }

    public static EcoUtils getInstance() {
        if (INSTANCE == null){
            synchronized (EcoUtils.class){
                if (INSTANCE == null) {
                    INSTANCE = new EcoUtils();
                }
            }
        }
        return INSTANCE;
    }

    public Economy getEco() {
        return eco;
    }
}

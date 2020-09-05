package cat.nyaa.heh.utils;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.business.transaction.TransactionController;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
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

    public void withdrawTax(OfflinePlayer payer, double tax, double fee, String reason){
        eco.withdrawPlayer(payer, tax);
        EconomyResponse economyResponse = eco.withdrawPlayer(payer, fee);
        if (economyResponse.type.equals(EconomyResponse.ResponseType.SUCCESS)){
            TransactionController instance = TransactionController.getInstance();
            Tax tax1 = instance.newTax(payer.getUniqueId(), tax, fee, System.currentTimeMillis(), reason);
            instance.retrieveTax(tax1);
        }else throw new RuntimeException("error withdrawing player: "+economyResponse.errorMessage);
    }
}

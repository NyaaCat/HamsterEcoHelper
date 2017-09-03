package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.balance.BalanceAPI;
import cat.nyaa.nyaacore.utils.ReflectionUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public class EconomyUtil {
    public final Economy eco;
    private final HamsterEcoHelper plugin;

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
        return money <= balance(p);
    }

    public boolean enoughMoney(OfflinePlayer p, double money) {
        return money <= balance(p);
    }

    public boolean withdraw(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean withdraw(OfflinePlayer p, double money) {
        EconomyResponse rsp = eco.withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer p, double money) {
        EconomyResponse rsp = eco.depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public synchronized Optional<Utils.GiveStat> transaction(OfflinePlayer buyer, OfflinePlayer seller, ItemStack item, double price, double tax) {
        int step = 0;
        try {
            if (buyer.equals(seller)) {
                Utils.GiveStat stat = Utils.giveItem(buyer, item);
                return Optional.of(stat);
            }
            if (!plugin.eco.withdraw(buyer, price + tax)) {
                return Optional.empty();
            }
            step = 1;
            if (!plugin.eco.deposit(seller, price)) {
                throw new RuntimeException("");
            }
            step = 2;
            if (tax > 0.0D) {
                BalanceAPI.deposit(tax);
            }
            step = 3;
            Utils.GiveStat stat = Utils.giveItem(buyer, item);
            return Optional.of(stat);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.format("log.error.transaction_fail", buyer.getName(), seller.getName(), Utils.getItemName(item), price, tax));
            try {
                plugin.getLogger().warning(I18n.format("log.error.transaction_fail_dump", ReflectionUtils.convertItemStackToJson(item)));
            } catch (Exception r) {
                plugin.getLogger().warning("failed to dump item json");
            }
            e.printStackTrace();
            switch (step) {
                case 3:
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_tax", tax));
                    BalanceAPI.withdraw(tax);
                    //fallthrough
                case 2:
                    boolean status2Seller = plugin.eco.withdraw(seller, price);
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_money",
                            seller.getName(), -price, status2Seller));
                    //fallthrough
                case 1:
                    boolean status1Buyer = plugin.eco.deposit(buyer, price + tax);
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_money",
                            buyer.getName(), price + tax, status1Buyer));
                case 0:
                    break;
                default:
                    throw new RuntimeException("invalid transaction status:" + step);
            }
            return Optional.empty();
        }
    }
}

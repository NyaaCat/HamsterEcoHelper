package cat.nyaa.HamsterEcoHelper.utils;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
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

    public synchronized Optional<MiscUtils.GiveStat> transaction(OfflinePlayer buyer, OfflinePlayer seller, ItemStack item, double price, double tax) {
        return transaction(buyer, seller, buyer, item, price, tax);
    }

    public synchronized Optional<MiscUtils.GiveStat> transaction(OfflinePlayer buyer, OfflinePlayer seller, OfflinePlayer drawee, ItemStack item, double price, double tax) {
        int step = 0;
        try {
            if (buyer.equals(seller) && drawee.equals(seller)) {
                MiscUtils.GiveStat stat = MiscUtils.giveItem(buyer, item);
                return Optional.of(stat);
            }
            EconomyResponse withdraw = eco.withdrawPlayer(drawee, price + tax);
            if (!withdraw.transactionSuccess()) {
                plugin.getLogger().info(I18n.format("log.info.withdraw_fail", buyer.getName(), seller.getName(), drawee.getName(), MiscUtils.getItemName(item), price, tax, withdraw.errorMessage));
                return Optional.empty();
            }
            step = 1;
            EconomyResponse deposit = eco.depositPlayer(seller, price);
            if (!deposit.transactionSuccess()) {
                throw new RuntimeException(deposit.errorMessage);
            }
            step = 2;
            if (tax > 0.0D) {
                plugin.systemBalance.deposit(tax, plugin);
            }
            step = 3;
            MiscUtils.GiveStat stat = MiscUtils.giveItem(buyer, item);
            return Optional.of(stat);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.format("log.error.transaction_fail", buyer.getName(), seller.getName(), drawee.getName(), MiscUtils.getItemName(item), price, tax));
            try {
                plugin.getLogger().warning(I18n.format("log.error.transaction_fail_dump", ItemStackUtils.itemToJson(item)));
            } catch (Exception r) {
                r.printStackTrace();
                plugin.getLogger().warning("failed to dump item json");
            }
            e.printStackTrace();
            switch (step) {
                case 3:
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_tax", tax));
                    plugin.systemBalance.withdraw(tax, plugin);
                    //fallthrough
                case 2:
                    EconomyResponse status2Seller = eco.withdrawPlayer(seller, price);
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_money",
                            seller.getName(), -price, status2Seller.transactionSuccess(), status2Seller.errorMessage));
                    //fallthrough
                case 1:
                    EconomyResponse status1Drawee = eco.depositPlayer(drawee, price + tax);
                    plugin.getLogger().warning(I18n.format("log.error.transaction_fail_rollback_money",
                            drawee.getName(), price + tax, status1Drawee.transactionSuccess(), status1Drawee.errorMessage));
                case 0:
                    break;
                default:
                    throw new RuntimeException("invalid transaction status:" + step);
            }
            return Optional.empty();
        }
    }
}

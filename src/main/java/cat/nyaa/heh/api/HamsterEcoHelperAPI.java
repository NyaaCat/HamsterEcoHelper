package cat.nyaa.heh.api;

import cat.nyaa.heh.business.item.ShopItemType;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public interface HamsterEcoHelperAPI {
    /**
     * withdraw player [amount] of money, including tax.
     * if withdraw failed, nothing will change.
     * @param player player to withdraw
     * @param amount amount of money
     * @param type item type, use in tax calculating
     * @return Response for this transaction
     */
    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount, ShopItemType type);

    /**
     * return the amount of balance in system account.
     * @see cat.nyaa.heh.utils.SystemAccountUtils
     * @return system balance.
     */
    BigDecimal getSystemBalance();

}

package cat.nyaa.heh.api;

import cat.nyaa.heh.business.item.ShopItemType;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface HamsterEcoHelperAPI {
    /**
     * withdraw player [amount] of money, including tax.
     * if withdraw failed, nothing will change.
     * @param player player to withdraw
     * @param amount amount of money
     * @param type item type, use in tax calculating
     * @param taxReason reason for taxing, see {@link cat.nyaa.heh.business.transaction.TaxReason}
     *
     * @return Response for this transaction
     */
    boolean withdrawPlayer(OfflinePlayer player, double amount, ShopItemType type, String taxReason);

    /**
     * return the amount of balance in system account.
     * @see cat.nyaa.heh.utils.SystemAccountUtils
     * @return system balance.
     */
    double getSystemBalance();


    /**
     * directly pay specific amount money to system account.
     * will generate a record from system itself.
     * @param reason {@link cat.nyaa.heh.business.transaction.TaxReason}
     * @param amount amout of money
     * @return success
     */
    boolean depositToSystem(String reason, double amount);

    /**
     * pay specific amount money to system account from a player.
     * will generate a tax record.
     * @param reason {@link cat.nyaa.heh.business.transaction.TaxReason}
     * @param amount amout of money
     * @return success
     */
    boolean depositToSystem(OfflinePlayer from, String reason, double amount);

    /**
     * same as {@link #depositToSystem(OfflinePlayer, String, double)}
     * but recorded as fee.
     * @param from
     * @param reason
     * @param amount
     * @return
     */
    boolean chargeFee(OfflinePlayer from, String reason, double amount);

    /**
     * directly take specific amount money to system account.
     * will generate a record from system itself.
     * @param reason {@link cat.nyaa.heh.business.transaction.TaxReason}
     * @param amount amout of money
     * @return success
     */
    boolean withdrawFromSystem(String reason, double amount);

    /**
     * take specific amount money from system account to a player.
     * will generate a tax record.
     * @param reason {@link cat.nyaa.heh.business.transaction.TaxReason}
     * @param amount amout of money
     * @return success
     */
    boolean withdrawFromSystem(OfflinePlayer from, String reason, double amount);


    /**
     * open a SignShopGUI Inventory (owned by shopOwner) for a player.
     * @param opener player to open the inventory for
     * @param shopOwner SignShop owner
     * @return opened Inventory.
     */
    Inventory openShopfor(Player opener, OfflinePlayer shopOwner);
}

package cat.nyaa.heh.transaction;

import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.utils.EcoUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class TransactionControler {
    private static final String TRANSACTION_TABLE_NAME = "transaction";
    private static final String TAX_TABLE_NAME = "tax";
    private long transactionUid = -1;
    private long taxUid = -1;
    private static TransactionControler INSTANCE;
    private TransactionControler(){}
    public static TransactionControler getInstance(){
        if (INSTANCE == null){
            synchronized (TransactionControler.class){
                if (INSTANCE == null) {
                    INSTANCE = new TransactionControler();
                }
            }
        }
        return INSTANCE;
    }

    public boolean makeTransaction(UUID buyer, UUID seller, ShopItem item, int amount){
        OfflinePlayer pBuyer = Bukkit.getOfflinePlayer(buyer);
        OfflinePlayer pSeller = Bukkit.getOfflinePlayer(seller);
        Economy eco = EcoUtils.getInstance().getEco();
        double balance = eco.getBalance(pBuyer);
        double itemPrice = item.getUnitPrice() * amount;
        double tax = Tax.calcTax(item, itemPrice);
        if (balance < itemPrice + tax){
            return false;
        }
        try{

        }catch (Exception e){

        }
        return false;
    }

    public void updateUid() {
        try {
            transactionUid = DatabaseManager.getInstance().getUidMax(TRANSACTION_TABLE_NAME);
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("failed to get max uid for table %s", TRANSACTION_TABLE_NAME));
            transactionUid = 0;
        }
        try {
            taxUid = DatabaseManager.getInstance().getUidMax(TAX_TABLE_NAME);
        } catch (SQLException throwables) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("failed to get max uid for table %s", TAX_TABLE_NAME));
            taxUid = 0;
        }
    }
}

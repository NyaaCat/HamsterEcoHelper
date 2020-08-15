package cat.nyaa.heh.transaction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.events.PreTransactionEvent;
import cat.nyaa.heh.events.TransactionEvent;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class TransactionController {
    private static final String TRANSACTION_TABLE_NAME = "transaction";
    private static final String TAX_TABLE_NAME = "tax";
    private long transactionUid = -1;
    private long taxUid = -1;

    private static TransactionController INSTANCE;
    private TransactionController(){}
    public static TransactionController getInstance(){
        if (INSTANCE == null){
            synchronized (TransactionController.class){
                if (INSTANCE == null) {
                    INSTANCE = new TransactionController();
                }
            }
        }
        return INSTANCE;
    }

    public boolean makeTransaction(UUID buyer, UUID seller, ShopItem item, int amount) {
        return makeTransaction(buyer, buyer, seller, item, amount);
    }

    public boolean makeTransaction(UUID buyer, UUID payer, UUID seller, ShopItem item, int amount){
        OfflinePlayer pBuyer = Bukkit.getOfflinePlayer(buyer);
        OfflinePlayer pPayer = Bukkit.getOfflinePlayer(payer);
        OfflinePlayer pSeller = Bukkit.getOfflinePlayer(seller);
        Economy eco = EcoUtils.getInstance().getEco();
        double balance = eco.getBalance(pBuyer);
        BigDecimal itemPrice = BigDecimal.valueOf(item.getUnitPrice())
                .multiply(BigDecimal.valueOf(amount));
        BigDecimal tax = Tax.calcTax(item, itemPrice);
        if (balance < itemPrice.add(tax).doubleValue()){
            return false;
        }
        BigDecimal toTake = itemPrice.add(tax);
        long taxUid = getNextTaxUid();
        long nextTransactionUid = getNextTransactionUid();
        double payerBalBefore = eco.getBalance(pPayer);
        double sellerBalBefore = eco.getBalance(pSeller);
        int soldAmountBefore = item.getSoldAmount();
        BukkitRunnable transactionRecorderTask = null;

        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(item, amount, toTake.doubleValue(), buyer, seller);
        Bukkit.getPluginManager().callEvent(preTransactionEvent);
        if (preTransactionEvent.isCanceled()){
            return false;
        }

        try{
            long time = System.currentTimeMillis();

            EconomyResponse rspBuyer = eco.withdrawPlayer(pPayer, toTake.doubleValue());
            EconomyResponse rspSeller = eco.depositPlayer(pSeller, itemPrice.doubleValue());
            if(!rspBuyer.type.equals(EconomyResponse.ResponseType.SUCCESS) || !rspSeller.type.equals(EconomyResponse.ResponseType.SUCCESS) ){
                throw new IllegalStateException("");
            }
            item.setSold(soldAmountBefore +amount);
            DatabaseManager.getInstance().updateShopItem(item);
            transactionRecorderTask = new BukkitRunnable() {
                @Override
                public void run() {
                    addTaxRecord(taxUid, pBuyer, tax.doubleValue(), 0, time);
                    addTransactionRecord(nextTransactionUid, item, amount, itemPrice, pBuyer, pSeller, taxUid, time);
                }
            };
            transactionRecorderTask.runTaskLaterAsynchronously(HamsterEcoHelper.plugin, 0);

            giveItemTo(pBuyer,pPayer, item, amount);
            TransactionEvent transactionEvent = new TransactionEvent(item, amount, toTake.doubleValue(), buyer, seller);
            Bukkit.getPluginManager().callEvent(transactionEvent);
            return true;
        }catch (Exception e){
            double payerBalAfter = eco.getBalance(pPayer);
            double sellerBalAfter = eco.getBalance(pSeller);
            double dBuyer = payerBalAfter - payerBalBefore;
            double dSeller = sellerBalAfter - sellerBalBefore;
            eco.depositPlayer(pPayer, dBuyer);
            eco.withdrawPlayer(pSeller, dSeller);
            Message message = new Message(I18n.format("transaction.error.exception", e.getMessage()));
            message.send(pBuyer);
            message.send(pSeller);
            int soldAmountAfter = item.getSoldAmount();
            if (sellerBalBefore != soldAmountAfter){
                item.setSold(soldAmountBefore);
                DatabaseManager.getInstance().updateShopItem(item);
            }
            if (transactionRecorderTask != null){
                transactionRecorderTask.cancel();
            }
            return false;
        }
    }

    private void addTransactionRecord(long nextTransactionUid, ShopItem item, int amount, BigDecimal itemPrice, OfflinePlayer pBuyer, OfflinePlayer pSeller, long taxUid, long time) {
        Transaction transaction = new Transaction(nextTransactionUid+1, item.getUid(), amount, itemPrice.doubleValue(), pBuyer.getUniqueId(), pSeller.getUniqueId(), taxUid, time);
        DatabaseManager.getInstance().insertTransaction(transaction);
        TransactionController.this.transactionUid++;
    }

    private void addTaxRecord(long taxUid, OfflinePlayer taxPayer, double tax, double fee, long time) {
        Tax taxRecord = new Tax(taxUid+1, taxPayer.getUniqueId().toString(), tax, time);
        DatabaseManager.getInstance().insertTax(taxRecord);
        TransactionController.this.taxUid++;
    }

    private void giveItemTo(OfflinePlayer pBuyer, OfflinePlayer pPayer, ShopItem item, int amount) {
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(amount);
        if (!pBuyer.isOnline()) {
            if (pPayer.getUniqueId().equals(pBuyer.getUniqueId())){
                throw new IllegalStateException("buyer is not online");
            }
            giveItemTo(pPayer, pPayer, item, amount);
            //todo put to temp storage
        }else {
            Player player = pBuyer.getPlayer();
            PlayerInventory inventory = player.getInventory();
            Inventory enderChest = player.getEnderChest();
            if (giveTo(inventory, itemStack)) {
                new Message(I18n.format("item.give.inventory")).send(pBuyer);
                return;
            }else if (giveTo(enderChest, itemStack)) {
               new Message(I18n.format("item.give.ender_chest")).send(pBuyer);
            }else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                new Message(I18n.format("item.give.temp_storage")).send(pBuyer);
                //todo put to temp storage
            }
        }
    }

    private boolean giveTo(Inventory inventory, ItemStack itemStack) {
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (InventoryUtils.addItem(inventory, itemStack)){
                return true;
            }
        }
        return false;
    }

    private long getNextTransactionUid() {
        return transactionUid+1;
    }

    private long getNextTaxUid() {
        return taxUid+1;
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

    public void retrieveTax(OfflinePlayer payer, double tax, double fee) {
        long taxUid = getNextTaxUid();
        addTaxRecord(taxUid, payer, tax, fee, System.currentTimeMillis());
    }
}

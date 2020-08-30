package cat.nyaa.heh.business.transaction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.DatabaseManager;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.events.PreTransactionEvent;
import cat.nyaa.heh.events.TransactionEvent;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.heh.utils.UidUtils;
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
import java.util.UUID;

public class TransactionController {
    private static final String TRANSACTION_TABLE_NAME = "table_transac";
    private static final String TAX_TABLE_NAME = "tax";
    private static UidUtils transactionUidManager = UidUtils.create(TRANSACTION_TABLE_NAME);
    private static UidUtils taxUidManager = UidUtils.create(TAX_TABLE_NAME);

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

    public boolean makeTransaction(UUID buyer, UUID seller, ShopItem item, int amount, double fee) {
        return makeTransaction(buyer, buyer, seller, item, amount, fee, null, null);
    }

    public boolean makeTransaction(UUID buyer, UUID seller, ShopItem item, int amount, double fee, Inventory receiveInv, Inventory returnInv) {
        return makeTransaction(buyer, buyer, seller, item, amount, fee, receiveInv, returnInv);
    }


    public boolean makeTransaction(UUID buyer, UUID payer, UUID seller, ShopItem item, int amount, double fee, Inventory receiveInv, Inventory returnInv){
        OfflinePlayer pBuyer = Bukkit.getOfflinePlayer(buyer);
        OfflinePlayer pPayer = Bukkit.getOfflinePlayer(payer);
        OfflinePlayer pSeller = Bukkit.getOfflinePlayer(seller);
        Economy eco = EcoUtils.getInstance().getEco();
        double balance = eco.getBalance(pBuyer);
        BigDecimal itemPrice = BigDecimal.valueOf(item.getUnitPrice())
                .multiply(BigDecimal.valueOf(amount));
        BigDecimal tax = Tax.calcTax(item.getShopItemType(), itemPrice);
        if (balance < itemPrice.add(tax).doubleValue()){
            return false;
        }
        BigDecimal toTake = itemPrice.add(tax);
        long nextTransactionUid = transactionUidManager.getCurrentUid();
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
            ItemStack itemStack = item.getItemStack();
            itemStack.setAmount(amount);
            new Message("").append(I18n.format("transaction.withdraw", pSeller.getName(), toTake.doubleValue()), itemStack).send(pPayer);
            new Message("").append(I18n.format("transaction.deposit", pPayer.getName(), itemPrice.doubleValue()), itemStack).send(pSeller);
            if(!rspBuyer.type.equals(EconomyResponse.ResponseType.SUCCESS) || !rspSeller.type.equals(EconomyResponse.ResponseType.SUCCESS) ){
                throw new IllegalStateException("");
            }
            item.setSold(soldAmountBefore +amount);
            DatabaseManager.getInstance().updateShopItem(item);
            transactionRecorderTask = new BukkitRunnable() {
                @Override
                public void run() {
                    long taxUid = taxUidManager.getNextUid();
                    addTaxRecord(taxUid, pBuyer, tax.doubleValue(), 0, time);
                    addTransactionRecord(nextTransactionUid, item, amount, itemPrice, pBuyer, pSeller, taxUid, time);
                }
            };
            transactionRecorderTask.runTaskLaterAsynchronously(HamsterEcoHelper.plugin, 0);

            if (receiveInv != null){
                if (!giveTo(receiveInv, itemStack)) {
                    double storageFeeUnit = HamsterEcoHelper.plugin.config.storageFeeUnit;
                    StorageItem storageItem = StorageConnection.getInstance().newStorageItem(buyer, itemStack, storageFeeUnit * itemStack.getAmount());
                    StorageConnection.getInstance().addStorageItem(storageItem);
                }
            }else {
                giveItemTo(pBuyer,pPayer, item, amount);
            }

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
        TransactionController.transactionUidManager.getNextUid();
    }

    private void addTaxRecord(long taxUid, OfflinePlayer taxPayer, double tax, double fee, long time) {
        Tax taxRecord = new Tax(taxUid, taxPayer.getUniqueId(), tax, fee, time);
        DatabaseManager.getInstance().insertTax(taxRecord);
        TransactionController.transactionUidManager.getNextUid();
    }

    private void giveItemTo(OfflinePlayer pBuyer, OfflinePlayer pPayer, ShopItem item, int amount) {
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(amount);
        if (!pBuyer.isOnline()) {
            if (pPayer.getUniqueId().equals(pBuyer.getUniqueId())){
                throw new IllegalStateException("buyer is not online");
            }
            giveItemTo(pPayer, pPayer, item, amount);
            double storageFeeUnit = HamsterEcoHelper.plugin.config.storageFeeUnit;
            StorageItem storageItem = StorageConnection.getInstance().newStorageItem(pBuyer.getUniqueId(), itemStack, storageFeeUnit * itemStack.getAmount());
            StorageConnection.getInstance().addStorageItem(storageItem);
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
                new Message(I18n.format("item.give.temp_storage")).send(pBuyer);
                double storageFeeUnit = HamsterEcoHelper.plugin.config.storageFeeUnit;
                StorageItem storageItem = StorageConnection.getInstance().newStorageItem(player.getUniqueId(), itemStack, storageFeeUnit * itemStack.getAmount());
                StorageConnection.getInstance().addStorageItem(storageItem);
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

    public void retrieveTax(OfflinePlayer payer, double tax, double fee) {
        long taxUid = taxUidManager.getNextUid();
        addTaxRecord(taxUid, payer, tax, fee, System.currentTimeMillis());
    }
}

package cat.nyaa.heh.business.auction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

public class Auction {
    private static Auction currentAuction;
    private static boolean hasAuction = false;

    private ShopItem item;
    private double basePrice;
    private double stepPrice;
    private double reservePrice;

    private double highestOffer = 0d;
    private UUID offerer = null;
    private boolean hasOffer = false;

    private int step = 0;
    private int auctionStepInterval;

    public Auction(ShopItem item, double basePrice, double stepPrice, double reservePrice) {
        this.item = item;
        this.basePrice = basePrice;
        this.stepPrice = stepPrice;
        this.reservePrice = reservePrice;
        this.auctionStepInterval = HamsterEcoHelper.plugin.config.auctionStepInterval;
    }

    public static Auction startAuction(ShopItem item, double basePrice, double stepPrice, double reservePrice) {
        if (currentAuction != null) {
            return null;
        }
        Auction auction = new Auction(item, basePrice, stepPrice, reservePrice);
        auction.start();
        return auction;
    }

    public static boolean hasAuction() {
        return hasAuction;
    }

    public static Auction currentAuction() {
        return currentAuction;
    }

    public void start() {
        if (hasAuction) {
            throw new IllegalStateException("an auction is running");
        }
        HamsterEcoHelper.plugin.setAuction(this);
        hasAuction = true;
        currentAuction = this;
        auctionTask = new AuctionTask(this);
        auctionTask.runTaskLater(HamsterEcoHelper.plugin, auctionStepInterval);
        String name = Bukkit.getOfflinePlayer(item.getOwner()).getName();
        if (item.isOwnedBySystem()) {
            name = SystemAccountUtils.getSystemName();
        }
        broadcast(new Message("").append(I18n.format("auction.start", name, basePrice, stepPrice), getItem()));
    }

    public void onBid(UUID offerer, double offer) {
        if (!isValidBid(offer)) {
            return;
        }
        this.highestOffer = offer;
        this.offerer = offerer;
        hasOffer = true;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        broadcast(new Message("").append(I18n.format("auction.bid.message", offlinePlayer.getName(), offer)));
        auctionTask.onBid();
    }

    public boolean isValidBid(double offer) {
        return this.highestOffer + stepPrice - offer <= 0.00001;
    }

    public void stop() {
        hasAuction = false;
        currentAuction = null;
    }

    public static void broadcast(Message message) {
        message.broadcast(new Permission("heh.bid"));
    }

    private void broadcastInfo(int step) {
        ItemStack itemStack = getItem();
        double current = basePrice;
        if (hasOffer) {
            current = highestOffer;
        }
        if (item.isOwnedBySystem()) {
            broadcast(new Message("").append(I18n.format("auction.info.system", current, stepPrice), itemStack));
        } else {
            broadcast(new Message("").append(I18n.format("auction.info.player", current, stepPrice), itemStack));
        }
        if (hasOffer) {
            String name = Bukkit.getOfflinePlayer(offerer).getName();
            broadcast(new Message("").append(I18n.format("auction.info.offer", name, highestOffer, step + 1)));
        } else {
            broadcast(new Message("").append(I18n.format("auction.info.no_offer")));
        }
    }

    /**
     * only use for server shutting down.
     * abort current auction and return item back.
     */
    public void abort() {
        if (currentAuction != null) {
            currentAuction.offerer = null;
            currentAuction.highestOffer = 0;
            currentAuction.hasOffer = false;
            currentAuction.onAucFail();
        }
    }

    private ItemStack getItem() {
        ItemStack itemStack = item.getItemStack();
        int amount = itemStack.getAmount();
        itemStack.setAmount(amount);
        return itemStack;
    }

    private static AuctionTask auctionTask;

    public boolean hasOffer() {
        return hasOffer;
    }

    public double getCurrentOffer() {
        return highestOffer;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public static class AuctionTask extends BukkitRunnable {
        private Auction auctionInstance;

        AuctionTask(Auction auctionInstance) {
            this.auctionInstance = auctionInstance;
        }

        @Override
        public void run() {
            switch (auctionInstance.step) {
                case 0:
                case 1:
                    auctionInstance.broadcastInfo(auctionInstance.step);
                    auctionInstance.step++;
                    break;
                case 2:
                    if (!auctionInstance.hasOffer || auctionInstance.highestOffer < auctionInstance.reservePrice) {
                        auctionInstance.onAucFail();
                    } else {
                        auctionInstance.onAucSuccess();
                    }
                    return;
                default:
                    Bukkit.getLogger().log(Level.WARNING, "auction ended unexpectedly");
                    auctionInstance.onAucFail();
                    auctionTask.cancel();
                    auctionTask = null;
                    return;
            }
            auctionTask = new AuctionTask(auctionInstance);
            auctionTask.runTaskLater(HamsterEcoHelper.plugin, auctionInstance.auctionStepInterval);
        }

        void onBid() {
            auctionInstance.step = 0;
            cancel();
            auctionTask = new AuctionTask(auctionInstance);
            auctionTask.runTaskLater(HamsterEcoHelper.plugin, auctionInstance.auctionStepInterval);
        }

    }

    private void onAucSuccess() {
        ItemStack itemStack = getItem();
        stop();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        double fee = HamsterEcoHelper.plugin.config.auctionFeeBase;
        TransactionController.getInstance().makeTransaction(offerer, this.item.getOwner(), this.item, this.item.getAmount(), fee);
        broadcast(new Message("").append(I18n.format("auction.success", offlinePlayer.getName(), highestOffer), itemStack));
    }

    private void onAucFail() {
        ItemStack itemStack = getItem();
        stop();
        broadcast(new Message("").append(I18n.format("auction.failed"), itemStack));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(item.getOwner());
        Inventory targetInventory = null;
        if (offlinePlayer.isOnline()) {
            targetInventory = offlinePlayer.getPlayer().getInventory();
        }else {
            //todo store item in temp inventory
            StorageConnection.getInstance().newStorageItem(offlinePlayer.getUniqueId(), itemStack, 0);
        }
        if(targetInventory != null){
            giveTo(targetInventory, itemStack);
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
}

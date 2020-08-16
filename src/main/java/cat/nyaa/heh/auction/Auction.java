package cat.nyaa.heh.auction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Auction {
    private static Auction currentAuction;
    private static boolean auctionInProgress = false;

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

    public static Auction startAuction(ShopItem item, double basePrice, double stepPrice, double reservePrice){
        if (currentAuction != null){
            return null;
        }
        Auction auction = new Auction(item, basePrice, stepPrice, reservePrice);
        auction.start();
        return auction;
    }

    public static boolean isInProgress(){
        return auctionInProgress;
    }

    public void start(){
        auctionInProgress = true;
        auctionTask = new AuctionTask();
        auctionTask.runTaskLater(HamsterEcoHelper.plugin, auctionStepInterval);
        if (item.isOwnedBySystem()){
            broadcast(new Message("").append(I18n.format("auction.start.system", basePrice, stepPrice), getItem()));
        }else {
            String name = Bukkit.getOfflinePlayer(item.getOwner()).getName();
            broadcast(new Message("").append(I18n.format("auction.start.player", name, basePrice, stepPrice), getItem()));
        }
    }

    public void onBid(UUID offerer, double offer){
        if (!isValidBid(offer)) {
            return;
        }
        this.highestOffer = offer;
        this.offerer = offerer;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        broadcast(new Message("").append(I18n.format("auction.bid.message", offlinePlayer.getName(), offer)));
        auctionTask.onBid();
    }

    public boolean isValidBid(double offer){
        return this.highestOffer + stepPrice - offer <= 0.00001;
    }

    public void stop(){
        auctionInProgress = false;
    }

    public static void broadcast(Message message){
        message.broadcast(new Permission("heh.bid"));
    }

    private void broadcastInfo(int step) {
        ItemStack itemStack = getItem();
        double current = basePrice;
        if (hasOffer) {
            current = highestOffer;
        }
        if (item.isOwnedBySystem()){
            broadcast(new Message("").append(I18n.format("auction.info.system", current, stepPrice), itemStack));
        }else {
            broadcast(new Message("").append(I18n.format("auction.info.player", current, stepPrice), itemStack));
        }
        if (hasOffer){
            String name = Bukkit.getOfflinePlayer(offerer).getName();
            broadcast(new Message("").append(I18n.format("auction.info.offer", name, highestOffer, step+1)));
        }else {
            broadcast(new Message("").append(I18n.format("auction.info.no_offer")));
        }
    }

    private ItemStack getItem() {
        ItemStack itemStack = item.getItemStack();
        int amount = itemStack.getAmount();
        itemStack.setAmount(amount);
        return itemStack;
    }

    private static AuctionTask auctionTask;
    public static class AuctionTask extends BukkitRunnable {
        private static final int INTERVAL = 10;

        private Auction auctionInstance;
        @Override
        public void run() {
            switch (auctionInstance.step){
                case 0:
                case 1:
                    auctionInstance.broadcastInfo(auctionInstance.step);
                    auctionInstance.step++;
                    break;
                case 2:
                    if (!auctionInstance.hasOffer || auctionInstance.highestOffer < auctionInstance.reservePrice){
                        auctionInstance.onAucFail();
                    }else {
                        auctionInstance.onAucSuccess();
                    }
                    break;
                default:
                    auctionTask.cancel();
                    auctionTask = null;
                    break;
            }
        }

        void onBid(){
            auctionInstance.step = 0;
            cancel();
            auctionTask = new AuctionTask();
            auctionTask.runTaskLater(HamsterEcoHelper.plugin, auctionInstance.auctionStepInterval);
        }

    }

    private void onAucSuccess() {
        ItemStack itemStack = getItem();
        stop();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        TransactionController.getInstance().makeTransaction(offerer, this.item.getOwner(), this.item, this.item.getAmount());
        broadcast(new Message("").append(I18n.format("auction.success", offlinePlayer.getName(), highestOffer), itemStack));
    }

    private void onAucFail() {
        ItemStack itemStack = getItem();
        stop();
        broadcast(new Message("").append(I18n.format("auction.failed"), itemStack));
    }
}

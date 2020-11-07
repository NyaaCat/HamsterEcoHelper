package cat.nyaa.heh.business.auction;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TaxMode;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.business.transaction.TransactionRequest;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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
        broadcast(new Message("").append(I18n.format("auction.start", name, basePrice, stepPrice), getItemStack()));
    }

    public void onBid(UUID offerer, double offer) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        double minOffer = this.getMinOffer();
        if (!isValidBid(offer)) {
            new Message(I18n.format("command.bid.invalid_price", minOffer)).send(offlinePlayer);
            return;
        }
        this.highestOffer = offer;
        this.offerer = offerer;
        hasOffer = true;
        broadcast(new Message("").append(I18n.format("auction.bid.message", offlinePlayer.getName(), offer)));
        auctionTask.onBid();
    }

    public boolean isValidBid(double offer) {
        return getMinOffer() - offer <= 0.00001;
    }

    public void stop() {
        hasAuction = false;
        currentAuction = null;
    }

    public static void broadcast(Message message) {
        message.broadcast(new Permission("heh.business.bid"));
    }

    private void broadcastInfo(int step) {
        ItemStack itemStack = getItemStack();
        double current = basePrice;
        if (hasOffer) {
            current = highestOffer;
        }
        String name = SystemAccountUtils.isSystemAccount(item.getOwner()) ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(item.getOwner()).getName();
        broadcast(new Message("").append(I18n.format("auction.info.player", name, stepPrice), itemStack));
        if (hasOffer) {
            String offerName = Bukkit.getOfflinePlayer(offerer).getName();
            broadcast(new Message("").append(I18n.format("auction.info.offer", offerName, highestOffer, step + 1)));
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

    public ShopItem getItem(){
        return item;
    }

    public ItemStack getItemStack() {
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

    public double getMinOffer() {
        double currentMinOffer = this.hasOffer() ? this.getCurrentOffer() : this.getBasePrice();
        double minOffer;
        if (this.hasOffer()){
            minOffer = currentMinOffer + Math.max(this.getStepPrice(), 1);
        }else {
            minOffer = this.getBasePrice();
        }
        return minOffer;
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
        ItemStack itemStack = getItemStack();
        this.stop();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offerer);
        double fee = HamsterEcoHelper.plugin.config.auctionFeeBase;
        TransactionRequest transactionRequest = buildTransactionReq();
        if (TransactionController.getInstance().makeTransaction(transactionRequest)) {
            broadcast(new Message("").append(I18n.format("auction.success", offlinePlayer.getName(), highestOffer), itemStack));
        }else {
            onAucFail();
        }
    }

    private TransactionRequest buildTransactionReq() {
        TransactionRequest.TransactionBuilder transactionBuilder = new TransactionRequest.TransactionBuilder();
        TransactionRequest req = transactionBuilder.item(item)
                .seller(item.getOwner())
                .buyer(offerer)
                .amount(item.getAmount())
                .reason(TaxReason.REASON_AUC)
                .taxMode(TaxMode.CHARGE)
                .priceOverride(highestOffer)
                .build();
        return req;
    }

    private void onAucFail() {
        ItemStack itemStack = getItemStack();
        stop();
        broadcast(new Message("").append(I18n.format("auction.failed"), itemStack));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(item.getOwner());
        Inventory targetInventory = null;
        if (offlinePlayer.isOnline()) {
            targetInventory = offlinePlayer.getPlayer().getInventory();
            if (giveTo(targetInventory, itemStack)) {
                new Message(I18n.format("item.give.inventory")).send(offlinePlayer);
            }else{
                giveToTempStorage(itemStack, offlinePlayer);
            }
        }else {
            giveToTempStorage(itemStack, offlinePlayer);
            return;
        }
    }

    private boolean giveToTempStorage(ItemStack itemStack, OfflinePlayer offlinePlayer) {
        try{
            StorageConnection.getInstance().getPlayerStorage(offlinePlayer.getUniqueId()).addItem(itemStack, 0);
            new Message(I18n.format("item.give.temp_storage")).send(offlinePlayer);
            return true;
        }catch (Exception e){
            HamsterEcoHelper.plugin.getLogger().log(Level.WARNING, "exception during giving item to temp storage", e);
            return false;
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

    private void broadcastButtons(){
        String format = I18n.format("ui.message.auc_bid");
        TextComponent button = Utils.newMessageButton(format, new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(format)), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/heh bid min"));
        new Message("").append(button).broadcast();
    }
}

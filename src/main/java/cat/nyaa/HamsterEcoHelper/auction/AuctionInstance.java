package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static cat.nyaa.HamsterEcoHelper.utils.Utils.uid;

public class AuctionInstance {
    private class CheckPointListener extends BukkitRunnable {
        @Override
        public void run() {
            switch (stage) {
                case 0:
                    if (currentHighPrice >= 0)
                        new Message(I18n.get("user.auc.first", currentHighPrice)).broadcast("heh.bid");
                    stage = 1;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 1:
                    if (currentHighPrice >= 0)
                        new Message(I18n.get("user.auc.second", currentHighPrice)).broadcast("heh.bid");
                    stage = 2;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 2:
                    if (currentHighPrice >= 0)
                        new Message(I18n.get("user.auc.third", currentHighPrice)).broadcast("heh.bid");
                    finish();
                    /*
                    stage = 3;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 3:
                    finish();*/
            }
        }

        public void resetTime() {
            this.cancel();
            checkPointListener = new CheckPointListener();
            checkPointListener.runTaskLater(plugin, timeout);
        }
    }

    private HamsterEcoHelper plugin;
    private Runnable finishCallback;
    private int stage = 0;
    public long currentHighPrice = -1;
    private OfflinePlayer currentPlayer = null;
    private CheckPointListener checkPointListener;

    private String itemName;
    public ItemStack itemStack;
    public int startPr;
    public int stepPr;
    public int timeout;
    public boolean hideName;

    public AuctionInstance(ItemStack itemToGive, int startPrice, int stepPrice, int timeout, boolean hideName, HamsterEcoHelper plugin, Runnable finishCallback) {
        itemStack = itemToGive;
        startPr = startPrice;
        stepPr = stepPrice;
        this.timeout = timeout;
        this.plugin = plugin;
        this.finishCallback = finishCallback;
        this.hideName = hideName;

        String realName;
        if (itemToGive.hasItemMeta() && itemToGive.getItemMeta().hasDisplayName()) {
            realName = itemToGive.getItemMeta().getDisplayName();
        } else {
            realName = itemToGive.getType().name() + ":" + itemToGive.getDurability();
        }

        if (hideName) {
            new Message(I18n.get("user.auc.new_auction_unknown", startPrice, stepPrice, (int) Math.floor(timeout / 20D))).broadcast("heh.bid");
            itemName = I18n.get("user.auc.mystery_item_placeholder");
        } else {
            new Message(I18n.get("user.auc.new_auction_0")).append(itemToGive)
                    .appendFormat("user.auc.new_auction_1", startPrice, stepPrice, (int) Math.floor(timeout / 20D))
                    .broadcast();
            itemName = realName;
        }
        plugin.logger.info(I18n.get("internal.info.auc_start", realName, itemToGive.getAmount(),
                Boolean.toString(hideName), startPrice, stepPrice, uid(this)));
        checkPointListener = new CheckPointListener();
        checkPointListener.runTaskLater(plugin, timeout);
    }

    public boolean onBid(Player p, int price) {
        currentHighPrice = price;
        currentPlayer = p;
        Message msg = new Message(I18n.get("user.auc.new_price_0", p.getName()));
        if (hideName) {
            msg.appendFormat("user.auc.mystery_item_placeholder");
        } else {
            msg.append(itemStack);
        }
        msg.appendFormat("user.auc.new_price_1", price).broadcast();
        p.sendMessage(I18n.get("user.auc.new_price_success"));
        plugin.logger.info(I18n.get("internal.info.auc_bid", uid(this), p.getName(), price));
        stage = 0;
        checkPointListener.resetTime();
        return true;
    }

    public void finish() {
        EconomyUtil e = plugin.eco;
        if (currentPlayer == null) {
            new Message(I18n.get("user.auc.fail")).append(itemStack).broadcast();
            plugin.logger.info(I18n.get("internal.info.auc_finish", uid(this), currentHighPrice, "", "NO_PLAYER_BID"));
        } else {
            boolean success = e.withdraw(currentPlayer, currentHighPrice);
            if (success) {
                int stat = Utils.giveItem(currentPlayer, itemStack);
                if (currentPlayer.isOnline() && currentPlayer instanceof Player) {
                    ((Player) currentPlayer).sendMessage(I18n.get("user.auc.item_given_" + Integer.toString(stat)));
                }
                new Message(I18n.get("user.auc.success_0")).append(itemStack)
                        .appendFormat("user.auc.success_1", currentPlayer.getName())
                        .broadcast();
                plugin.logger.info(I18n.get("internal.info.auc_finish", uid(this), currentHighPrice, currentPlayer.getName(), "SUCCESS"));
            } else {
                new Message(I18n.get("user.auc.fail")).append(itemStack).broadcast();
                plugin.logger.info(I18n.get("internal.info.auc_finish", uid(this), currentHighPrice, currentPlayer.getName(), "NOT_ENOUGH_MONEY"));
            }
        }
        finishCallback.run();
    }

    public void halt() {
        checkPointListener.cancel();
        plugin.logger.info(I18n.get("internal.info.auc_finish", uid(this), -1, "", "HALTED"));
    }

}

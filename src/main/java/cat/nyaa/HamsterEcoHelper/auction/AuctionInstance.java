package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.EconomyUtil;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

import static cat.nyaa.HamsterEcoHelper.utils.MiscUtils.uid;

public class AuctionInstance {
    public double currentHighPrice = -1;
    public ItemStack itemStack;
    public double startPr;
    public double stepPr;
    public int timeout;
    public boolean hideName;
    public OfflinePlayer owner = null;
    public double reservePrice = 0;
    private HamsterEcoHelper plugin;
    private Runnable finishCallback;
    private int stage = 0;
    private OfflinePlayer currentPlayer = null;
    private CheckPointListener checkPointListener;
    private String itemName;

    public AuctionInstance(OfflinePlayer player, ItemStack itemToGive, double startPrice, double stepPrice, double reservePrice, int timeout, boolean hideName, HamsterEcoHelper plugin, Runnable finishCallback) {
        itemStack = itemToGive;
        startPr = startPrice;
        stepPr = stepPrice;
        this.timeout = timeout;
        this.plugin = plugin;
        this.finishCallback = finishCallback;
        this.hideName = hideName;
        this.owner = player;
        this.reservePrice = reservePrice;

        String realName = MiscUtils.getItemName(itemToGive);

        if (hideName) {
            new Message(I18n.format("user.auc.new_auction_unknown", startPrice, stepPrice, (int) Math.floor(timeout / 20D))).broadcast(new Permission("heh.bid"));
            itemName = I18n.format("user.auc.mystery_item_placeholder");
        } else {
            if (owner == null) {
                new Message(I18n.format("user.auc.new_auction_0")).append(itemToGive)
                                                                  .appendFormat(plugin.i18n, "user.auc.new_auction_1", startPrice, stepPrice, (int) Math.floor(timeout / 20D))
                                                                  .broadcast();
            } else {
                new Message(I18n.format("user.auc.player_auction_0", owner.getName())).append(itemToGive)
                                                                                      .appendFormat(plugin.i18n, "user.auc.player_auction_1", startPrice, stepPrice, (int) Math.floor(timeout / 20D))
                                                                                      .broadcast();
            }
            itemName = realName;
        }
        if (owner == null) {
            plugin.logger.info(I18n.format("log.info.auc_start", realName, itemToGive.getAmount(),
                    Boolean.toString(hideName), startPrice, stepPrice, uid(this)));
        } else {
            realName = MiscUtils.getItemName(itemToGive);
            long id = plugin.database.addItemLog(player, itemStack, startPrice, itemStack.getAmount());
            plugin.logger.info(I18n.format("log.info.player_auc_start", id, player.getName(), realName, itemToGive.getAmount(),
                    Boolean.toString(hideName), startPrice, stepPrice, uid(this)));
        }
        checkPointListener = new CheckPointListener();
        checkPointListener.runTaskLater(plugin, timeout);
    }

    public boolean onBid(Player p, double price) {
        currentHighPrice = price;
        currentPlayer = p;
        Message msg = new Message(I18n.format("user.auc.new_price_0", p.getName()));
        if (hideName) {
            msg.appendFormat(plugin.i18n, "user.auc.mystery_item_placeholder");
        } else {
            msg.append(itemStack);
        }
        msg.appendFormat(plugin.i18n, "user.auc.new_price_1", price).broadcast();
        p.sendMessage(I18n.format("user.auc.new_price_success"));
        plugin.logger.info(I18n.format("log.info.auc_bid", uid(this), p.getName(), price));
        stage = 0;
        checkPointListener.resetTime();
        return true;
    }

    public void finish() {
        EconomyUtil e = plugin.eco;
        if (currentPlayer == null) {
            if (this.owner != null && this.itemStack != null) {
                MiscUtils.giveItem(this.owner, this.itemStack);
            }
            new Message(I18n.format("user.auc.fail")).append(itemStack).broadcast();
            plugin.logger.info(I18n.format("log.info.auc_finish", uid(this), currentHighPrice, "", "NO_PLAYER_BID"));
        } else {
            boolean success = false;
            if (owner != null) {
                plugin.auctionManager.cooldown.put(owner.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerAuctionCooldownTicks * 50));
            }
            if (currentHighPrice >= reservePrice) {
                success = e.enoughMoney(currentPlayer, currentHighPrice);
            }
            if (success) {
                if (this.owner != null) {
                    double commissionFee = 0.0D;
                    if (plugin.config.playerAuctionCommissionFee > 0) {
                        commissionFee = (currentHighPrice / 100) * plugin.config.playerAuctionCommissionFee;
                    }
                    Optional<MiscUtils.GiveStat> stat = e.transaction(currentPlayer, owner, itemStack, currentHighPrice, commissionFee);
                    if (stat.isPresent()) {
                        if (currentPlayer.isOnline() && currentPlayer instanceof Player) {
                            ((Player) currentPlayer).sendMessage(I18n.format("user.auc.item_given_" + stat.get().name()));
                        }
                    } else {
                        success = false;
                    }
                } else {
                    if (e.withdraw(currentPlayer, currentHighPrice)) {
                        MiscUtils.GiveStat stat = MiscUtils.giveItem(currentPlayer, itemStack);
                        if (currentPlayer.isOnline() && currentPlayer instanceof Player) {
                            ((Player) currentPlayer).sendMessage(I18n.format("user.auc.item_given_" + stat.name()));
                        }
                        if (this.owner == null && plugin.systemBalance.isEnabled()) {
                            plugin.systemBalance.deposit(currentHighPrice, plugin);
                            plugin.config.save();
                            plugin.logger.info(I18n.format("log.info.current_balance", plugin.systemBalance.getBalance()));
                        }
                    } else {
                        success = false;
                    }
                }
            }
            if (!success) {
                if (this.owner != null && this.itemStack != null) {
                    MiscUtils.giveItem(this.owner, this.itemStack);
                }
                new Message(I18n.format("user.auc.fail")).append(itemStack).broadcast();
                plugin.logger.info(I18n.format("log.info.auc_finish", uid(this), currentHighPrice, currentPlayer.getName(), "NOT_ENOUGH_MONEY"));
            } else {
                new Message(I18n.format("user.auc.success_0")).append(itemStack)
                                                              .appendFormat(plugin.i18n, "user.auc.success_1", currentPlayer.getName())
                                                              .broadcast();
                plugin.logger.info(I18n.format("log.info.auc_finish", uid(this), currentHighPrice, currentPlayer.getName(), "SUCCESS"));
            }
        }
        this.itemStack = null;
        this.owner = null;
        checkPointListener.cancel();
        finishCallback.run();
    }

    public void halt() {
        if (this.owner != null && this.itemStack != null) {
            MiscUtils.giveItem(this.owner, this.itemStack);
            this.itemStack = null;
            this.owner = null;
        }
        checkPointListener.cancel();
        plugin.logger.info(I18n.format("log.info.auc_finish", uid(this), -1.0d, "", "HALTED"));
    }

    private class CheckPointListener extends BukkitRunnable {
        @Override
        public void run() {
            switch (stage) {
                case 0:
                    if (currentHighPrice >= 0)
                        new Message(I18n.format("user.auc.first", currentHighPrice)).broadcast(new Permission("heh.bid"));
                    stage = 1;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 1:
                    if (currentHighPrice >= 0)
                        new Message(I18n.format("user.auc.second", currentHighPrice)).broadcast(new Permission("heh.bid"));
                    stage = 2;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 2:
                    if (currentHighPrice >= 0)
                        new Message(I18n.format("user.auc.third", currentHighPrice)).broadcast(new Permission("heh.bid"));
                    finish();

                    stage = 3;
                    break;
                case 3:
                    // BukkitRunnable sucks again
                    checkPointListener.cancel();
                    checkPointListener = null;
                    break;
            }
        }

        public void resetTime() {
            this.cancel();
            checkPointListener = new CheckPointListener();
            checkPointListener.runTaskLater(plugin, timeout);
        }
    }

}

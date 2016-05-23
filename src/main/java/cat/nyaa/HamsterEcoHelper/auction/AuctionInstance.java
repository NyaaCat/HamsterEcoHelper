package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.EconomyHelper;
import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AuctionInstance {
    private class CheckPointListener extends BukkitRunnable {
        @Override
        public void run() {
            switch (stage) {
                case 0:
                    Bukkit.broadcast(I18n.get("user.auc.first", currentHighPrice), "heh.bid");
                    stage = 1;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 1:
                    Bukkit.broadcast(I18n.get("user.auc.second", currentHighPrice), "heh.bid");
                    stage = 2;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 2:
                    Bukkit.broadcast(I18n.get("user.auc.third", currentHighPrice), "heh.bid");
                    stage = 3;
                    checkPointListener = new CheckPointListener();
                    checkPointListener.runTaskLater(plugin, timeout);
                    break;
                case 3:
                    finish();
            }
        }

        public void resetTime() {
            this.cancel();
            checkPointListener = new CheckPointListener();
            checkPointListener.runTaskLater(plugin, timeout);
        }
    }

    private Set<Player> involvedPlayers = new HashSet<>();

    private HamsterEcoHelper plugin;
    private Runnable finishCallback;
    private int stage = 0;
    public long currentHighPrice = -1;
    private OfflinePlayer currentPlayer = null;
    private Location dropLocation = null;
    private CheckPointListener checkPointListener;

    private String itemName;
    public ItemStack itemStack;
    public int startPr;
    public int stepPr;
    public int timeout;

    public AuctionInstance(ItemStack itemToGive, int startPrice, int stepPrice, int timeout, HamsterEcoHelper plugin, Runnable finishCallback) {
        itemStack = itemToGive;
        startPr = startPrice;
        stepPr = stepPrice;
        this.timeout = timeout;
        this.plugin = plugin;
        this.finishCallback = finishCallback;
        String name = itemToGive.hasItemMeta() ? itemToGive.getItemMeta().getDisplayName() : itemToGive.getType().name();
        itemName = name;
        Bukkit.broadcast(I18n.get("user.auc.new_auction", name, startPrice, stepPrice, (int) Math.floor(timeout / 20D)), "heh.bid");
        checkPointListener = new CheckPointListener();
        checkPointListener.runTaskLater(plugin, timeout);
    }

    public boolean onBid(Player p, int price) {
        currentHighPrice = price;
        currentPlayer = p;
        dropLocation = p.getEyeLocation();
        involvedPlayers.add(p);
        for (Player p2 : involvedPlayers) {
            if (p2.isOnline() && !p2.equals(p)) {
                p2.sendMessage(I18n.get("user.auc.new_price", p.getName(), itemName, price));
            }
        }
        p.sendRawMessage(I18n.get("user.auc.new_price_success"));
        checkPointListener.resetTime();
        return true;
    }

    public void finish() {
        EconomyHelper e = plugin.eco;
        if (currentPlayer == null) {
            Bukkit.broadcast(I18n.get("user.auc.fail", itemName), "heh.bid");
        } else {
            boolean success = e.withdraw(currentPlayer, currentHighPrice);
            if (success) {
                dropLocation.getWorld().dropItem(dropLocation, itemStack.clone());
            } else {
                Bukkit.broadcast(I18n.get("user.auc.fail", itemName), "heh.bid");
            }
        }
        finishCallback.run();
    }

    public void halt() {
        checkPointListener.cancel();
        Bukkit.broadcast(I18n.get("user.auc.halted"), "heh.bid");
    }

}

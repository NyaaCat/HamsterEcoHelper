package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.*;

public class AuctionCommands {

    @SubCommand(value = "addauc", permission = "heh.addauction")
    public static void addAuc(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        if (args.length() != 6) {
            msg(sender, "manual.command.addauc");
            return;
        }
        AuctionItemTemplate item = new AuctionItemTemplate();
        item.templateItemStack = getItemInHand(sender).clone();
        item.baseAuctionPrice = args.nextInt();
        item.bidStepPrice = args.nextInt();
        item.randomWeight = args.nextDouble();
        item.hideName = args.nextBoolean();
        item.waitTimeTicks = args.nextInt();
        plugin.config.itemsForAuction.add(item);
        plugin.config.saveToPlugin();
    }

    @SubCommand(value = "runauc", permission = "heh.runauc")
    public static void runAuction(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        boolean success;
        if (args.length() == 2) {
            int id = args.nextInt();
            if (id < 0 || id >= plugin.config.itemsForAuction.size()) {
                msg(sender, "admin.error.auc_id_oor", 0, plugin.config.itemsForAuction.size() - 1);
                return;
            } else {
                success = plugin.auctionManager.newAuction(plugin.config.itemsForAuction.get(id));
            }
        } else {
            success = plugin.auctionManager.newAuction();
        }
        if (!success) {
            msg(sender, "admin.error.run_auc_fail");
        }
    }

    @SubCommand(value = "haltauc", permission = "heh.runauc")
    public static void haltAuction(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        AuctionInstance auc = plugin.auctionManager.getCurrentAuction();
        if (auc == null) {
            msg(sender, "user.info.no_current_auction");
            return;
        }
        plugin.auctionManager.halt();
    }

    @SubCommand(value = "bid", permission = "heh.bid")
    public static void userBid(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player p = asPlayer(sender);
        AuctionInstance auc = plugin.auctionManager.getCurrentAuction();
        if (auc == null) {
            msg(p, "user.info.no_current_auc");
            return;
        }
        if (args.length() == 1) {
            msg(sender, "manual.command.bid");
            return;
        }

        long minPrice = auc.currentHighPrice == -1? auc.startPr: auc.currentHighPrice + auc.stepPr;
        String tmp = args.top();
        int bid;
        if ("min".equals(tmp)) {
            bid = (int)minPrice;
        } else {
            bid = args.nextInt();
        }

        if (!plugin.eco.enoughMoney(p, bid)) {
            msg(p, "user.warn.no_enough_money");
            return;
        }
        if (bid < minPrice) {
            msg(p, "user.warn.not_high_enough", minPrice);
            return;
        }
        auc.onBid(p, bid);
    }

    @SubCommand(value = "retrieve", permission = "heh.retrieve")
    public static void userRetrieve(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player p = asPlayer(sender);
        if (args.length() == 1) {
            msg(sender, "user.retrieve.need_confirm");
            return;
        }
        List<ItemStack> items = plugin.database.getTemporaryStorage(p);
        if (items.size() == 0) {
            msg(sender, "user.retrieve.no_item");
            return;
        }
        for(ItemStack s : items) {
            p.getWorld().dropItem(p.getEyeLocation(), s);
        }
        plugin.database.clearTemporaryStorage(p);
    }
}

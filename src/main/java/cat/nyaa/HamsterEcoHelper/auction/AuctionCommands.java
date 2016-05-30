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
        if (args.length() != 4) {
            msg(sender, "manual.command.addauc");
            return;
        }
        AuctionItemTemplate item = new AuctionItemTemplate();
        item.templateItemStack = getItemInHand(sender).clone();
        item.baseAuctionPrice = args.nextInt();
        item.bidStepPrice = args.nextInt();
        item.randomWeight = args.nextDouble();
        plugin.config.itemsForAuction.add(item);
        plugin.config.saveToPlugin();
    }

    @SubCommand(value = "runauc", permission = "heh.runauc")
    public static void runAuction(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        plugin.auctionManager.newAuction();
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
        int bid = args.nextInt();
        if (!plugin.eco.enoughMoney(p, bid)) {
            msg(p, "user.warn.no_enough_money");
            return;
        }
        if (bid < auc.currentHighPrice + auc.stepPr) {
            msg(p, "user.warn.not_high_enough", auc.currentHighPrice + auc.stepPr);
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

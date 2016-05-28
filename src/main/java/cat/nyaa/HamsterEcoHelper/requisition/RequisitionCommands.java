package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.*;
public class RequisitionCommands {
    @SubCommand(value = "addreq", permission = "heh.addreq")
    public static void addReq(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        if (args.length() <= 1) {
            msg(sender, "manual.command.addreq");
            return;
        }
        // TODO
        RequisitionSpecification req = new RequisitionSpecification();
        req.itemTemplate = getItemInHand(sender).clone();
        req.minPurchasePrice = args.nextInt();
        req.maxPurchasePrice = req.minPurchasePrice;
        req.randomWeight = args.nextDouble();
        plugin.config.itemsForReq.add(req);
        plugin.config.saveToPlugin();
    }

    @SubCommand(value = "runreq", permission = "heh.runreq")
    public static void runRequisition(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        plugin.reqManager.newRequisition();
    }

    @SubCommand(value = "sell", permission = "heh.sell")
    public static void userSell(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player p = asPlayer(sender);
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req == null) {
            msg(sender, "user.info.no_current_requisition");
            return;
        }
        if (args.length() != 2) {
            msg(p, "manual.command.sell");
            return;
        }
        int amount = args.nextInt();
        int price = req.purchase(p, amount);
        if (price <= 0) {
            msg(p, "user.req.fail");
        } else {
            msg(p, "user.req.success", price);
            plugin.eco.deposit(p, price);
        }
    }
}

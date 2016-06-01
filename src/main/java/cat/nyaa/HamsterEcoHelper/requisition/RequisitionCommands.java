package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.*;
public class RequisitionCommands {
    @SubCommand(value = "addreq", permission = "heh.addreq")
    public static void addReq(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        if (args.length() != 13) {
            msg(sender, "manual.command.addreq");
            return;
        }
        RequisitionSpecification req = new RequisitionSpecification();
        req.itemTemplate = getItemInHand(sender).clone();
        req.itemTemplate.setAmount(1);
        req.minPurchasePrice = args.nextInt();
        req.maxPurchasePrice = args.nextInt();
        req.minAmount = args.nextInt();
        req.maxAmount = args.nextInt();
        req.randomWeight = args.nextDouble();
        req.timeoutTicks = args.nextInt();
        req.matchRule.requireExact = args.nextBoolean();
        req.matchRule.minDamageValue = args.nextInt();
        req.matchRule.maxDamageValue = args.nextInt();
        req.matchRule.enchantMatch = args.nextEnum(RequisitionSpecification.MatchingMode.class);
        req.matchRule.loreMatch = args.nextEnum(RequisitionSpecification.MatchingMode.class);
        req.matchRule.nameMatch = args.nextEnum(RequisitionSpecification.MatchingMode.class);
        plugin.config.itemsForReq.add(req);
        plugin.config.saveToPlugin();
    }

    @SubCommand(value = "runreq", permission = "heh.runreq")
    public static void runRequisition(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        boolean success = false;
        if (args.length() == 2) {
            int id = args.nextInt();
            if (id < 0 || id >= plugin.config.itemsForReq.size()) {
                msg(sender, "admin.error.req_id_oor", 0, plugin.config.itemsForReq.size()-1);
                return;
            } else {
                success = plugin.reqManager.newRequisition(plugin.config.itemsForReq.get(id));
            }
        } else {
            success = plugin.reqManager.newRequisition();
        }
        if (!success) {
            msg(sender, "admin.error.run_req_fail");
        }
    }

    @SubCommand(value = "haltreq", permission = "heh.runreq")
    public static void haltRequisition(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req == null) {
            msg(sender, "user.info.no_current_requisition");
            return;
        }
        plugin.reqManager.halt();
    }

    @SubCommand(value = "sell", permission = "heh.sell")
    public static void userSell(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player p = asPlayer(sender);
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req == null) {
            msg(sender, "user.info.no_current_requisition");
            return;
        }

        int amount;
        if (args.length() == 1) {
            amount = Math.min(getItemInHand(sender).getAmount(), req.getAmountRemains());
        } else if (args.length() == 2){
            amount = args.nextInt();
        } else {
            msg(p, "manual.command.sell");
            return;
        }

        if (!req.canSellAmount(amount)) {
            msg(p, "user.req.sell_amount_limit", req.getAmountRemains());
            return;
        }
        int price = req.purchase(p, amount);
        if (price < 0) {
            switch(price) {
                case -1:
                    msg(p, "user.req.not_enough");
                    break;
                case -2:
                    msg(p, "user.req.not_match");
                    break;
                default:
                    msg(p, "user.req.fail");
            }
        } else {
            msg(p, "user.req.success", price);
            plugin.eco.deposit(p, price);
        }
    }
}

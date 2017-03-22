package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.ItemLog;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RequisitionCommands extends CommandReceiver<HamsterEcoHelper> {
    private HamsterEcoHelper plugin;

    public RequisitionCommands(Object plugin, Internationalization i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "requisition";
    }

    @SubCommand(value = "addreq", permission = "heh.addreq")
    public void addReq(CommandSender sender, Arguments args) {
        if (args.length() != 14) {
            msg(sender, "manual.requisition.addreq.usage");
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
        plugin.config.requisitionConfig.itemsForReq.add(req);
        plugin.config.saveToPlugin();
    }

    @SubCommand(value = "runreq", permission = "heh.runreq")
    public void runRequisition(CommandSender sender, Arguments args) {
        boolean success = false;
        if (args.length() == 3) {
            int id = args.nextInt();
            if (id < 0 || id >= plugin.config.requisitionConfig.itemsForReq.size()) {
                msg(sender, "admin.error.req_id_oor", 0, plugin.config.requisitionConfig.itemsForReq.size() - 1);
                return;
            } else {
                success = plugin.reqManager.newRequisition(plugin.config.requisitionConfig.itemsForReq.get(id));
            }
        } else {
            success = plugin.reqManager.newRequisition();
        }
        if (!success) {
            msg(sender, "admin.error.run_req_fail");
        }
    }

    @SubCommand(value = "haltreq", permission = "heh.runreq")
    public void haltRequisition(CommandSender sender, Arguments args) {
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req == null) {
            msg(sender, "user.info.no_current_requisition");
            return;
        }
        plugin.reqManager.halt();
    }

    @SubCommand(value = "sell", permission = "heh.sell")
    public void userSell(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req == null) {
            msg(sender, "user.info.no_current_requisition");
            return;
        }

        if (req.owner != null && req.owner.getUniqueId().equals(p.getUniqueId())) {
            msg(sender, "user.req.sell_to_self");
            return;
        }

        int amount;
        if (args.length() == 2) {
            amount = Math.min(getItemInHand(sender).getAmount(), req.getAmountRemains());
        } else if (args.length() == 3) {
            amount = args.nextInt();
        } else {
            msg(p, "manual.requisition.sell.usage");
            return;
        }

        if (!req.canSellAmount(amount)) {
            msg(p, "user.req.sell_amount_limit", req.getAmountRemains());
            return;
        }
        double price = req.purchase(p, amount);
        if (price < 0) {
            if(price == -1) {
                msg(p, "user.req.not_enough");
            } else if(price == -2) {
                msg(p, "user.req.not_match");
            }else{
                msg(p, "user.req.fail");
            }
        } else {
            msg(p, "user.req.success", price);
            plugin.eco.deposit(p, price);
            if (req.owner == null && plugin.balanceAPI.isEnabled()) {
                plugin.balanceAPI.withdraw(price);
            }
        }
    }

    @SubCommand(value = "req", permission = "heh.userreq")
    public void Requisition(CommandSender sender, Arguments args) {
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req != null) {
            return;
        }
        if (args.length() != 5) {
            msg(sender, "manual.requisition.req.usage");
            return;
        }

        Player player = asPlayer(sender);
        String itemName = args.next().toUpperCase();
        ItemStack item = null;
        double unitPrice = args.nextDouble("#.##");
        int amount = args.nextInt();
        if (plugin.reqManager.cooldown.containsKey(player.getUniqueId())
                && plugin.reqManager.cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            msg(sender, "user.info.cooldown", (plugin.reqManager.cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000);
            return;
        }

        if (!(unitPrice > 0 && amount > 0)) {
            msg(sender, "user.error.not_double");
            return;
        }

        if (itemName.equals("HAND")) {
            item = getItemInHand(sender).clone();
        } else {
            try {
                item = new ItemStack(Material.valueOf(itemName));
            } catch (IllegalArgumentException e) {
                msg(sender, "user.error.unknown_item", itemName);
                return;
            }
            if (!ReflectionUtil.isValidItem(item)) {
                msg(sender, "user.error.unknown_item", itemName);
                return;
            }
        }
        if (!plugin.eco.enoughMoney(player, unitPrice * amount)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        boolean success = plugin.reqManager.newPlayerRequisition(player, item, unitPrice, amount);
        if (success) {
            plugin.eco.withdraw(player, amount * unitPrice);
            plugin.reqManager.cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerRequisitionCooldownTicks * 50));
        }
    }

    @SubCommand(value = "giveitem", permission = "heh.giveitem")
    public void GiveItem(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemLog item = plugin.database.getItemLog(args.nextInt());
        if (item != null) {
            Utils.giveItem(p, item.getItemStack());
            p.sendMessage("player: " + Bukkit.getPlayer(item.getOwner()).getName());
            p.sendMessage("price: " + item.getPrice());
        }

    }
}

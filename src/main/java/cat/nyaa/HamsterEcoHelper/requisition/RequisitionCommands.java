package cat.nyaa.HamsterEcoHelper.requisition;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.MiscUtils;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RequisitionCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public RequisitionCommands(Object plugin, LanguageRepository i18n) {
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
        plugin.config.save();
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
        if (args.remains() == 0) {
            amount = Math.min(getItemInHand(sender).getAmount(), req.getAmountRemains());
        } else if (args.remains() == 1) {
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
            if (price == -1) {
                msg(p, "user.req.not_enough");
            } else if (price == -2) {
                msg(p, "user.req.not_match");
            } else {
                msg(p, "user.req.fail");
            }
        } else {
            msg(p, "user.req.success", price);
            plugin.eco.deposit(p, price);
            if (req.owner == null && plugin.systemBalance.isEnabled()) {
                plugin.systemBalance.withdraw(price, plugin);
            }
        }
    }

    @SubCommand(value = "req", permission = "heh.userreq", tabCompleter = "reqTabComplete")
    public void Requisition(CommandSender sender, Arguments args) {
        RequisitionInstance req = plugin.reqManager.getCurrentRequisition();
        if (req != null) {
            return;
        }
        if (args.remains() != 3) {
            if ((args.remains() != 4 || !args.getRawArgs()[3].equalsIgnoreCase("true") && !args.getRawArgs()[3].equalsIgnoreCase("false"))) {
                msg(sender, "manual.requisition.req.usage");
                return;
            }
        }

        Player player = asPlayer(sender);
        String itemName = args.nextString().toUpperCase();
        ItemStack item = null;
        double unitPrice = args.nextDouble("#.##");
        int amount = args.nextInt();
        boolean hasStrictArg = args.top() != null;
        boolean argStrict = hasStrictArg && args.nextBoolean();

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
            Material material = MiscUtils.getMaterial(itemName, sender);
            if (material != null) {
                item = new ItemStack(material);
            }
            if (item == null || !material.isItem() || material.isAir()) {
                msg(sender, "user.error.unknown_item", itemName);
                return;
            }
        }
        boolean isStrict = (!hasStrictArg && isShulkerBox(item)) || argStrict;

        if (!plugin.eco.enoughMoney(player, unitPrice * amount)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        boolean success = plugin.reqManager.newPlayerRequisition(player, item, unitPrice, amount, isStrict);
        if (success) {
            plugin.eco.withdraw(player, amount * unitPrice);
            plugin.reqManager.cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.config.playerRequisitionCooldownTicks * 50));
        }
    }


    private static final Set<Material> SHULKER_BOXES = new HashSet<>();
    static {
        Collections.addAll(SHULKER_BOXES,
            Material.SHULKER_BOX,
                    Material.WHITE_SHULKER_BOX,
                    Material.ORANGE_SHULKER_BOX,
                    Material.MAGENTA_SHULKER_BOX,
                    Material.YELLOW_SHULKER_BOX,
                    Material.LIME_SHULKER_BOX,
                    Material.PINK_SHULKER_BOX,
                    Material.GRAY_SHULKER_BOX,
                    Material.LIGHT_BLUE_SHULKER_BOX,
                    Material.LIGHT_GRAY_SHULKER_BOX,
                    Material.CYAN_SHULKER_BOX,
                    Material.PURPLE_SHULKER_BOX,
                    Material.BLUE_SHULKER_BOX,
                    Material.BROWN_SHULKER_BOX,
                    Material.GREEN_SHULKER_BOX,
                    Material.RED_SHULKER_BOX,
                    Material.BLACK_SHULKER_BOX
        );
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item == null)return false;
        return SHULKER_BOXES.contains(item.getType());
    }

    public List<String> reqTabComplete(CommandSender sender, Arguments args) {
        List<String> list = new ArrayList<>();
        if (args.remains() == 1) {
            list.addAll(tabCompleteItemName(sender, args));
        }
        return list;
    }

    public List<String> tabCompleteItemName(CommandSender sender, Arguments args) {
        List<String> list = new ArrayList<>();
        if (args.remains() >= 1) {
            String name = args.nextString().toUpperCase(Locale.ENGLISH);
            if ("HAND".contains(name)) {
                list.add("HAND");
            }
            if (name.length() > 0) {
                    for (Material material : Material.values()) {
                    if (!material.name().startsWith("LEGACY_") && !material.isAir() && material.isItem() && material.name().contains(name)) {
                        list.add(material.name());
                    }
                }
            }
        }
        return list;
    }
}

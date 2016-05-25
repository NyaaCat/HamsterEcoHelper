package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionInstance;
import cat.nyaa.HamsterEcoHelper.data.AuctionItemTemplate;
import cat.nyaa.HamsterEcoHelper.data.RequisitionSpecification;
import cat.nyaa.HamsterEcoHelper.market.Market;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionInstance;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor {
    private static class NotPlayerException extends RuntimeException {
    }

    private static class NoItemInHandException extends RuntimeException {
    }

    private static class BadCommandException extends RuntimeException {
        public BadCommandException(String msg) {
            super(msg);
        }

        public BadCommandException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private final HamsterEcoHelper plugin;
    private Map<String, Method> subCommands = new HashMap<>();
    private Map<String, String> subCommandPermission = new HashMap<>();

    public CommandHandler(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        for (Method m : getClass().getDeclaredMethods()) {
            SubCommand anno = m.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (!(params.length == 2 && params[0] == CommandSender.class && params[1] == Arguments.class)) {
                plugin.getLogger().warning(I18n.get("internal.warn.bad_subcommand", m.toString()));
            } else {
                m.setAccessible(true);
                subCommands.put(anno.value().toLowerCase(), m);
                if (!anno.permission().equals(""))
                    subCommandPermission.put(anno.value(), anno.permission());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Arguments cmd = Arguments.parse(args);
            if (cmd == null) return false;
            String subCommand = cmd.next();
            if (subCommand == null || cmd.length() == 0 || !subCommands.containsKey(subCommand.toLowerCase())) {
                subCommand = "help";
            }

            if (subCommandPermission.containsKey(subCommand)) {
                if (!sender.hasPermission(subCommandPermission.get(subCommand))) {
                    sender.sendMessage("No Permission");
                    return true;
                }
            }

            try {
                subCommands.get(subCommand.toLowerCase()).invoke(this, sender, cmd);
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }
        } catch (NotPlayerException ex) {
            msg(sender, "user.info.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, "user.info.no_item_hand");
        } catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage("Internal Server Error");
        }
        return true;
    }

    @SubCommand("help")
    public void printHelp(CommandSender sender, Arguments arg) {
        sender.sendMessage("Under construction...");
    }

    @SubCommand(value = "debug", permission = "heh.debug")
    public void debug(CommandSender sender, Arguments arg) {
        String sub = arg.next();
        if ("showitem".equals(sub) || sender instanceof Player) {
            Player player = (Player) sender;
            new Message("Player has item: ").append(player.getInventory().getItemInMainHand()).send(player);
        }
    }

    @SubCommand(value = "addauc", permission = "heh.addauction")
    public void addAuc(CommandSender sender, Arguments args) {
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

    @SubCommand(value = "addreq", permission = "heh.addreq")
    public void addReq(CommandSender sender, Arguments args) {
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

    @SubCommand(value = "runauc", permission = "heh.runauc")
    public void runAuction(CommandSender sender, Arguments args) {
        plugin.auctionManager.newAuction();
    }

    @SubCommand(value = "runreq", permission = "heh.runreq")
    public void runRequisition(CommandSender sender, Arguments args) {
        plugin.reqManager.newRequisition();
    }

    @SubCommand(value = "save", permission = "heh.admin")
    public void forceSave(CommandSender sender, Arguments arg) {
        plugin.config.saveToPlugin();
        msg(sender, "admin.info.save_done");
    }

    @SubCommand(value = "force-load", permission = "heh.admin")
    public void forceLoad(CommandSender sender, Arguments args) {
        plugin.config.loadFromPlugin();
        msg(sender, "admin.info.load_done");
    }

    @SubCommand(value = "bid", permission = "heh.bid")
    public void userBid(CommandSender sender, Arguments args) {
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

    @SubCommand(value = "sell", permission = "heh.sell")
    public void userSell(CommandSender sender, Arguments args) {
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

    @SubCommand(value = "mailbox", permission = "heh.user")
    public void openMailbox(CommandSender sender, Arguments args) {
        Player player = (Player) sender;
        Market.openMailbox(player);
    }

    @SubCommand(value = "offer", permission = "heh.offer")
    public void offer(CommandSender sender, Arguments args) {
        if (args.length() == 2) {
            Player player = (Player) sender;
            double price=0.0;
            try{
                price = Double.parseDouble(new DecimalFormat("#.##").format(Double.parseDouble(args.next())));
            } catch (IllegalArgumentException ex){
                //return;
            }
            if(!(price>=0.01)){
                msg(sender,"user.error.not_double");
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item!=null && item.getType()!=Material.AIR && item.getAmount()>0 ){
                if(Market.offer(player, item, price)){
                    player.getInventory().setItemInMainHand(null);
                }
                return;
            }else {
                msg(sender,"user.info.not_item_hand");
                return;
            }
        }
    }

    @SubCommand(value = "view", permission = "heh.view")
    public void view(CommandSender sender, Arguments args) {
        Player player = (Player) sender;
        if(args.length()==2){
            OfflinePlayer seller = Bukkit.getOfflinePlayer(args.next());
            if (seller!=null){
                Market.view(player, 1,seller.getUniqueId().toString());
            }
        }else {
            Market.view(player, 1,"");
        }
    }

    private Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
    }

    private void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(I18n.get(template, args));
    }

    private ItemStack getItemInHand(CommandSender se) {
        if (se instanceof Player) {
            Player p = (Player) se;
            if (p.getInventory() != null) {
                ItemStack i = p.getInventory().getItemInMainHand();
                if (i != null && i.getType() != Material.AIR) {
                    return i;
                }
            }
            throw new NoItemInHandException();
        } else {
            throw new NotPlayerException();
        }
    }

    private static class Arguments {

        private List<String> parsedArguments = new ArrayList<>();
        private int index = 0;

        private Arguments() {
        }

        public static Arguments parse(String[] rawArg) {
            if (rawArg.length == 0) return new Arguments();
            String cmd = rawArg[0];
            for (int i = 1; i < rawArg.length; i++)
                cmd += " " + rawArg[i];

            List<String> cmdList = new ArrayList<>();
            boolean escape = false, quote = false;
            String tmp = "";
            for (int i = 0; i < cmd.length(); i++) {
                char chr = cmd.charAt(i);
                if (escape) {
                    if (chr == '\\' || chr == '`') tmp += chr;
                    else return null; // bad escape char
                    escape = false;
                } else if (chr == '\\') {
                    escape = true;
                } else if (chr == '`') {
                    if (quote) {
                        if (i + 1 == cmd.length() || cmd.charAt(i + 1) == ' ') {
                            cmdList.add(tmp);
                            tmp = "";
                            i++;
                            quote = false;
                        } else {
                            return null; //bad quote end
                        }
                    } else {
                        if (tmp.length() > 0)
                            return null; // bad quote start
                        quote = true;
                    }
                } else if (chr == ' ') {
                    if (quote) {
                        tmp += ' ';
                    } else if (tmp.length() > 0) {
                        cmdList.add(tmp);
                        tmp = "";
                    }
                } else {
                    tmp += chr;
                }
            }
            if (tmp.length() > 0) cmdList.add(tmp);
            if (escape || quote) return null;

            Arguments ret = new Arguments();
            ret.parsedArguments = cmdList;
            return ret;
        }

        public String at(int index) {
            return parsedArguments.get(index);
        }

        public String next() {
            if (index < parsedArguments.size())
                return parsedArguments.get(index++);
            else
                return null;
        }

        public int nextInt() {
            String str = next();
            if (str == null) throw new BadCommandException("No more integers in argument");
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                throw new BadCommandException(I18n.get("user.error.not_int", str), ex);
            }
        }

        public double nextDouble() {
            String str = next();
            if (str == null) throw new BadCommandException("No more numbers in argument");
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                throw new BadCommandException(I18n.get("user.error.not_double", str), ex);
            }
        }


        public int length() {
            return parsedArguments.size();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface SubCommand {
        String value();

        String permission() default "";
    }
}

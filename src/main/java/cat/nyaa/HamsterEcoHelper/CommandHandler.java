package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionCommands;
import cat.nyaa.HamsterEcoHelper.market.Market;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionCommands;
import cat.nyaa.HamsterEcoHelper.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        registerSubcommandHandler(CommandHandler.class);
        registerSubcommandHandler(RequisitionCommands.class);
        registerSubcommandHandler(AuctionCommands.class);
    }

    public List<String> getSubcommands() {
        ArrayList<String> ret = new ArrayList<>();
        ret.addAll(subCommands.keySet());
        ret.sort(String::compareTo);
        return ret;
    }

    private void registerSubcommandHandler(Class<?> handlerClass) {
        for (Method m : handlerClass.getDeclaredMethods()) {
            SubCommand anno = m.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            if (!Modifier.isStatic(m.getModifiers())) {
                plugin.getLogger().warning(I18n.get("internal.warn.bad_subcommand", m.toString()));
                continue;
            }
            Class<?>[] params = m.getParameterTypes();
            if (!(params.length == 3 &&
                    params[0] == CommandSender.class &&
                    params[1] == Arguments.class &&
                    params[2] == HamsterEcoHelper.class)) {
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
                subCommands.get(subCommand.toLowerCase()).invoke(null, sender, cmd, plugin);
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }
            msg(sender, "user.info.command_complete");
        } catch (NotPlayerException ex) {
            msg(sender, "user.info.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, "user.info.no_item_hand");
        } catch (BadCommandException ex) {
            sender.sendMessage(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            msg(sender, "user.error.command_exception");
        }
        return true;
    }

    @SubCommand("help")
    public static void printHelp(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        List<String> cmds = plugin.commandHandler.getSubcommands();
        if (args.length() <= 1) {
            String tmp = "";
            for (String cmd : cmds) {
                tmp += "\n    " + cmd + ":\t" + (I18n.hasKey("manual.description." + cmd) ? I18n.get("manual.description." + cmd) : I18n.get("manual.no_desc"));
            }
            msg(sender, "manual.general", tmp);
        } else {
            String sub = args.next();
            if (!cmds.contains(sub)) {
                msg(sender, "manual.no_such_cmd", sub);
                return;
            }
            msg(sender, "manual.general_title", sub);
            if (I18n.hasKey("manual.description." + sub)) {
                msg(sender, "manual.description." + sub);
            } else {
                msg(sender, "manual.no_desc");
            }
            if (I18n.hasKey("manual.command." + sub)) {
                msg(sender, "manual.command." + sub);
            } else {
                msg(sender, "manual.no_usage");
            }
        }
    }

    @SubCommand(value = "debug", permission = "heh.debug")
    public static void debug(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        String sub = args.next();
        if ("showitem".equals(sub) && sender instanceof Player) {
            Player player = (Player) sender;
            new Message("Player has item: ").append(player.getInventory().getItemInMainHand()).send(player);
        } else if ("dbi".equals(sub) && sender instanceof Player) {
            plugin.database.addTemporaryStorage((Player)sender, new ItemStack(Material.DIAMOND, 64));
        } else if ("ymllist".equals(sub)) {
            List<ItemStack> t = new ArrayList<ItemStack>(){{add(new ItemStack(Material.DIAMOND));
            add(new ItemStack(Material.ACACIA_DOOR));}};
            YamlConfiguration yml = new YamlConfiguration();
            yml.addDefault("abc", t);
            yml.set("abc", t);
            sender.sendMessage("\n"+yml.saveToString());
        }
    }

    @SubCommand(value = "save", permission = "heh.admin")
    public static void forceSave(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        plugin.config.saveToPlugin();
        msg(sender, "admin.info.save_done");
    }

    @SubCommand(value = "force-load", permission = "heh.admin")
    public static void forceLoad(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        plugin.reset();
        msg(sender, "admin.info.load_done");
    }

    @SubCommand(value = "mailbox", permission = "heh.user")
    public static void openMailbox(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player player = (Player) sender;
        Market.openMailbox(player);
    }

    @SubCommand(value = "offer", permission = "heh.offer")
    public static void offer(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
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
    public static void view(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
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

    public static Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
    }

    public static void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(I18n.get(template, args));
    }

    public static ItemStack getItemInHand(CommandSender se) {
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

    public static class Arguments {

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

        public <T extends Enum<T>> T nextEnum(Class<T> cls) {
            String str = next();
            if (str == null) throw new BadCommandException("No more EnumValues in argument");
            try {
                return Enum.valueOf(cls, str);
            } catch (IllegalArgumentException ex) {
                String vals = "";
                for (T k : cls.getEnumConstants()) {
                    vals += k.name() + "|";
                }
                throw new BadCommandException(I18n.get("user.error.bad_enum", cls.getName(), vals));
            }
        }

        public boolean nextBoolean() {
            String str = next();
            if (str == null) throw new BadCommandException("No more booleans in argument");
            return Boolean.parseBoolean(str);
        }

        public int length() {
            return parsedArguments.size();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubCommand {
        String value();

        String permission() default "";
    }
}

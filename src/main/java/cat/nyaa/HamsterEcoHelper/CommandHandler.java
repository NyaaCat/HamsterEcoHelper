package cat.nyaa.HamsterEcoHelper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor {

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
                plugin.getLogger().warning("Bad subcommand handler: " + m.toString());
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
            ShowItem t = new ShowItem();
            t.setItem(player.getInventory().getItemInMainHand());
            t.setMessage("item: {item} {amount}");
            t.sendToPlayer(player);
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

package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.ads.AdsCommands;
import cat.nyaa.HamsterEcoHelper.auction.AuctionCommands;
import cat.nyaa.HamsterEcoHelper.balance.BalanceCommands;
import cat.nyaa.HamsterEcoHelper.market.MarketCommands;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SearchCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopCommands;
import cat.nyaa.HamsterEcoHelper.utils.GlobalMuteList;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.Message;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends CommandReceiver<HamsterEcoHelper> {

    private final HamsterEcoHelper plugin;
    @SubCommand("auction")
    public AuctionCommands auctionCommands;
    @SubCommand("requisition")
    public RequisitionCommands requisitionCommands;
    @SubCommand("market")
    public MarketCommands marketCommands;
    @SubCommand("balance")
    public BalanceCommands balanceCommands;
    @SubCommand("shop")
    public SignShopCommands signShopCommands;
    @SubCommand("ads")
    public AdsCommands adsCommands;
    @SubCommand("search")
    public SearchCommands searchCommands;

    public CommandHandler(HamsterEcoHelper plugin, Internationalization i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "";
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String cmd = args[0].toLowerCase();
            String subCommand = "";
            if (cmd.equals("view") || cmd.equals("offer")) {
                subCommand = "market";
            } else if (cmd.equals("sell") || cmd.equals("req")) {
                subCommand = "requisition";
            } else if (cmd.equals("auc") || cmd.equals("bid")) {
                subCommand = "auction";
            } else if (cmd.equals("search") || cmd.equals("searchpage")) {
                subCommand = "search";
            }
            if (subCommand.length() > 0) {
                String[] tmp = new String[args.length + 1];
                tmp[0] = subCommand;
                for (int i = 0; i < args.length; i++) {
                    tmp[i + 1] = args[i];
                }
                return super.onCommand(sender, command, label, tmp);
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    @SubCommand(value = "save", permission = "heh.admin")
    public void forceSave(CommandSender sender, Arguments args) {
        plugin.config.save();
        msg(sender, "admin.info.save_done");
    }


    @SubCommand(value = "debug", permission = "heh.debug")
    public void debug(CommandSender sender, Arguments args) {
        String sub = args.next();
        if ("showitem".equals(sub) && sender instanceof Player) {
            Player player = (Player) sender;
            new Message("Player has item: ").append(player.getInventory().getItemInMainHand()).send(player);
        } else if ("dbi".equals(sub) && sender instanceof Player) {
            plugin.database.addTemporaryStorage((Player) sender, new ItemStack(Material.DIAMOND, 64));
        } else if ("ymllist".equals(sub)) {
            List<ItemStack> t = new ArrayList<ItemStack>() {{
                add(new ItemStack(Material.DIAMOND));
                add(new ItemStack(Material.ACACIA_DOOR));
            }};
            YamlConfiguration yml = new YamlConfiguration();
            yml.addDefault("abc", t);
            yml.set("abc", t);
            sender.sendMessage("\n" + yml.saveToString());
        }
    }

    @SubCommand(value = "force-load", permission = "heh.admin")
    public void forceLoad(CommandSender sender, Arguments args) {
        plugin.reload();
        msg(sender, "admin.info.load_done");
    }

    @SubCommand(value = "version")
    public void version(CommandSender sender, Arguments args) {
        String ver = plugin.getDescription().getVersion();
        List<String> authors = plugin.getDescription().getAuthors();
        String au = authors.get(0);
        for (int i = 1; i < authors.size(); i++) {
            au += " " + authors.get(i);
        }
        msg(sender, "manual.license", ver, au);
    }

    @SubCommand(value = "mute", permission = "heh.mute")
    public void mute(CommandSender sender, Arguments args) {
        GlobalMuteList.add(asPlayer(sender));
        msg(sender, "user.info.mute");
    }

    @SubCommand(value = "unmute", permission = "heh.mute")
    public void unmute(CommandSender sender, Arguments args) {
        GlobalMuteList.remove(asPlayer(sender));
        msg(sender, "user.info.unmute");
    }

    @SubCommand(value = "retrieve", permission = "heh.retrieve")
    public void userRetrieve(CommandSender sender, Arguments args) {
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
        for (ItemStack s : items) {
            p.getWorld().dropItem(p.getEyeLocation(), s);
        }
        plugin.database.clearTemporaryStorage(p);
    }
}

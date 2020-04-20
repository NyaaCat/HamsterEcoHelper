package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.ads.AdsCommands;
import cat.nyaa.HamsterEcoHelper.auction.AuctionCommands;
import cat.nyaa.HamsterEcoHelper.balance.BalanceCommands;
import cat.nyaa.HamsterEcoHelper.kit.KitCommands;
import cat.nyaa.HamsterEcoHelper.market.MarketCommands;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SearchCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopCommands;
import cat.nyaa.HamsterEcoHelper.transaction.TransactionCommands;
import cat.nyaa.HamsterEcoHelper.utils.GlobalMuteList;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends CommandReceiver {

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
    @SubCommand("transaction")
    public TransactionCommands transactionCommands;
    @SubCommand("kit")
    public KitCommands kitCommands;

    public CommandHandler(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "save", permission = "heh.admin")
    public void forceSave(CommandSender sender, Arguments args) {
        plugin.config.save();
        msg(sender, "admin.info.save_done");
    }


    @SubCommand(value = "debug", permission = "heh.debug")
    public void debug(CommandSender sender, Arguments args) {
        String sub = args.nextString();
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
            try{
                p.getWorld().dropItem(p.getEyeLocation(), s);
            }catch (Exception e){
                Bukkit.getLogger().warning("exception retrieving items.");
                e.printStackTrace();
            }
        }
        plugin.database.clearTemporaryStorage(p);
    }
}

class HMCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HMCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "market.offer";
    }

    @SubCommand(value = "", permission = "heh.offer", isDefaultCommand = true)
    public void offer(CommandSender sender, Arguments args) {
        if (args.remains() == 0) {
            plugin.commandHandler.marketCommands.view(sender, args);
        } else {
            plugin.commandHandler.marketCommands.offer(sender, args);
        }
    }
}

class HSellToCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HSellToCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "transaction.sellto";
    }

    @SubCommand(value = "", permission = "heh.transaction.sellto", isDefaultCommand = true)
    public void sellto(CommandSender sender, Arguments args) {
        plugin.commandHandler.transactionCommands.sellTo(sender, args);
    }
}

class HPayCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HPayCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "transaction.pay";
    }

    @SubCommand(value = "", permission = "heh.transaction.pay", isDefaultCommand = true)
    public void pay(CommandSender sender, Arguments args) {
        plugin.commandHandler.transactionCommands.pay(sender, args);
    }
}

class HAucCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HAucCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "auction.auc";
    }

    @SubCommand(value = "", permission = "heh.userauc", isDefaultCommand = true)
    public void auc(CommandSender sender, Arguments args) {
        plugin.commandHandler.auctionCommands.Auc(sender, args);
    }
}

class HBidCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HBidCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "auction.bid";
    }

    @SubCommand(value = "", permission = "heh.bid", isDefaultCommand = true)
    public void bid(CommandSender sender, Arguments args) {
        plugin.commandHandler.auctionCommands.userBid(sender, args, true);
    }
}

class HReqCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HReqCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "requisition.req";
    }

    @SubCommand(value = "", permission = "heh.userreq", isDefaultCommand = true, tabCompleter = "reqTabComplete")
    public void req(CommandSender sender, Arguments args) {
        plugin.commandHandler.requisitionCommands.Requisition(sender, args);
    }

    public List<String> reqTabComplete(CommandSender sender, Arguments args) {
        List<String> list = new ArrayList<>();
        if (args.remains() == 1) {
            list.addAll(plugin.commandHandler.requisitionCommands.tabCompleteItemName(sender, args));
        }
        return list;
    }
}

class HSellCommand extends CommandReceiver {

    private final HamsterEcoHelper plugin;

    public HSellCommand(HamsterEcoHelper plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "requisition.sell";
    }

    @SubCommand(value = "", permission = "heh.sell", isDefaultCommand = true)
    public void sell(CommandSender sender, Arguments args) {
        plugin.commandHandler.requisitionCommands.userSell(sender, args);
    }
}
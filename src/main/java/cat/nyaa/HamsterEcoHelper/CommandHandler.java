package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.ads.AdsCommands;
import cat.nyaa.HamsterEcoHelper.auction.AuctionCommands;
import cat.nyaa.HamsterEcoHelper.balance.BalanceCommands;
import cat.nyaa.HamsterEcoHelper.database.*;
import cat.nyaa.HamsterEcoHelper.market.MarketCommands;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SearchCommands;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopCommands;
import cat.nyaa.HamsterEcoHelper.utils.GlobalMuteList;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.database.Database;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.RelationalDB;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.librazy.nyaautils_lang_checker.LangKey;

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

    public CommandHandler(HamsterEcoHelper plugin, LanguageRepository i18n) {
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

    @SubCommand(value = "dump", permission = "heh.admin")
    public void databaseDump(CommandSender sender, Arguments args) {
        String from = args.next();
        RelationalDB todb =  plugin.database.database;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (RelationalDB fromdb = DatabaseUtils.get(from).connect()) {
                fromdb.beginTransaction();
                todb.beginTransaction();
                int r = 0;
                List<ItemLog> itemLogs = fromdb.query(ItemLog.class).select();
                r = itemLogs.size();
                msg(sender, "admin.info.dump.ing", ItemLog.class.getName(), from, r);
                for (ItemLog itemLog : itemLogs) {
                    r--;
                    todb.query(ItemLog.class).insert(itemLog);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", ItemLog.class.getName(), from, r);
                    }
                }

                List<LottoStorageLocation> lottoStorageLocations = fromdb.query(LottoStorageLocation.class).select();
                r = lottoStorageLocations.size();
                msg(sender, "admin.info.dump.ing", LottoStorageLocation.class.getName(), from, r);
                for (LottoStorageLocation lottoStorageLocation : lottoStorageLocations) {
                    r--;
                    todb.query(LottoStorageLocation.class).insert(lottoStorageLocation);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", LottoStorageLocation.class.getName(), from, r);
                    }
                }

                List<MarketItem> marketItems = fromdb.query(MarketItem.class).select();
                r = marketItems.size();
                msg(sender, "admin.info.dump.ing", MarketItem.class.getName(), from, r);
                for (MarketItem marketItem : marketItems) {
                    r--;
                    todb.query(MarketItem.class).insert(marketItem);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", MarketItem.class.getName(), from, r);
                    }
                }

                List<ShopStorageLocation> shopStorageLocations = fromdb.query(ShopStorageLocation.class).select();
                r = shopStorageLocations.size();
                msg(sender, "admin.info.dump.ing", ShopStorageLocation.class.getName(), from, r);
                for (ShopStorageLocation shopStorageLocation : shopStorageLocations) {
                    r--;
                    todb.query(ShopStorageLocation.class).insert(shopStorageLocation);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", ShopStorageLocation.class.getName(), from, r);
                    }
                }

                List<Sign> signs = fromdb.query(Sign.class).select();
                r = signs.size();
                msg(sender, "admin.info.dump.ing", Sign.class.getName(), from, r);
                for (Sign sign : signs) {
                    r--;
                    todb.query(Sign.class).insert(sign);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", Sign.class.getName(), from, r);
                    }
                }


                List<SignShop> signShops = fromdb.query(SignShop.class).select();
                r = signShops.size();
                msg(sender, "admin.info.dump.ing", SignShop.class.getName(), from, r);
                for (SignShop signShop : signShops) {
                    r--;
                    todb.query(SignShop.class).insert(signShop);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", SignShop.class.getName(), from, r);
                    }
                }

                List<TempStorageRepo> tempStorageRepos = fromdb.query(TempStorageRepo.class).select();
                r = tempStorageRepos.size();
                msg(sender, "admin.info.dump.ing", TempStorageRepo.class.getName(), from, r);
                for (TempStorageRepo tempStorageRepo : tempStorageRepos) {
                    r--;
                    todb.query(TempStorageRepo.class).insert(tempStorageRepo);
                    if(r %100 == 0){
                        msg(sender, "admin.info.dump.ing", TempStorageRepo.class.getName(), from, r);
                    }
                }

                fromdb.commitTransaction();
                todb.commitTransaction();
                msg(sender, "admin.info.dump.finished", from);
            }
        });
    }
}

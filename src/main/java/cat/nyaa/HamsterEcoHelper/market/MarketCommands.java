package cat.nyaa.HamsterEcoHelper.market;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.Database;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.*;

public class MarketCommands {
    
    @SubCommand(value = "offer", permission = "heh.offer")
    public static void offer(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        if (args.length() == 2) {
            Player player = asPlayer(sender);
            double price = 0.0;
            try {
                price = Double.parseDouble(new DecimalFormat("#.##").format(Double.parseDouble(args.next())));
            } catch (IllegalArgumentException ex) {
                //return;
            }
            if (!(price >= 0.01)) {
                msg(sender, "user.error.not_double");
                return;
            }
            ItemStack item = getItemInHand(sender);
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                if (MarketManager.offer(player, item, price)) {
                    player.getInventory().setItemInMainHand(null);
                }
                return;
            } else {
                msg(sender, "user.info.not_item_hand");
                return;
            }
        }
    }

    @SubCommand(value = "view", permission = "heh.view")
    public static void view(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player player = asPlayer(sender);
        ItemStack[] mailbox = MarketManager.getMailbox(player);
        boolean save = false;
        for (int i = 0; i < mailbox.length; i++) {
            ItemStack item = mailbox[i];
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                Utils.giveItem(player, item);
                mailbox[i] = new ItemStack(Material.AIR);
                save = true;
            }
        }
        if (save) {
            MarketManager.setMailbox(player, mailbox);
        }
        if (args.length() == 2) {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(args.next());
            if (seller != null) {
                MarketManager.openGUI(player, 1, seller.getUniqueId());
            }
        } else {
            MarketManager.openGUI(player, 1, null);
        }
    }

    @SubCommand(value = "givemarketitem", permission = "heh.givemarketitem")
    public static void give(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player player = asPlayer(sender);
        Database.MarketItem item = MarketManager.getItem(args.nextInt());
        if (item != null) {
            int slot = player.getInventory().firstEmpty();
            if (slot >= 0 && player.getInventory().getItem(slot) == null) {
                msg(player, "user.market.offered", item.getPlayerName());
                msg(player, "user.market.unit_price", item.getUnitPrice());
                player.getInventory().setItem(slot, item.getItemStack(1));
            }
        }
    }
}

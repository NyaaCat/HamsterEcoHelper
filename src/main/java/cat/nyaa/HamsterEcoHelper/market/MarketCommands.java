package cat.nyaa.HamsterEcoHelper.market;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

import static cat.nyaa.HamsterEcoHelper.CommandHandler.*;

public class MarketCommands {
    @SubCommand(value = "mailbox", permission = "heh.user")
    public static void openMailbox(CommandSender sender, Arguments args, HamsterEcoHelper plugin) {
        Player player = asPlayer(sender);
        Market.openMailbox(player);
    }

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
                if (Market.offer(player, item, price)) {
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
        if (args.length() == 2) {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(args.next());
            if (seller != null) {
                Market.view(player, 1, seller.getUniqueId());
            }
        } else {
            Market.view(player, 1, null);
        }
    }
}

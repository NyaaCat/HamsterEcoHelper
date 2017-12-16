package cat.nyaa.HamsterEcoHelper.market;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MarketCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public MarketCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "market";
    }

    @SubCommand(value = "offer", permission = "heh.offer")
    public void offer(CommandSender sender, Arguments args) {
        if (args.length() == 3) {
            Player player = asPlayer(sender);
            double price = 0.0;
            price = args.nextDouble("#.##");
            if (!(price >= 0.01)) {
                msg(sender, "user.error.not_double");
                return;
            }
            ItemStack item = getItemInHand(sender);
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                if (plugin.marketManager.offer(player, item, price)) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
                return;
            } else {
                msg(sender, "user.info.not_item_hand");
                return;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SubCommand(value = "view", permission = "heh.view")
    public void view(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (args.length() == 3) {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(args.next());
            if (seller != null) {
                plugin.marketManager.openGUI(player, 1, seller.getUniqueId());
            }
        } else {
            plugin.marketManager.openGUI(player, 1, null);
        }
    }

    @SubCommand(value = "give", permission = "heh.giveitem")
    public void give(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        ItemStack item = plugin.database.getItemByID(args.nextInt());
        player.getInventory().addItem(item);
    }
}

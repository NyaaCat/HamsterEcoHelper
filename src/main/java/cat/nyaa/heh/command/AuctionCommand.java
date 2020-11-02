package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.auction.Auction;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class AuctionCommand extends CommandReceiver implements ShortcutCommand {

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public AuctionCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    private static final String PERMISSION_AUCTION = "heh.business.auction";
    private static final String PERMISSION_ADMIN = "heh.admin.run";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_AUCTION, tabCompleter = "auctionCompleter")
    public void onAuction(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.auction.no_item")).send(sender);
            return;
        }

        double basePrice = arguments.nextDouble();
        double stepPrice = Math.max(arguments.nextDouble(), 1);
        double reservePrice = basePrice;

        if (basePrice <= 0 || stepPrice < 0 ){
            throw new BadCommandException(I18n.format("command.auction.bad_input"));
        }
        if (Auction.hasAuction()) {
            new Message(I18n.format("command.auction.exist")).send(sender);
            return;
        }
        if (arguments.top() != null) {
            reservePrice = arguments.nextDouble();
        }
        boolean isSystemAuc = false;
        if (sender.hasPermission(PERMISSION_ADMIN) && arguments.top() != null){
            isSystemAuc = arguments.nextBoolean();
        }
        UUID from = isSystemAuc? SystemAccountUtils.getSystemUuid() : player.getUniqueId();

        ShopItem item = ShopItemManager.newShopItem(from, ShopItemType.AUCTION, itemInMainHand.clone(), basePrice);
        ShopItemManager.insertShopItem(item);
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        Auction.startAuction(item, basePrice, stepPrice, reservePrice);
    }

    public List<String> auctionCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<base price>");
                break;
            case 2:
                completeStr.add("<step price>");
                break;
            case 3:
                completeStr.add("<reserve price>");
                break;
            case 4:
                if (sender.hasPermission(PERMISSION_ADMIN)) {
                    completeStr.add("<is system auc?>");
                }
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getShortcutName() {
        return "hauc";
    }
}

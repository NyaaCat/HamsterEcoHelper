package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.item.ShopItemManager;
import cat.nyaa.heh.item.ShopItemType;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cat.nyaa.heh.command.CommandUtils.filtered;
import static cat.nyaa.heh.command.CommandUtils.getOnlinePlayers;

public class SellToCommand extends CommandReceiver {

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public SellToCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    private static final String PERMISSION_SELLTO = "heh.business.sellto";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_SELLTO, tabCompleter = "sellToCompleter")
    public void onSellTo(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand().clone()  ;
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.sellto.no_item")).send(sender);
            return;
        }

        Player sellToPlayer = arguments.nextPlayer();
        double price = arguments.nextDouble();

        int amount = itemInMainHand.getAmount();
        double unitPrice = price / amount;
        ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.DIRECT, itemInMainHand, unitPrice);
        ShopItemManager.insertShopItem(shopItem);
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        double realPrice = shopItem.getUnitPrice() * amount;
        new Message("").append(I18n.format("command.sellto.incoming_invoice", player.getName(), realPrice, shopItem.getUid()), shopItem.getItemStack())
                .send(sellToPlayer);
        new Message("").append(I18n.format("command.sellto.invoice_created", sellToPlayer.getName(), realPrice, shopItem.getUid()), shopItem.getItemStack())
                .send(sender);
    }

    public List<String> sellToCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.addAll(getOnlinePlayers());
                break;
            case 2:
                completeStr.add("<price>");
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
}

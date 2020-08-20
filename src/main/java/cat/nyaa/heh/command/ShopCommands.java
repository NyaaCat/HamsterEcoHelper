package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopSell;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.item.ShopItemManager;
import cat.nyaa.heh.item.ShopItemType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class ShopCommands extends CommandReceiver {

    private static final String PERMISSION_SHOP = "heh.business.signshop";

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public ShopCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    @SubCommand(value = "sell", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onSell(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.shop.no_item")).send(sender);
            return;
        }
        double unitPrice = arguments.nextDouble();
        ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.SIGNSHOP_SELL, itemInMainHand, unitPrice);
        ShopItemManager.insertShopItem(shopItem);
        new Message("").append(I18n.format("command.shop.sell.success", unitPrice), shopItem.getItemStack())
                .send(sender);
        UiManager.getInstance().getSignShopUis(player.getUniqueId()).stream()
                .forEach(SignShopGUI::refreshGUI);
    }

    @SubCommand(value = "buy", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onBuy(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.shop.no_item")).send(sender);
            return;
        }
        double unitPrice = arguments.nextDouble();
        ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.SIGNSHOP_BUY, itemInMainHand, unitPrice);
        ShopItemManager.insertShopItem(shopItem);
        new Message("").append(I18n.format("command.shop.buy.success", unitPrice), shopItem.getItemStack())
                .send(sender);
        UiManager.getInstance().getSignShopUis(player.getUniqueId()).stream()
                .forEach(SignShopGUI::refreshGUI);
    }

    @SubCommand(value = "create", permission = PERMISSION_SHOP, tabCompleter = "createCompleter")
    public void onCreate(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlock(null, 10);
        if (!(targetBlock.getState() instanceof Sign)){
            new Message(I18n.format("command.sign.create.not_sign")).send(sender);
            return;
        }
        Sign sign = ((Sign) targetBlock.getState());
        String type = arguments.nextString().toUpperCase();
        List<String> msgs = new ArrayList<>();
        while (arguments.top() != null){
            msgs.add(Utils.colored(arguments.nextString()));
        }
        BaseSignShop shop;
        switch (type){
            case "SELL":
                shop = new SignShopSell(player.getUniqueId());
                shop.setSign(sign);
                break;
            case "BUY":
                shop = new SignShopBuy(player.getUniqueId());
                shop.setSign(sign);
                break;
            default:
                new Message(I18n.format("command.sign.create.bad_type", type)).send(sender);
                return;
        }
        sign.setLine(0, shop.getTitle());

        int msgSize = msgs.size();
        for (int i = 0; i < 3; i++) {
            String line = i < msgSize ? "" : msgs.get(i);
            sign.setLine(i+1, line);
        }
        sign.update();
        new Message(I18n.format("command.sign.create.success")).send(sender);
    }

    public List<String> createCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("buy");
                completeStr.add("sell");
                break;
        }
        return filtered(arguments, completeStr);
    }

    public List<String> sellCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<unit price>");
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
}

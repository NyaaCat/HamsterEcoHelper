package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.signshop.*;
import cat.nyaa.heh.ui.LottoGUI;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class ShopCommands extends CommandReceiver implements ShortcutCommand{

    private static final String PERMISSION_SHOP = "heh.business.signshop";
    private static final String PERMISSION_ADMIN = "heh.admin.remove";

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

    @SubCommand(value = "lotto", permission = PERMISSION_SHOP, tabCompleter = "lottoCompleter")
    public void onLotto(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        String action = arguments.next();
        switch (action){
            case "view":
                SignShopLotto signShopLotto = new SignShopLotto(player.getUniqueId(), 0);
                signShopLotto.newGUI().open(player);
                break;
            case "offer":
                if (itemInMainHand.getType().isAir()){
                    new Message(I18n.format("command.shop.no_item")).send(player);
                    return;
                }
                SignShopLotto signShopLotto1 = new SignShopLotto(player.getUniqueId(), 0);
                signShopLotto1.loadItems();
                List<ShopItem> items = signShopLotto1.getItems();
                int size = items.size();
                if (size >= 54){
                    new Message(I18n.format("command.shop.lotto.limited")).send(player);
                    return;
                }
                ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.LOTTO, itemInMainHand, 0);
                ShopItemManager.insertShopItem(shopItem);
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                UiManager.getInstance().getLottoGUIs(signShopLotto1.getOwner()).forEach(LottoGUI::refreshGUI);
                new Message(I18n.format("command.shop.lotto.offer")).send(player);
                break;
        }
    }

    @SubCommand(value = "sell", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onSell(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlockExact = player.getTargetBlockExact(10);
        if (targetBlockExact == null || !(targetBlockExact.getState() instanceof Sign) || !(isShopSign(targetBlockExact))){
            new Message(I18n.format("command.shop.sell.wrong_target")).send(sender);
            return;
        }
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(targetBlockExact.getLocation());
        if (!shopAt.getOwner().equals(player.getUniqueId()) || !(shopAt instanceof SignShopSell)){
            new Message(I18n.format("command.shop.sell.wrong_target")).send(sender);
            return;
        }
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.shop.sell.no_item")).send(sender);
            return;
        }
        double unitPrice = arguments.nextDouble();
        ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.SIGN_SHOP_SELL, itemInMainHand, unitPrice);
        ShopItemManager.insertShopItem(shopItem);
        new Message("").append(I18n.format("command.shop.sell.success", unitPrice), shopItem.getItemStack())
                .send(sender);
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        UiManager.getInstance().getSignShopUis(player.getUniqueId()).stream()
                .forEach(SignShopGUI::refreshGUI);
    }

    private boolean isShopSign(Block block1) {
        return SignShopManager.getInstance().isSignShop(block1);
    }

    @SubCommand(value = "buy", permission = PERMISSION_SHOP, tabCompleter = "sellCompleter")
    public void onBuy(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlockExact = player.getTargetBlockExact(10);
        if (targetBlockExact == null || !(targetBlockExact.getState() instanceof Sign) || !(isShopSign(targetBlockExact))){
            new Message(I18n.format("command.shop.sell.wrong_target")).send(sender);
            return;
        }
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(targetBlockExact.getLocation());
        if (!shopAt.getOwner().equals(player.getUniqueId()) || !(shopAt instanceof SignShopBuy)){
            new Message(I18n.format("command.shop.sell.wrong_target")).send(sender);
            return;
        }
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()){
            new Message(I18n.format("command.shop.no_item")).send(sender);
            return;
        }
        double unitPrice = arguments.nextDouble();
        ShopItem shopItem = ShopItemManager.newShopItem(player.getUniqueId(), ShopItemType.SIGN_SHOP_BUY, itemInMainHand, unitPrice);
        ShopItemManager.insertShopItem(shopItem);
        new Message("").append(I18n.format("command.shop.buy.success", shopItem.getAmount(), unitPrice), shopItem.getItemStack())
                .send(sender);
        UiManager.getInstance().getSignShopUis(player.getUniqueId()).stream()
                .forEach(SignShopGUI::refreshGUI);
    }

    @SubCommand(value = "create", permission = PERMISSION_SHOP, tabCompleter = "createCompleter")
    public void onCreate(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlockExact(10);
        if (!(targetBlock.getState() instanceof Sign)){
            new Message(I18n.format("command.sign.create.not_sign")).send(sender);
            return;
        }
        Sign sign = ((Sign) targetBlock.getState());
        String[] lines = sign.getLines();
        if (Arrays.stream(lines).anyMatch(s -> s!=null && !s.equals(""))){
            new Message(I18n.format("command.sign.create.not_empty")).send(sender);
            return;
        }

        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(targetBlock.getLocation());
        if (shopAt!=null){
            new Message(I18n.format("command.sign.create.existed")).send(sender);
            return;
        }

        String type = arguments.nextString().toUpperCase();
        List<String> msgs = new ArrayList<>();
        while (arguments.top() != null){
            msgs.add(Utils.colored(arguments.nextString()));
        }
        BaseSignShop shop;
        switch (type){
            case "SELL":
                shop = new SignShopSell(player.getUniqueId());
                shop.setLores(msgs);
                shop.setSign(sign);
                break;
            case "BUY":
                shop = new SignShopBuy(player.getUniqueId());
                shop.setLores(msgs);
                shop.setSign(sign);
                break;
            case "LOTTO":
                if (msgs.size()<=0){
                    throw new BadCommandException("command.sign.create.lotto.bad_price", "");
                }
                String priceStr = msgs.get(0);
                try{
                    double price = Double.parseDouble(priceStr);
                    msgs.remove(0);
                    shop = new SignShopLotto(player.getUniqueId(), price);
                    shop.setLores(msgs);
                    shop.setSign(sign);
                }catch (NumberFormatException | NullPointerException e){
                    new Message(I18n.format("command.sign.create.lotto.bad_price", priceStr));
                    return;
                }
                break;
            default:
                new Message(I18n.format("command.sign.create.bad_type", type)).send(sender);
                return;
        }
        SignShopManager.getInstance().addShop(shop);
        shop.updateSign();
        new Message(I18n.format("command.sign.create.success")).send(sender);
    }

    @SubCommand(value = "remove", permission = PERMISSION_SHOP, tabCompleter = "removeCompleter")
    public void onRemove(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlockExact(10);
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(targetBlock.getLocation());
        String action = arguments.top();
        if (action != null){
            if (action.equals("buy")){
                arguments.next();
                shopAt.loadItems();
                List<ShopItem> items = shopAt.getItems();
                items.sort(Comparator.comparingLong(ShopItem::getUid));
                if (arguments.remains() == 0){
                    new Message(I18n.format("command.remove.buy.message")).send(sender);
                    for (int i = 0; i < items.size(); i++) {
                        ShopItem shopItem = items.get(i);
                        new Message("").append(I18n.format("command.remove.buy.item", i), shopItem.getModel()).send(sender);
                    }
                    return;
                }
                int index = arguments.nextInt();
                ShopItem shopItem = items.get(index);
                ShopItemManager.getInstance().invalidateItem(shopItem);
                UiManager.getInstance().getSignShopUis(shopAt.getOwner()).forEach(SignShopGUI::refreshGUI);
                new Message("").append(I18n.format("command.remove.buy.success", index), shopItem.getModel()).send(sender);
            }
            return;
        }
        if (shopAt == null){
            new Message(I18n.format("shop.remove.not_sign_shop")).send(sender);
            return;
        }
        if (!sender.hasPermission(PERMISSION_ADMIN) && !shopAt.getOwner().equals(player.getUniqueId())) {
            new Message(I18n.format("shop.remove.not_owner")).send(sender);
            return;
        }
        SignShopManager.getInstance().removeShopAt(shopAt);
        new Message(I18n.format("shop.remove.success")).send(sender);
    }

    public List<String> lottoCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("view");
                completeStr.add("offer");
                break;
        }
        return filtered(arguments, completeStr);
    }


    public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("buy");
                break;
        }
        return filtered(arguments, completeStr);
    }


    public List<String> createCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("buy");
                completeStr.add("sell");
                completeStr.add("lotto");
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

    @Override
    public String getShortcutName() {
        return "hshop";
    }
}

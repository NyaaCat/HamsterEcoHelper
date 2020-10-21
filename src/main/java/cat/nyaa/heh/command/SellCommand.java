package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.auction.Requisition;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.business.signshop.SignShopBuy;
import cat.nyaa.heh.business.signshop.SignShopManager;
import cat.nyaa.heh.business.transaction.*;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class SellCommand extends CommandReceiver implements ShortcutCommand{
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public SellCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    private static final String PERMISSION_SELL = "heh.business.sell";

    private boolean isShopSign(Block block1) {
        Sign sign = (Sign) block1.getState();
        return SignShopManager.getInstance().isSignShop(sign);
    }

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_SELL, tabCompleter = "sellCompleter")
    public void onSell(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);

        Block targetBlockExact = player.getTargetBlockExact(10);
        if (targetBlockExact != null && isShopSign(targetBlockExact)){
            try {
                if (sellToShopSign(player, arguments, targetBlockExact)) {
                }
                return;
            }catch (Exception e){
                new Message(I18n.format("command.sell.failed")).send(sender);
                return;
            }
        }

        if (!Requisition.hasRequisition()) {
            new Message(I18n.format("command.sell.no_requisition")).send(sender);
            return;
        }
        sellToRequisition(player, arguments);
    }

    private boolean sellToRequisition(Player player, Arguments arguments) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        Requisition requisition = Requisition.currentRequisition();
        if (!requisition.isValidItem(itemInMainHand)) {
            new Message(I18n.format("command.sell.invalid_item")).send(player);
            return false;
        }
        String input = arguments.top();
        int amountToSell = 0;
        if ("all".equals(input)){
            arguments.next();
            amountToSell = itemInMainHand.getAmount();
        }else {
            amountToSell = arguments.nextInt();
        }
        if (itemInMainHand.getAmount() < amountToSell){
            new Message(I18n.format("command.sell.insufficient_amount")).send(player);
            return false;
        }
        if (amountToSell<=0){
            new Message(I18n.format("command.sell.invalid_amount"));
            return false;
        }
        int remains = requisition.remains();
        if (amountToSell > remains){
            new Message(I18n.format("command.sell.oversold", amountToSell, remains)).send(player);
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack clone = itemInMainHand.clone();
        itemInMainHand.setAmount(Math.max(itemInMainHand.getAmount() - amountToSell, 0));
        inventory.setItemInMainHand(itemInMainHand);
        try{
            ItemStack clone1 = clone.clone();
            clone1.setAmount(amountToSell);
            requisition.onSell(player, clone1);
            return true;
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "error selling item: ", e);
            inventory.setItemInMainHand(clone);
        }
        return false;
    }

    private boolean sellToShopSign(Player player, Arguments arguments, Block targetBlockExact) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        String input = arguments.top();
        int amountToSell = 0;
        if ("all".equals(input)){
            arguments.next();
            amountToSell = itemInMainHand.getAmount();
        }else {
            amountToSell = arguments.nextInt();
        }
        BaseSignShop shopAt = SignShopManager.getInstance().getShopAt(targetBlockExact.getLocation());
        if (!(shopAt instanceof SignShopBuy) || shopAt.getOwner().equals(player.getUniqueId())) {
            new Message(I18n.format("command.sell.invalid_target")).send(player);
            return false;
        }
        shopAt.loadItems();
        List<ShopItem> items = shopAt.getItems();

        ShopItem shopItem1 = items.stream()
                .filter(shopItem -> isValidItem(shopItem, itemInMainHand))
                .sorted(Comparator.comparingLong(ShopItem::getUid))
                .findFirst().orElse(null);
        if (shopItem1 == null) {
            new Message(I18n.format("command.sell.invalid_item")).send(player);
            return false;
        }

        if (itemInMainHand.getAmount() < amountToSell){
            new Message(I18n.format("command.sell.insufficient_amount")).send(player);
            return false;
        }
        if (amountToSell<=0){
            new Message(I18n.format("command.sell.invalid_amount"));
            return false;
        }
        int finalAmountToSell = amountToSell;

        double amountToPay = shopItem1.getUnitPrice() * (1 + Tax.getTaxRate(shopItem1));
        if (!EcoUtils.getInstance().getEco().has(Bukkit.getOfflinePlayer(shopItem1.getOwner()), amountToPay)){
            new Message(I18n.format("transaction.sell.insufficient_funds")).send(player);
            return false;
        }
        TransactionRequest req = new TransactionRequest.TransactionBuilder()
                .buyer(shopItem1.getOwner())
                .seller(player.getUniqueId())
                .amount(finalAmountToSell)
                .item(shopItem1)
                .taxMode(TaxMode.ADDITION)
                .reason(TaxReason.REASON_SIGN_SHOP)
                .build();
        TransactionController.getInstance().makeTransaction(req);
        shopItem1.setSold(0);
        ShopItemManager.getInstance().updateShopItem(shopItem1);
        itemInMainHand.setAmount(Math.max(0, itemInMainHand.getAmount() - finalAmountToSell));
        return true;
    }

    public boolean isValidItem(ShopItem shopItem, ItemStack sellItem) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.itemTemplate = shopItem.getItemStack();
        return itemMatcher.matches(sellItem);
    }


    public List<String> sellCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<amount>");
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getHelpPrefix() {
        return "sell";
    }

    @Override
    public String getShortcutName() {
        return "hsell";
    }
}

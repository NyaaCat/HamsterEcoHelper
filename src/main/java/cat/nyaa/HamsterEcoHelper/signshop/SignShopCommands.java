package cat.nyaa.HamsterEcoHelper.signshop;

import cat.nyaa.HamsterEcoHelper.CommandHandler;
import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.ShopStorageLocation;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.Sign;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class SignShopCommands extends CommandReceiver {
    @SubCommand("storage")
    public StorageCommands storageCommands;
    @SubCommand("lotto")
    public LottoCommands lottoCommands;
    private HamsterEcoHelper plugin;

    public SignShopCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }

    @SubCommand(value = "buy", permission = "heh.signshop.buy")
    public void buy(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.shop.buy.usage");
            return;
        }
        Player player = asPlayer(sender);
        BlockIterator blockIterator = new BlockIterator(player, 5);
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (SignShopManager.isSign(block)) {
                Sign sign = plugin.signShopManager.getSign(block);
                if (sign != null && player.getUniqueId().equals(sign.getOwner())) {
                    if (plugin.signShopManager.getItemCount(player) >= plugin.signShopManager.getSlotLimit(player)) {
                        player.sendMessage(I18n.format("user.signshop.not_enough_slot"));
                        return;
                    }
                    ShopStorageLocation chest = plugin.signShopManager.getChestLocation(player.getUniqueId());
                    if (chest == null || chest.getLocation() == null ||
                            !SignShopManager.isChest(chest.getLocation().getBlock())) {
                        player.sendMessage(I18n.format("user.signshop.storage.set"));
                        return;
                    }
                    ItemStack itemStack = getItemInHand(sender).clone();
                    double unitPrice = 0.0;
                    unitPrice = args.nextDouble("#.##");
                    if (!(unitPrice >= 0.01)) {
                        msg(sender, "user.error.not_double");
                        return;
                    }
                    if (itemStack.getAmount() > 0) {
                        plugin.signShopManager.addItemToShop(player.getUniqueId(), itemStack.clone(), 1,
                                unitPrice, ShopMode.BUY);
                        player.sendMessage(I18n.format("user.signshop.sell.add"));
                    }
                    return;
                }
            }
        }
        player.sendMessage(I18n.format("user.signshop.not_sign"));
    }

    @SubCommand(value = "sell", permission = "heh.signshop.sell")
    public void sell(CommandSender sender, CommandHandler.Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.shop.sell.usage");
            return;
        }
        Player player = asPlayer(sender);
        BlockIterator blockIterator = new BlockIterator(player, 5);
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (SignShopManager.isSign(block)) {
                Sign sign = plugin.signShopManager.getSign(block);
                if (sign != null && player.getUniqueId().equals(sign.getOwner())) {
                    if (plugin.signShopManager.getItemCount(player) >= plugin.signShopManager.getSlotLimit(player)) {
                        player.sendMessage(I18n.format("user.signshop.not_enough_slot"));
                        return;
                    }
                    ItemStack itemStack = getItemInHand(sender).clone();
                    double unitPrice = args.nextDouble("#.##");
                    if (!(unitPrice >= 0.01)) {
                        msg(sender, "user.error.not_double");
                        return;
                    }
                    if(MarketManager.containsBook(itemStack)){
                        msg(sender,"user.error.shulker_box_contains_book");
                        return;
                    }
                    if (itemStack.getAmount() > 0) {
                        plugin.signShopManager.addItemToShop(player.getUniqueId(), itemStack.clone(),
                                itemStack.getAmount(), unitPrice, ShopMode.SELL);
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        player.sendMessage(I18n.format("user.signshop.buy.add"));
                        plugin.signShopManager.updateGUI(player.getUniqueId(), ShopMode.SELL);
                    }
                    return;
                }
            }
        }
        player.sendMessage(I18n.format("user.signshop.not_sign"));
    }
}
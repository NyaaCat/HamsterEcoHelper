package cat.nyaa.HamsterEcoHelper.signshop;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop.ShopStorageLocation;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StorageCommands extends CommandReceiver<HamsterEcoHelper> {

    private final HamsterEcoHelper plugin;

    public StorageCommands(Object plugin, Internationalization i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    public String getHelpPrefix() {
        return "storage";
    }

    @SubCommand(value = "info", permission = "heh.signshop.buy")
    public void info(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        ShopStorageLocation chest = plugin.signShopManager.getChestLocation(player.getUniqueId());
        if (chest == null || chest.getLocation() == null || !SignShopManager.isChest(chest.getLocation().getBlock())) {
            player.sendMessage(I18n._("user.signshop.storage.set"));
            return;
        }
        player.sendMessage(I18n._("user.signshop.storage.info", chest.getWorld(),
                chest.getLocation().getBlockX(), chest.getLocation().getBlockY(), chest.getLocation().getBlockZ()));
    }

    @SubCommand(value = "set", permission = "heh.signshop.buy")
    public void setStorage(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        player.sendMessage(I18n._("user.signshop.storage.select_chest"));
        plugin.signShopListener.selectChest.put(player.getUniqueId(), ShopMode.BUY);
    }
}

package cat.nyaa.HamsterEcoHelper.kit;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.database.Kit;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class KitCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public KitCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "kit";
    }

    @SubCommand(value = "create", permission = "heh.kit.admin")
    public void commandCreate(CommandSender sender, Arguments args) {
        if (args.length() < 3) {
            msg(sender, "manual.kit.create.usage");
            return;
        }
        Player player = asPlayer(sender);
        String kitName = args.nextString();
        Kit kit = plugin.kitManager.getKit(kitName);
        boolean replaceOldKit = kit != null && args.top() != null && args.nextBoolean();
        if (kit == null || replaceOldKit) {
            List<ItemStack> tmp = new ArrayList<>();
            for (int i = 9; i <= 35; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    tmp.add(item.clone());
                }
            }
            if (tmp.isEmpty()) {
                msg(sender, "user.kit.create.empty");
                return;
            }
            plugin.kitManager.createKit(kitName, tmp, replaceOldKit);
            msg(sender, "user.kit.create.success", kitName);
        } else {
            msg(sender, "user.kit.create.exist", kitName);
        }
    }

    @SubCommand(value = "view", permission = "heh.kit.admin")
    public void commandView(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.kit.view.usage");
            return;
        }
        Player player = asPlayer(sender);
        String kitName = args.nextString();
        Inventory inv = Bukkit.createInventory(null, 27, kitName);
        inv.setContents(getKit(kitName).getItems().toArray(new ItemStack[0]));
        player.openInventory(inv);
    }

    @SubCommand(value = "remove", permission = "heh.kit.admin")
    public void commandRemove(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.kit.remove.usage");
            return;
        }
        String kitName = args.nextString();
        if (getKit(kitName) != null) {
            plugin.database.removeKit(kitName);
            plugin.database.removeKitRecord(kitName);
            msg(sender, "user.kit.remove", kitName);
        }
    }

    @SubCommand(value = "give", permission = "heh.kit.admin")
    public void commandGive(CommandSender sender, Arguments args) {
        if (args.length() == 4) {
            Kit kit = getKit(args.nextString());
            HashSet<Player> players = new HashSet<>();
            if ("all".equalsIgnoreCase(args.top())) {
                players.addAll(Bukkit.getOnlinePlayers());
            } else {
                players.add(args.nextPlayer());
            }
            for (Player p : players) {
                if (InventoryUtils.addItems(p.getInventory(), kit.getItems())) {
                    p.sendMessage(I18n.format("user.kit.give.receive", kit.id));
                    msg(sender, "user.kit.give.success", kit.id, p.getName());
                } else {
                    p.sendMessage(I18n.format("user.kit.not_enough_space"));
                    msg(sender, "user.kit.give.error", p.getName());
                }
            }
            return;
        }
        msg(sender, "manual.kit.give.usage");
    }

    @SubCommand(value = "reset", permission = "heh.kit.admin")
    public void commandReset(CommandSender sender, Arguments args) {
        if (args.length() == 4) {
            Kit kit = getKit(args.nextString());
            if ("all".equalsIgnoreCase(args.top())) {
                plugin.database.removeKitRecord(kit.id);
            } else {
                OfflinePlayer p = args.nextOfflinePlayer();
                plugin.database.removeKitRecord(kit.id, p);
            }
            msg(sender, "user.kit.reset.success");
            return;
        }
        msg(sender, "manual.kit.reset.usage");
    }

    private Kit getKit(String kitName) {
        Kit kit = plugin.kitManager.getKit(kitName);
        if (kit != null) {
            return kit;
        }
        throw new CommandReceiver.BadCommandException("user.kit.not_exist", kitName);
    }
}

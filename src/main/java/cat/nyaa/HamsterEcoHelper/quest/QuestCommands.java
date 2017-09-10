package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.quest.gui.PaginatedListGui;
import cat.nyaa.HamsterEcoHelper.quest.gui.PairList;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public class QuestCommands extends CommandReceiver{
    private final HamsterEcoHelper plugin;

    public QuestCommands(HamsterEcoHelper plugin, I18n i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "quest";
    }

    @SubCommand(value = "add", permission = "heh.quest.post")
    public void postQuest(CommandSender sender, Arguments args) {
        Sign stationSign = getSignLookat(sender);
        QuestStation station = QuestCommon.toQuestStation(stationSign.getLocation());
        if (station == null) {
            msg(sender, "user.quest.not_station");
            return;
        }
        new QuestWizard(station.id, asPlayer(sender), 30);
    }

    public PaginatedListGui gui;
    @SubCommand(value = "test", permission = "heh.quest.admin")
    public void testCmd(CommandSender sender, Arguments args) {
        gui = new PaginatedListGui("Dummy") {
            @Override
            protected PairList<String, ItemStack> getFullGuiContent() {
                PairList<String, ItemStack> ret = new PairList<>();
                for (int i = 0; i < 150;i++) {
                    ItemStack n = new ItemStack(Material.BOOK);
                    ItemMeta m = n.getItemMeta();
                    m.setDisplayName("Book #" + Integer.toString(i));
                    n.setItemMeta(m);
                    ret.put(Integer.toString(i), n);
                }
                return ret;
            }

            @Override
            protected void itemClicked(Player player, String itemKey) {
                player.sendMessage("Clicked item #" + itemKey);
            }
        };
        gui.openFor(asPlayer(sender));
    }

    public Sign getSignLookat(CommandSender sender) {
        Player p = asPlayer(sender);
        Block b = p.getTargetBlock((Set<Material>) null, 5);// TODO use nms rayTrace

        if (b == null || !b.getType().isBlock() || (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST)) {
            throw new BadCommandException("user.error.not_sign");
        }
        return (Sign)b.getState();
    }
}

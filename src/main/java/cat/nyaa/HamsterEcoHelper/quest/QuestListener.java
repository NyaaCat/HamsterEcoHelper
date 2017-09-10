package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.quest.gui.QuestStationGui;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class QuestListener implements Listener {
    private final HamsterEcoHelper plugin;

    public QuestListener(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        if ("[Quest]".equalsIgnoreCase(event.getLine(0))) {
            Player player = event.getPlayer();
            if (!player.hasPermission("heh.quest.admin")) return;
            Double fee;
            try {
                fee = Double.parseDouble(event.getLine(3));
            } catch (NumberFormatException ex) {
                fee = null;
            }
            if (fee == null) {
                event.getPlayer().sendMessage(I18n.format("user.quest.invalid_fee"));
                return;
            }
            plugin.database.query(QuestStation.class).insert(new QuestStation(event.getBlock().getLocation(), fee));
            event.getPlayer().sendMessage(I18n.format("user.quest.station_created"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent ev) {
        Block b = ev.getBlock();
        if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
            Location loc = b.getLocation();
            if (QuestCommon.hasStation(loc)) {
                if (!ev.getPlayer().hasPermission("heh.quest.admin")) {
                    ev.getPlayer().sendMessage(I18n.format("user.quest.cannot_break"));
                    ev.setCancelled(true);
                } else {
                    // TODO check quests
                    QuestCommon.removeStation(loc);
                    ev.getPlayer().sendMessage(I18n.format("user.quest.station_removed"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            QuestStation station = QuestCommon.toQuestStation(block.getLocation());
            if (station == null) return;
            event.getPlayer().sendMessage("Station clicked");
            List<QuestEntry> quests = plugin.database.query(QuestEntry.class).whereEq("station_id", station.id).select();
            for (QuestEntry q : quests) {
                event.getPlayer().sendMessage("QUEST: " + q.questName);
            }
            QuestStationGui.getStationGui(station).openFor(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClicked(InventoryClickEvent ev) {
        if (ev.getClickedInventory().getHolder() instanceof QuestStationGui) {
            ((QuestStationGui)ev.getClickedInventory().getHolder()).onInventoryClicked(ev);
        }
        if (ev.getClickedInventory().getHolder() == plugin.commandHandler.questCommands.gui) {
            plugin.commandHandler.questCommands.gui.onInventoryClicked(ev);
        }
    }
}

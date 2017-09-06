package cat.nyaa.HamsterEcoHelper.quest.gui;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class QuestStationGui extends PaginatedListGui {
    // static methods to make gui unique for each station
    private static final Map<String, QuestStationGui> cachedGui = new HashMap<>();
    public static QuestStationGui getStationGui(QuestStation station) {
        QuestStationGui gui = cachedGui.get(station.id);
        if (gui == null) {
            gui = new QuestStationGui(station);
            cachedGui.put(station.id, gui);
        }
        return gui;
    }

    // non-static members

    private final String stationId;
    private QuestStationGui(QuestStation station) {
        super("Quest Station #" + station.id);
        stationId = station.id;
    }

    @Override
    protected PairList<String, ItemStack> getFullGuiContent() {
        PairList<String, ItemStack> pair = new PairList<>();
        List<QuestEntry> quests = HamsterEcoHelper.instance.database.query(QuestEntry.class).whereEq("station_id", stationId).select();
        for (QuestEntry e : quests) {
            ItemStack i = new ItemStack(Material.BOOK_AND_QUILL);
            ItemMeta m = i.getItemMeta();
            m.setDisplayName(e.questName);
            i.setItemMeta(m);
            pair.put(e.id, i);
        }
        return pair;
    }

    @Override
    protected void itemClicked(Player player, String itemKey) {
        QuestEntry e = HamsterEcoHelper.instance.database.query(QuestEntry.class).whereEq("id", itemKey).selectUnique();
        player.sendMessage("Quest: " + e.questName);
        player.sendMessage(e.questDescription);
        if (e.rewardType == QuestEntry.QuestType.ITEM) {
            for (ItemStack i : e.rewardItem) {
                new Message("").append(i).send(player);
            }
        }
    }
}

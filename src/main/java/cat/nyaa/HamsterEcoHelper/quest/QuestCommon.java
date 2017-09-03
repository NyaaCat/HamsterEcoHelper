package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestInstance;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import org.bukkit.Location;


public class QuestCommon {
    public static QuestStation toQuestStation(Location loc) {
        try {
            return HamsterEcoHelper.instance.database.query(QuestStation.class)
                    .whereEq("world", loc.getWorld().getName())
                    .whereEq("x", loc.getBlockX())
                    .whereEq("y", loc.getBlockY())
                    .whereEq("z", loc.getBlockZ())
                    .selectUnique();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}

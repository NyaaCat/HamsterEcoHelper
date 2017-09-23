package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.Database;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestInstance;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestInstance.QuestStatus.*;


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

    public static boolean hasStation(Location loc) {
        return HamsterEcoHelper.instance.database.query(QuestStation.class)
                .whereEq("world", loc.getWorld().getName())
                .whereEq("x", loc.getBlockX())
                .whereEq("y", loc.getBlockY())
                .whereEq("z", loc.getBlockZ())
                .count() > 0;
    }

    public static void removeStation(Location loc) {
        HamsterEcoHelper.instance.database.query(QuestStation.class)
                .whereEq("world", loc.getWorld().getName())
                .whereEq("x", loc.getBlockX())
                .whereEq("y", loc.getBlockY())
                .whereEq("z", loc.getBlockZ())
                .delete();
    }

    public static void claimQuest(Player player, String questId) {
        if (!player.isOnline()) throw new RuntimeException("user.quest.menu.unexpected_exception");
        String playerId = player.getUniqueId().toString();
        QuestEntry quest;
        Database db = HamsterEcoHelper.instance.database;
        try {
            quest = db.query(QuestEntry.class)
                    .whereEq("id", questId)
                    .selectUnique();
        } catch (RuntimeException ex) {
            throw new RuntimeException("user.quest.menu.unexpected_exception");
        }

        if (!db.query(QuestInstance.class)
                .whereEq("claimer", player.getUniqueId())
                .whereEq("status", IN_PROGRESS.name())
                .select().isEmpty()) {
            throw new RuntimeException("user.quest.menu.reject_unfinished");
        }

        if (!quest.claimable || quest.masked) throw new RuntimeException("user.quest.menu.reject_unavailable");
        if (quest.isExpired()) throw new RuntimeException("user.quest.menu.reject_expired");
        // TODO prereq etc.

        else {
            QuestInstance q = new QuestInstance();
            q.id = UUID.randomUUID().toString();
            q.questId = questId;
            q.claimer = playerId;
            q.status = IN_PROGRESS;
            q.startTime = ZonedDateTime.now();

            if (!quest.isRecurrentQuest) {
                quest.claimable = false;
                db.query(QuestEntry.class).whereEq("id", questId).update(quest, "claimable");
            }
            db.query(QuestInstance.class).insert(q);
        }
    }

    /* Check the database for all in-progress quest of this player and submit them */
    public static void submitQuest(Player player) {
        String playerId = player.getUniqueId().toString();
        Database db = HamsterEcoHelper.instance.database;
        List<QuestInstance> quests = db.query(QuestInstance.class)
                .whereEq("claimer", player.getUniqueId())
                .whereEq("status", IN_PROGRESS.name())
                .select();
        for (QuestInstance q : quests) {
            String questId = q.questId;
            QuestEntry e = db.query(QuestEntry.class).whereEq("id", questId).selectUniqueUnchecked();
            if (e == null) {
                player.sendMessage(I18n.format("user.quest.submit.no_entry", questId));
                q.status = INVALID;
                q.endTime = ZonedDateTime.now();
                db.query(QuestInstance.class).whereEq("id", q.id).update(q, "status", "end_time");
                continue;
            }

            player.sendMessage(I18n.format("user.quest.submit.submitting", e.questName));

            if (!e.completedInTime(q.startTime)) {
                player.sendMessage(I18n.format("user.quest.submit.timeout"));
                q.status = TIMEOUT;
                q.endTime = ZonedDateTime.now();
                if (!e.isRecurrentQuest && !e.isExpired()) {
                    e.claimable = true;
                    db.query(QuestEntry.class).whereEq("id", e.id).update(e, "claimable");
                }
                db.query(QuestInstance.class).whereEq("id", q.id).update(q, "status", "end_time");
                continue;
            }

            if (e.targetType == QuestEntry.QuestType.OTHER) {
                player.sendMessage(I18n.format("user.quest.submit.need_verification"));
                UUID publisherId = UUID.fromString(e.publisher);
                if (Bukkit.getPlayer(publisherId) != null) Bukkit.getPlayer(publisherId).sendMessage(
                        I18n.format("user.quest.quest_need_verify", player.getName(), e.questName));
                q.status = UNVERIFIED;
                q.endTime = ZonedDateTime.now();
                db.query(QuestInstance.class).whereEq("id", q.id).update(q, "status", "end_time");
            } else if (e.targetType == QuestEntry.QuestType.ITEM) {
                List<ItemStack> ret = InventoryUtils.withdrawInventoryAtomic(player.getInventory(), e.targetItems);
                if (ret == null) {
                    UUID publisherId = UUID.fromString(e.publisher);
                    for (ItemStack item : e.targetItems) Utils.giveItem(Bukkit.getOfflinePlayer(publisherId), item);
                    msgIfOnline(e.publisher, "user.quest.quest_complete_by");
                    switch (e.rewardType) {
                        case ITEM: for (ItemStack i : e.rewardItem) Utils.giveItem(player, i); break;
                        case MONEY: HamsterEcoHelper.instance.eco.deposit(player, e.rewardMoney); break;
                        default: break;
                    }
                    q.status = COMPLETED;
                    q.endTime = ZonedDateTime.now();
                    db.query(QuestInstance.class).whereEq("id", q.id).update(q, "status", "end_time");
                    player.sendMessage(I18n.format("user.quest.submit.quest_complete", e.id));
                } else {
                    player.sendMessage(I18n.format("user.quest.submit.target_not_satisfy", e.id));
                }
            } else {
                player.sendMessage(I18n.format("user.quest.submit.bad_target_type", e.id));
                q.status = INVALID;
                q.endTime = ZonedDateTime.now();
                db.query(QuestInstance.class).whereEq("id", q.id).update(q, "status", "end_time");
            }
        }
    }

    public static void confirmQuest(String questInstanceId, boolean confirmed) {
        Database db = HamsterEcoHelper.instance.database;
        QuestInstance questInstance = db.query(QuestInstance.class).whereEq("id", questInstanceId).selectUnique();
        if (questInstance.status != UNVERIFIED) throw new IllegalArgumentException("quest status incorrect, expecting UNVERIFIED: " + questInstanceId);
        QuestEntry questEntry = db.query(QuestEntry.class).whereEq("id", questInstance.questId).selectUnique();
        UUID claimerId = UUID.fromString(questInstance.claimer);
        if (!confirmed) {
            msgIfOnline(claimerId, "user.quest.rejected", questEntry.questName);
            questInstance.status = QuestInstance.QuestStatus.REJECTED;
            db.query(QuestInstance.class).whereEq("id", questInstanceId).update(questInstance, "status");
            if (!questEntry.isRecurrentQuest && !questEntry.isExpired()) {
                questEntry.claimable = true;
                db.query(QuestEntry.class).whereEq("id", questEntry.id).update(questEntry, "claimable");
            }
        } else {
            OfflinePlayer claimer = Bukkit.getOfflinePlayer(claimerId);
            switch (questEntry.rewardType) {
                case ITEM: for (ItemStack i : questEntry.rewardItem) Utils.giveItem(claimer, i); break;
                case MONEY: HamsterEcoHelper.instance.eco.deposit(claimer, questEntry.rewardMoney); break;
                default: break;
            }
            questInstance.status = COMPLETED;
            db.query(QuestInstance.class).whereEq("id", questInstanceId).update(questInstance, "status");
            msgIfOnline(claimerId, "user.quest.verified", questEntry.questName);
        }
    }

    public static void cancelQuest(String questInstanceId) {
        Database db = HamsterEcoHelper.instance.database;
        QuestInstance questInstance = db.query(QuestInstance.class).whereEq("id", questInstanceId).selectUnique();
        if (questInstance.status != IN_PROGRESS && questInstance.status != UNVERIFIED)
            throw new IllegalArgumentException("quest status incorrect, expecting IN_PROGRESS or UNVERIFIED: " + questInstanceId);
        QuestEntry questEntry = db.query(QuestEntry.class).whereEq("id", questInstance.questId).selectUnique();
        msgIfOnline(questInstance.claimer, "user.quest.cancelled", questEntry.questName);
        questInstance.status = QuestInstance.QuestStatus.CANCELLED;
        questInstance.endTime = ZonedDateTime.now();
        db.query(QuestInstance.class).whereEq("id", questInstance.id).update(questInstance, "status", "end_time");
        if (!questEntry.isRecurrentQuest && !questEntry.isExpired()) {
            questEntry.claimable = true;
            db.query(QuestEntry.class).whereEq("id", questEntry.id).update(questEntry, "claimable");
        }
    }

    public static void withdrawQuest(String questEntryId) {
        Database db = HamsterEcoHelper.instance.database;
        QuestEntry questEntry = db.query(QuestEntry.class).whereEq("id", questEntryId).selectUnique();
        for (QuestInstance qi : db.query(QuestInstance.class).whereEq("questId", questEntryId).select())
            if (qi.status == IN_PROGRESS || qi.status == UNVERIFIED)
                cancelQuest(qi.id);
        questEntry.masked = true;
        db.query(QuestEntry.class).whereEq("id", questEntryId).update(questEntry, "masked");
        msgIfOnline(questEntry.publisher, "user.quest.withdrawn", questEntry.questName);
    }

    private static void msgIfOnline(String id, String template, Object... objs) {;
        UUID uid = UUID.fromString(id);
        msgIfOnline(uid, template, objs);
    }

    private static void msgIfOnline(UUID uid, String template, Object... objs) {
        if (Bukkit.getPlayer(uid) != null) {
            Bukkit.getPlayer(uid).sendMessage(I18n.format(template, objs));
        }
    }
}

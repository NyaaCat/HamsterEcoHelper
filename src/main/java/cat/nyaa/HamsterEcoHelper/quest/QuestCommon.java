package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.Database;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestInstance;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import cat.nyaa.nyaacore.database.BaseDatabase;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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

        if (!quest.claimable) throw new RuntimeException("user.quest.menu.reject_unavailable");
        if (quest.isExpired()) throw new RuntimeException("user.quest.menu.reject_expired");
        // TODO prereq etc.

        else {
            QuestInstance q = new QuestInstance();
            q.id = UUID.randomUUID().toString();
            q.questId = questId;
            q.claimer = playerId;
            q.status = IN_PROGRESS;
            q.startTime = ZonedDateTime.now();

            quest.claimable = false;

            db.query(QuestEntry.class).whereEq("id", questId).update(quest, "claimable");
            db.query(QuestInstance.class).insert(q);
        }
    }

    public static void submitQuest(Player player) {
        String playerId = player.getUniqueId().toString();
        Database db = HamsterEcoHelper.instance.database;
        List<QuestInstance> quests = db.query(QuestInstance.class)
                .whereEq("claimer", player.getUniqueId())
                .whereEq("status", IN_PROGRESS.name())
                .select();
        for (QuestInstance q : quests) {
            String questId = q.questId;
            QuestEntry e = selectUniqueUnchecked(db.query(QuestEntry.class).whereEq("id", questId));
            // TODO reset quest claimable
            if (e == null) {
                player.sendMessage(I18n.format("user.quest.submit.no_entry", questId));
                q.status = CANCELLED;
                db.query(QuestInstance.class).update(q, "status");
                continue;
            }

            if (!e.completedInTime(q.startTime)) {
                player.sendMessage(I18n.format("user.quest.submit.timeout"));
                q.status = TIMEOUT;
                db.query(QuestInstance.class).update(q, "status");
                continue;
            }

            if (e.targetType == QuestEntry.QuestType.OTHER) {
                player.sendMessage(I18n.format("user.quest.submit.need_verification"));
                q.status = UNVERIFIED;
                db.query(QuestInstance.class).update(q, "status");
            } else if (e.targetType == QuestEntry.QuestType.ITEM) {
                List<ItemStack> ret = withdrawInventoryAtomic(player.getInventory(), e.targetItems);
                if (ret == null) {
                    // TODO return target item to publisher notification.
                    //for (ItemStack item : e.targetItems) Utils.giveItem(Bukkit.getOfflinePlayer(UUID.fromString(e.publisher)), item);
                    switch (e.rewardType) {
                        case ITEM: for (ItemStack i : e.rewardItem) Utils.giveItem(player, i); break;
                        case MONEY: HamsterEcoHelper.instance.eco.deposit(player, e.rewardMoney); break;
                        default: break;
                    }
                    q.status = COMPLETED;
                    // TODO update endTime
                    db.query(QuestInstance.class).update(q, "status");
                    player.sendMessage(I18n.format("user.quest.submit.quest_complete", e.id));
                } else {
                    player.sendMessage(I18n.format("user.quest.submit.target_not_satisfy", e.id));
                }
            } else {
                player.sendMessage(I18n.format("user.quest.submit.bad_target_type", e.id));
                q.status = CANCELLED;
                db.query(QuestInstance.class).update(q, "status");
            }



        }
    }

    // TODO move to NC
    private static <T> T selectUniqueUnchecked(BaseDatabase.Query<T> q) {
        try {
            return q.selectUnique();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * Remove items from inventory.
     * Either all removed or none removed.
     * @param inv the inventory
     * @param itemToBeTaken items to be removed
     * @return If null, then all designated items are removed. If not null, it contains the items missing
     * TODO move to NC
     */
    private static List<ItemStack> withdrawInventoryAtomic(Inventory inv, List<ItemStack> itemToBeTaken) {
        ItemStack[] itemStacks = inv.getContents();
        ItemStack[] cloneStacks = new ItemStack[itemStacks.length];
        for (int i = 0; i < itemStacks.length; i++) {
            cloneStacks[i] = itemStacks[i] == null ? null : itemStacks[i].clone();
        }

        List<ItemStack> ret = new ArrayList<>();

        for (ItemStack item : itemToBeTaken) {
            int sizeReq = item.getAmount();

            for (int i = 0; i < cloneStacks.length;i++) {
                if (cloneStacks[i] == null) continue;
                if (cloneStacks[i].isSimilar(item)) {
                    int sizeSupp = cloneStacks[i].getAmount();
                    if (sizeSupp > sizeReq) {
                        cloneStacks[i].setAmount(sizeSupp - sizeReq);
                        sizeReq = 0;
                        break;
                    } else {
                        cloneStacks[i] = null;
                        sizeReq -= sizeSupp;
                        if (sizeReq == 0) break;
                    }
                }
            }

            if (sizeReq > 0) {
                ItemStack n = item.clone();
                item.setAmount(sizeReq);
                ret.add(n);
            }
        }

        if (ret.size() == 0) {
            inv.setContents(cloneStacks);
            return null;
        } else {
            return ret;
        }
    }

}

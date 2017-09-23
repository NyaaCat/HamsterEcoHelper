package cat.nyaa.HamsterEcoHelper.utils.database.tables.quest;

import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataTable("quest_entry")
public class QuestEntry {
    public static final ZonedDateTime NEVER_EXPIRE = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC+0"));
    public static final Duration NO_TIME_LIMIT = Duration.ZERO.minusSeconds(1);

    public enum QuestType {
        NONE,
        ITEM,
        MONEY,
        OTHER;
    }

    @DataColumn
    @PrimaryKey
    public String id = UUID.randomUUID().toString();
    @DataColumn("station_id")
    public String stationId = ""; // which station this quest is in
    @DataColumn
    public String questName = "";
    @DataColumn
    public String questDescription = "";
    @DataColumn
    public String publisher = ""; // uuid of the player who published the quest
    @DataColumn
    public Boolean claimable = false; // if this quest can be claimed by a player
    @DataColumn
    public Boolean isRecurrentQuest = false; // if the quest can be claimed by many players. NOTE: the rewards will be created from nowhere (i.e. duplicated)
    @DataColumn
    public Long singlePlayerClaimLimit = -1L; // how many time a single player can claim this quest, valid only if is recurrent quest
    @DataColumn
    public String iconMaterial = "";
    @DataColumn
    public Boolean masked = false;

    public QuestType prerequisiteType = QuestType.NONE; // NONE, ITEM or MONEY
    public List<ItemStack> prerequisiteItems = new ArrayList<>();
    @DataColumn("prereq_money")
    public Double prerequisiteMoney = 0D;

    public List<ItemStack> earlyRewardItems = new ArrayList<>(); // items will be given to player when they claimed the quest

    public QuestType targetType = QuestType.NONE; // ITEM or OTHER
    public List<ItemStack> targetItems = new ArrayList<>();

    public QuestType rewardType = QuestType.NONE; // NONE, ITEM or MONEY
    public List<ItemStack> rewardItem = new ArrayList<>();
    @DataColumn("reward_money")
    public Double rewardMoney = 0D;

    public ZonedDateTime questExpire = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()); // when will the quest expire
    public Duration questTimeLimit = Duration.parse("PT-1H"); // the time limit the quest should be finished once it's claimed

    @DataColumn("prereq_type")
    public String getPrerequisiteType() {
        return prerequisiteType.name();
    }

    public void setPrerequisiteType(String prerequisiteType) {
        this.prerequisiteType = QuestType.valueOf(prerequisiteType);
    }

    @DataColumn("prereq_item")
    public String getPrerequisiteItems() {
        return Utils.encodeItemStacks(prerequisiteItems);
    }

    public void setPrerequisiteItems(String prerequisiteItems) {
        this.prerequisiteItems = Utils.decodeItemStacks(prerequisiteItems);
    }

    @DataColumn("early_reward_item")
    public String getEarlyRewardItems() {
        return Utils.encodeItemStacks(earlyRewardItems);
    }

    public void setEarlyRewardItems(String earlyRewardItems) {
        this.earlyRewardItems = Utils.decodeItemStacks(earlyRewardItems);
    }

    @DataColumn("target_type")
    public String getTargetType() {
        return targetType.name();
    }

    public void setTargetType(String targetType) {
        this.targetType = QuestType.valueOf(targetType);
    }

    @DataColumn("target_item")
    public String getTargetItems() {
        return Utils.encodeItemStacks(targetItems);
    }

    public void setTargetItems(String targetItems) {
        this.targetItems = Utils.decodeItemStacks(targetItems);
    }

    @DataColumn("reward_type")
    public String getRewardType() {
        return rewardType.name();
    }

    public void setRewardType(String rewardType) {
        this.rewardType = QuestType.valueOf(rewardType);
    }

    @DataColumn("reward_item")
    public String getRewardItem() {
        return Utils.encodeItemStacks(rewardItem);
    }

    public void setRewardItem(String rewardItem) {
        this.rewardItem = Utils.decodeItemStacks(rewardItem);
    }

    @DataColumn("expire")
    public String getQuestExpire() {
        return questExpire.toString();
    }

    public void setQuestExpire(String questExpire) {
        this.questExpire = ZonedDateTime.parse(questExpire);
    }

    @DataColumn("time_limit")
    public String getQuestTimeLimit() {
        return questTimeLimit.toString();
    }

    public void setQuestTimeLimit(String questTimeLimit) {
        this.questTimeLimit = Duration.parse(questTimeLimit);
    }

    public Material getIconMaterial() {
        try {
            return Material.valueOf(iconMaterial);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("icon material not exists for " + id);
            return Material.BOOK_AND_QUILL;
        }
    }

    public boolean isExpired() {
        if (questExpire.toEpochSecond() == 0) return false;
        return questExpire.isBefore(ZonedDateTime.now());
    }

    public boolean completedInTime(ZonedDateTime startTime) {
        if (questTimeLimit.isNegative()) return true;
        return startTime.plus(questTimeLimit).isAfter(ZonedDateTime.now());
    }
}

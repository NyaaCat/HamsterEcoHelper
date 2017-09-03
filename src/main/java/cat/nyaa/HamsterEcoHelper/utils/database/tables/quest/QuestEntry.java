package cat.nyaa.HamsterEcoHelper.utils.database.tables.quest;

import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@DataTable("quest_entry")
public class QuestEntry {
    public enum QuestType {
        NONE,
        ITEM,
        MONEY,
        OTHER;
    }

    @DataColumn
    @PrimaryKey
    public String id;
    @DataColumn
    public String station_id; // which station this quest is in
    @DataColumn
    public String questName;
    @DataColumn
    public String questDescription;
    @DataColumn
    public String publisher; // uuid of the player who published the quest
    @DataColumn
    public Boolean claimable; // if this quest can be claimed by a player
    @DataColumn
    public Boolean isRecurrentQuest; // if the quest can be claimed by many players. NOTE: the rewards will be created from nowhere (i.e. duplicated)
    @DataColumn
    public Long singlePlayerClaimLimit; // how many time a single player can claim this quest, valid only if is recurrent quest

    public QuestType prerequisiteType; // NONE, ITEM or MONEY
    public List<ItemStack> prerequisiteItems;
    @DataColumn("prereq_money")
    public Double prerequisiteMoney;

    public List<ItemStack> earlyRewardItems; // items will be given to player when they claimed the quest

    public QuestType targetType; // NONE, ITEM or OTHER
    public List<ItemStack> targetItems;

    public QuestType rewardType; // NONE, ITEM or MONEY
    public List<ItemStack> rewardItem;
    @DataColumn("reward_money")
    public Double rewardMoney;

    public ZonedDateTime questExpire; // when will the quest expire
    public Duration questTimeLimit; // the time limit the quest should be finished once it's claimed

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
}

package cat.nyaa.HamsterEcoHelper.utils.database.tables.quest;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@DataTable("quest_instance")
public class QuestInstance {
    private static final ZonedDateTime EPOCH = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC+0"));
    public enum QuestStatus {
        IN_PROGRESS,
        UNVERIFIED,

        COMPLETED,
        CANCELLED,
        REJECTED,
        TIMEOUT,
        INVALID;
    }

    @DataColumn
    @PrimaryKey
    public String id = "";
    @DataColumn
    public String questId = "";
    @DataColumn
    public String claimer = ""; // uuid of the player who claimed the quest

    public QuestStatus status = QuestStatus.INVALID;
    public ZonedDateTime startTime = EPOCH;
    public ZonedDateTime endTime = EPOCH;

    @DataColumn("status")
    public String getStatus() {
        return status.name();
    }

    public void setStatus(String status) {
        this.status = QuestStatus.valueOf(status);
    }

    @DataColumn("start_time")
    public String getStartTime() {
        return startTime.toString();
    }

    public void setStartTime(String startTime) {
        this.startTime = ZonedDateTime.parse(startTime);
    }

    @DataColumn("end_time")
    public String getEndTime() {
        return endTime.toString();
    }

    public void setEndTime(String endTime) {
        this.endTime = ZonedDateTime.parse(endTime);
    }
}

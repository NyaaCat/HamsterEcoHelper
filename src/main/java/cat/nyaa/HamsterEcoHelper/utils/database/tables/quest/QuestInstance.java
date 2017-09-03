package cat.nyaa.HamsterEcoHelper.utils.database.tables.quest;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;

import java.time.ZonedDateTime;

@DataTable("quest_instance")
public class QuestInstance {
    public enum QuestStatus {
        IN_PROGRESS,
        UNVERIFIED,

        COMPLETED,
        CANCELLED,
        TIMEOUT;
    }

    @DataColumn
    @PrimaryKey
    public String id;
    @DataColumn
    public String questId;
    @DataColumn
    public String claimer; // uuid of the player who claimed the quest

    public QuestStatus status;
    public ZonedDateTime startTime;
    public ZonedDateTime endTime;

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

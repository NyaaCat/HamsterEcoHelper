package cat.nyaa.HamsterEcoHelper.database;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "temporary_storage")
@Access(AccessType.FIELD)
public class TempStorageRepo {
    public UUID playerId;
    @Column(name = "yaml", columnDefinition = "LONGTEXT")
    public String yaml = "";

    @Access(AccessType.PROPERTY)
    @Column(name = "player_id")
    @Id
    public String getPlayerId() {
        return playerId.toString();
    }

    public void setPlayerId(String owner) {
        this.playerId = UUID.fromString(owner);
    }
}
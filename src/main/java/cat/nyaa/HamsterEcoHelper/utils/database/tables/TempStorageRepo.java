package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name = "temporary_storage")
public class TempStorageRepo {
    @Column(name = "player_id", length = 36)
    @Id
    public String playerId;
    @Column(name = "yaml")
    public String yaml = "";

    public UUID getPlayerId() {
        return UUID.fromString(playerId);
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId.toString();
    }

    public String getYaml() {
        return Base64.getEncoder().encodeToString(yaml.getBytes());
    }

    public void setYaml(String yaml) {
        this.yaml = new String(Base64.getDecoder().decode(yaml));
    }
}
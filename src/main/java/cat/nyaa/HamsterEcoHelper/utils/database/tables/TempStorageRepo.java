package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;

import java.util.Base64;
import java.util.UUID;

@DataTable("temporary_storage")
public class TempStorageRepo {
    @DataColumn("player_id")
    @PrimaryKey
    public String playerId;
    @DataColumn("yaml")
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
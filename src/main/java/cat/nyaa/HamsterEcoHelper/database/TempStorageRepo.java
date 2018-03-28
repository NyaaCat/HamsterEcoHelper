package cat.nyaa.HamsterEcoHelper.database;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table(name = "temporary_storage")
public class TempStorageRepo {
    @Column(name = "player_id")
    @Id
    public UUID playerId;
    @Column(name = "yaml")
    public String yaml = "";
}
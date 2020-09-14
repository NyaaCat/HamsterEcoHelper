package heh7_2.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.UUID;

@Table("temporary_storage")
public class TempStorageRepo {
    @Column(name = "player_id", primary = true)
    public UUID playerId;
    @Column(name = "yaml", columnDefinition = "MEDIUMTEXT")
    public String yaml = "";
}
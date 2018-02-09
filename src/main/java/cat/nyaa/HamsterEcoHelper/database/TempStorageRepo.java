package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;

import java.util.Base64;
import java.util.UUID;

@DataTable("temporary_storage")
public class TempStorageRepo {
    @DataColumn("player_id")
    @PrimaryKey
    public UUID playerId;
    @DataColumn("yaml")
    public String yaml = "";
}
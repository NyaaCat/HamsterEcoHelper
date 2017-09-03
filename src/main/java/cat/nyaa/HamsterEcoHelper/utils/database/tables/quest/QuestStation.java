package cat.nyaa.HamsterEcoHelper.utils.database.tables.quest;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@DataTable("quest_station")
public class QuestStation {
    @DataColumn
    @PrimaryKey
    public String id;
    @DataColumn
    public String world;
    @DataColumn
    public Long x;
    @DataColumn
    public Long y;
    @DataColumn
    public Long z;
    @DataColumn
    public Double postingFee;

    public QuestStation() { }

    public QuestStation(Location loc, Double fee) {
        id = UUID.randomUUID().toString();
        setLocation(loc);
        postingFee = fee;
    }

    public Location getLocation() {
        if (Bukkit.getServer().getWorld(world) != null) {
            return new Location(Bukkit.getServer().getWorld(world), x, y, z);
        }
        return null;
    }

    public void setLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.x = (long) loc.getBlockX();
        this.y = (long) loc.getBlockY();
        this.z = (long) loc.getBlockZ();
    }
}

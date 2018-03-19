package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@DataTable("signshop_lotto")
public class LottoStorageLocation {
    @DataColumn("owner")
    @PrimaryKey
    public UUID owner;
    @DataColumn("world")
    public String world;
    @DataColumn("x")
    public Long x;
    @DataColumn("y")
    public Long y;
    @DataColumn("z")
    public Long z;

    public LottoStorageLocation() {
    }

    public LottoStorageLocation(UUID player, Location loc) {
        this.owner = player;
        setLocation(loc);
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

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

}

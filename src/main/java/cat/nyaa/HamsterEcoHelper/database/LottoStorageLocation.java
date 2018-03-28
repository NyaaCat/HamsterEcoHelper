package cat.nyaa.HamsterEcoHelper.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table(name= "signshop_lotto")
public class LottoStorageLocation {
    @Column(name= "owner")
    @Id
    public UUID owner;
    @Column(name= "world")
    public String world;
    @Column(name= "x")
    public Long x;
    @Column(name= "y")
    public Long y;
    @Column(name= "z")
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

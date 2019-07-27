package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@Table("signshop_storage")

public class ShopStorageLocation {
    @Column(primary = true)
    public UUID owner;
    @Column
    public String world;
    @Column
    public Long x;
    @Column
    public Long y;
    @Column
    public Long z;

    public ShopStorageLocation() {
    }

    public ShopStorageLocation(UUID player, Location loc) {
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
}

package cat.nyaa.HamsterEcoHelper.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "signshop_storage")
@Access(AccessType.FIELD)
public class ShopStorageLocation {

    public UUID owner;
    @Column(name = "world")
    public String world;
    @Column(name = "x")
    public Long x;
    @Column(name = "y")
    public Long y;
    @Column(name = "z")
    public Long z;

    public ShopStorageLocation() {
    }

    public ShopStorageLocation(UUID player, Location loc) {
        this.owner = player;
        setLocation(loc);
    }

    @Access(AccessType.PROPERTY)
    @Column(name= "owner")
    @Id
    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(String owner) {
        this.owner = UUID.fromString(owner);
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

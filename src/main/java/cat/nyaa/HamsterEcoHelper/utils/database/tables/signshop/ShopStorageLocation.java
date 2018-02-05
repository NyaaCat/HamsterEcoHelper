package cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "signshop_storage")
public class ShopStorageLocation {
    @Column(name = "owner", length = 36)
    @Id
    public String owner;
    @Column(name = "world")
    public String world;
    @Column(name = "x")
    public Long x;
    @Column(name = "y")
    public Long y;
    @Column(name =  "z")
    public Long z;

    public ShopStorageLocation() {
    }

    public ShopStorageLocation(UUID player, Location loc) {
        setOwner(player);
        setLocation(loc);
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public Long getZ() {
        return z;
    }

    public void setZ(Long z) {
        this.z = z;
    }

    public Location getLocation() {
        if (Bukkit.getServer().getWorld(world) != null) {
            return new Location(Bukkit.getServer().getWorld(world), x, y, z);
        }
        return null;
    }

    public void setLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.setX((long) loc.getBlockX());
        this.setY((long) loc.getBlockY());
        this.setZ((long) loc.getBlockZ());
    }

    public UUID getOwner() {
        return UUID.fromString(owner);
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid.toString();
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(getOwner());
    }

}

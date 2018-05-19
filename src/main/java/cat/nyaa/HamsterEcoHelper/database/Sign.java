package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "signshop_location")
@Access(AccessType.FIELD)
public class Sign {
    @Column(name = "id")
    @Id
    public String id;
    public UUID owner;
    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    public ShopMode shopMode;
    @Column(name = "world")
    public String world = "";
    @Column(name = "x")
    public Long x;
    @Column(name = "y")
    public Long y;
    @Column(name = "z")
    public Long z;
    @Column(name = "lotto_price")
    public Double lotto_price = 0.0D;

    public Sign() {
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "owner")
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
        setLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public void setLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = (long) x;
        this.y = (long) y;
        this.z = (long) z;
        this.id = "world:" + world + ",x:" + x + ",y:" + y + ",z:" + z;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }


}
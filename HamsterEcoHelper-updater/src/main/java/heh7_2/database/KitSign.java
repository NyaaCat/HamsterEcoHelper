package heh7_2.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Table("kitsign_location")
public class KitSign {
    @Column(name = "id", primary = true)
    public String id;
    @Column(name = "world")
    public String world = "";
    @Column(name = "x")
    public Long x;
    @Column(name = "y")
    public Long y;
    @Column(name = "z")
    public Long z;
    @Column(name = "cost")
    public Double cost = 0.0D;
    @Column(name = "kit_name")
    public String kitName = "";
    @Column(name = "type")
    public SignType type = SignType.BUY;

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
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

    public enum SignType {
        BUY, RESET
    }
}
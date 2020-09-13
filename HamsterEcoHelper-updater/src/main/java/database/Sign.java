package database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Table("signshop_location")

public class Sign {
    @Column(primary = true)
    public String id;
    @Column
    public UUID owner;
    @Column(name = "mode")
    public ShopMode shopMode;
    @Column
    public String world = "";
    @Column
    public Long x;
    @Column
    public Long y;
    @Column
    public Long z;
    @Column
    public Double lotto_price = 0.0D;

    public Sign() {
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
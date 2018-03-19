package cat.nyaa.HamsterEcoHelper.database;

import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import cat.nyaa.nyaacore.database.DataColumn;
import cat.nyaa.nyaacore.database.DataTable;
import cat.nyaa.nyaacore.database.PrimaryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@DataTable("signshop_location")
public class Sign {
    @DataColumn("id")
    @PrimaryKey
    public String id;
    @DataColumn("owner")
    public UUID owner;
    @DataColumn("mode")
    public ShopMode shopMode;
    @DataColumn("world")
    public String world = "";
    @DataColumn("x")
    public Long x;
    @DataColumn("y")
    public Long y;
    @DataColumn("z")
    public Long z;
    @DataColumn("lotto_price")
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
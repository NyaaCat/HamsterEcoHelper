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
    public String owner;
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

    public Double getLotto_price() {
        return lotto_price;
    }

    public void setLotto_price(Double lotto_price) {
        this.lotto_price = lotto_price;
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

    @DataColumn("mode")
    public String getShopMode() {
        return shopMode.name();
    }

    public void setShopMode(String shopMode) {
        this.shopMode = ShopMode.valueOf(shopMode.toUpperCase());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.setId("world:" + world + ",x:" + x + ",y:" + y + ",z:" + z);
    }

    public void setLocation(String world, int x, int y, int z) {
        this.setWorld(world);
        this.setX((long) x);
        this.setY((long) y);
        this.setZ((long) z);
        this.setId("world:" + world + ",x:" + x + ",y:" + y + ",z:" + z);
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
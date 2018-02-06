package cat.nyaa.HamsterEcoHelper.utils.database.tables.signshop;

import cat.nyaa.HamsterEcoHelper.signshop.ShopMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "signshop_location")
public class Sign {
    @Column(name = "id", length = 36)
    @Id
    public String id;
    @Column(name = "owner")
    public String owner;
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

    @Column(name = "mode")
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
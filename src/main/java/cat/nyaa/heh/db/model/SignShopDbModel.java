package cat.nyaa.heh.db.model;

import cat.nyaa.heh.enums.SignShopType;
import cat.nyaa.heh.signshop.BaseSignShop;
import cat.nyaa.heh.signshop.SignShopBuy;
import cat.nyaa.heh.signshop.SignShopSell;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Location;

import java.util.UUID;

@Table("shop")
public class SignShopDbModel {
    @Column(name = "uid", primary = true)
    long uid;
    @Column(name = "world")
    String world;
    @Column(name = "x")
    int x;
    @Column(name = "y")
    int y;
    @Column(name = "z")
    int z;
    @Column(name = "owner")
    UUID owner;
    @Column(name = "type")
    SignShopType type;

    public SignShopDbModel() {
    }

    public SignShopDbModel(BaseSignShop signShop) {
        uid = -1;
        Location location = signShop.getLocation();
        this.world = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.owner = signShop.getOwner();
        this.type = signShop.getType();
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public SignShopType getType() {
        return type;
    }

    public void setType(SignShopType type) {
        this.type = type;
    }
}

package cat.nyaa.heh.db.model;

import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.UUID;

@Table("locations")
public class LocationDbModel {
    @Column(name = "uid", primary = true)
    private long uid;
    @Column(name = "world")
    private String world;
    @Column(name = "x")
    private double x;
    @Column(name = "y")
    private double y;
    @Column(name = "z")
    private double z;
    @Column(name = "type")
    private LocationType locationType;
    @Column(name = "entityuuid", nullable = true)
    private UUID entityUUID;
    @Column(name = "owner")
    private UUID owner;
    @Column(name = "data", nullable = true)
    private String data;

    private static final Gson gson = new Gson();

    public LocationDbModel(){}

    public LocationDbModel(BaseSignShop baseSignShop) {
        this.uid = baseSignShop.getUid();
        this.setLocationType(baseSignShop.getType());
        this.setLocation(baseSignShop.getLocation());
        this.setOwner(baseSignShop.getOwner());
        this.data = gson.toJson(baseSignShop.getData());
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public void setEntityUUID(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Block getBlock() {
        World world = Bukkit.getWorld(this.world);
        return new Location(world, x, y, z).getBlock();
    }

    public Entity getEntity(){
        return entityUUID == null ? null : Bukkit.getEntity(entityUUID);
    }

    public void setLocation(Location location) {
        this.x = location.getBlock().getX();
        this.y = location.getBlock().getY();
        this.z = location.getBlock().getZ();
        this.setWorld(location.getWorld().getName());
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

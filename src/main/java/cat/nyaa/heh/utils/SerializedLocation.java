package cat.nyaa.heh.utils;

import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializedLocation implements ISerializable {
    @Serializable
    String world = "";
    @Serializable
    double x = 0d;
    @Serializable
    double y = 0d;
    @Serializable
    double z = 0d;

    public SerializedLocation(){}

    public SerializedLocation(World world, double x, double y, double z) {
        this.world = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SerializedLocation(Location location){
        World world = location.getWorld();
        this.world = world == null? "" : world.getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}

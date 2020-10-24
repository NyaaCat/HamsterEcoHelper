package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.db.SignShopConnection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SignShopManager {
    private static SignShopManager INSTANCE;

    private SignShopManager() {
    }

    public static SignShopManager getInstance() {
        if (INSTANCE == null) {
            synchronized (SignShopManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SignShopManager();
                }
            }
        }
        return INSTANCE;
    }

    private Map<Location, BaseSignShop> locationMap = new HashMap<>();

    public void load(){
        SignShopConnection instance = SignShopConnection.getInstance();
        instance.getShops().stream().forEach(baseSignShop -> {
            locationMap.put(baseSignShop.getLocation(), baseSignShop);
        });
    }

    public void updateSigns(){
        locationMap.values().stream()
                .forEach(signShopSell ->
                        //can run updateSigns() asynchronously
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                signShopSell.updateSign();
                            }
                        }.runTask(HamsterEcoHelper.plugin)
                );
    }

    public boolean isSignShop(Block sign) {
        return locationMap.get(sign.getLocation()) != null;
    }

    public BaseSignShop getShopAt(Location location) {
        return locationMap.get(location);
    }

    public void removeShopAt(BaseSignShop shopAt) {
        locationMap.remove(shopAt.getLocation());
        SignShopConnection.getInstance().removeShop(shopAt);
    }

    public void addShop(BaseSignShop shop) {
        SignShopConnection.getInstance().addSignShop(shop);
        locationMap.put(shop.getLocation(), shop);
    }
}

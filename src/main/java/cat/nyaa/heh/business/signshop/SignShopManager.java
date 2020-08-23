package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.db.SignShopConnection;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Stream;

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

    private Map<UUID, List<SignShopSell>> sellMap = new HashMap<>();
    private Map<UUID, List<SignShopBuy>> buyMap = new HashMap<>();
    private Map<Location, BaseSignShop> locationMap = new HashMap<>();

    public List<SignShopSell> getSellShop(UUID uuid){
        return sellMap.get(uuid);
    }

    public List<SignShopBuy> getBuyShop(UUID uuid){
        return buyMap.get(uuid);
    }

    public void load(){
        SignShopConnection instance = SignShopConnection.getInstance();
        sellMap = instance.getSellShops();
        buyMap = instance.getBuyShops();
        buildLocationMap();
    }

    private void buildLocationMap() {
        locationMap.clear();
        Stream.of(buyMap.values(), sellMap.values())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .forEach(baseSignShop -> locationMap.put(baseSignShop.getLocation(), baseSignShop));
    }

    public void updateSigns(){
        sellMap.values().stream().flatMap(Collection::stream)
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

    public void createSellShop(Sign sign, UUID owner){
        SignShopSell signShopSell = new SignShopSell(owner);
        signShopSell.setSign(sign);
        signShopSell.updateSign();
        List<SignShopSell> signShopSells = sellMap.computeIfAbsent(signShopSell.getOwner(), uuid -> new ArrayList<>());
        signShopSells.add(signShopSell);
        locationMap.put(signShopSell.getLocation(), signShopSell);
        SignShopConnection.getInstance().addSignShop(signShopSell);
    }

    public void createBuyShop(Sign sign, UUID owner){
        SignShopBuy signShopBuy = new SignShopBuy(owner);
        signShopBuy.setSign(sign);
        signShopBuy.updateSign();
        List<SignShopBuy> signShopBuys = buyMap.computeIfAbsent(signShopBuy.getOwner(), uuid -> new ArrayList<>());
        signShopBuys.add(signShopBuy);
        locationMap.put(signShopBuy.getLocation(), signShopBuy);
        SignShopConnection.getInstance().addSignShop(signShopBuy);
    }

    public boolean isSignShop(Sign sign) {
        return locationMap.get(sign.getLocation()) != null;
    }

    public BaseSignShop getShopAt(Location location) {
        return locationMap.get(location);
    }

    public void removeShopAt(BaseSignShop shopAt) {
        UUID owner = shopAt.getOwner();
        if (shopAt instanceof SignShopBuy) {
            List<SignShopBuy> buyShops = buyMap.getOrDefault(owner, null);
            if (buyShops != null) {
                buyShops.remove(shopAt);
            }
        }
        if (shopAt instanceof SignShopSell) {
            List<SignShopSell> sellShops = sellMap.getOrDefault(owner, null);
            if (sellShops != null) {
                sellShops.remove(shopAt);
            }
        }
        locationMap.remove(shopAt.getLocation());
        SignShopConnection.getInstance().removeShop(shopAt);
    }

    public void addShop(BaseSignShop shop) {
        SignShopConnection.getInstance().addSignShop(shop);
    }
}

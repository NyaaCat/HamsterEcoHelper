package cat.nyaa.heh.signshop;

import cat.nyaa.heh.db.SignShopConnection;
import org.bukkit.block.Sign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    }

    public void createSellShop(Sign sign, UUID owner){
        SignShopSell signShopSell = new SignShopSell(owner);
        signShopSell.setSign(sign);
        SignShopConnection.getInstance().addSignShop(signShopSell);
    }
}

package cat.nyaa.heh.ui;

import cat.nyaa.heh.business.signshop.BaseSignShop;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UiManager {
    private static UiManager INSTANCE;

    private UiManager(){}

    public static UiManager getInstance(){
        if (INSTANCE == null){
            synchronized (UiManager.class){
                if (INSTANCE == null){
                    INSTANCE = new UiManager();
                }
            }
        }
        return INSTANCE;
    }

    private HashMap<Inventory, BaseUi> uiByInventory = new HashMap<>();
    private HashMap<UUID, BaseUi> uiByUuid = new HashMap<>();

    public MarketGUI newMarketGUI(){
        return addGUI(new MarketGUI());
    }

    public SignShopGUI newSignShopGUI(BaseSignShop signShop){
        return addGUI(new SignShopGUI(signShop));
    }

    private <T extends BaseUi> T addGUI(T marketGUI) {
        uiByInventory.put(marketGUI.getInventory(), marketGUI);
        uiByUuid.put(marketGUI.getUid(), marketGUI);
        return marketGUI;
    }

    public boolean isHehUi(Inventory inventory){
        return uiByInventory.keySet().contains(inventory);
    }

    public BaseUi getUi(Inventory inventory) {
        return uiByInventory.get(inventory);
    }

    public List<MarketGUI> getMarketUis() {
        return uiByUuid.values().stream().filter(baseUi -> baseUi instanceof MarketGUI)
                .map(baseUi -> (MarketGUI)baseUi)
                .collect(Collectors.toList());
    }

    public void removeUi(Inventory inventory) {
        BaseUi remove = uiByInventory.remove(inventory);
        if (remove != null){
            uiByUuid.remove(remove.getUid());
        }
    }

    public List<SignShopGUI> getSignShopUis(UUID uniqueId) {
        return uiByUuid.values().stream()
                .filter(baseUi -> baseUi instanceof SignShopGUI)
                .map(baseUi -> ((SignShopGUI) baseUi))
                .filter(signShopGUI -> signShopGUI.getOwner().equals(uniqueId))
                .collect(Collectors.toList());
    }
}

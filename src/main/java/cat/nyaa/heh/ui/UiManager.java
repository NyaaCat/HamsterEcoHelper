package cat.nyaa.heh.ui;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;

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

    private HashMap<Inventory, BaseUi> hehUiMap = new HashMap<>();

    public boolean isHehUi(Inventory inventory){
        return hehUiMap.keySet().contains(inventory);
    }

    public BaseUi getUi(Inventory inventory) {
        return hehUiMap.get(inventory);
    }

}

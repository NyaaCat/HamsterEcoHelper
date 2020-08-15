package cat.nyaa.heh.market;

import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.ui.MarketGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Market {
    private static Market INSTANCE;

    private Market() {
    }

    public static Market getInstance() {
        if (INSTANCE == null) {
            synchronized (Market.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Market();
                }
            }
        }
        return INSTANCE;
    }

    private List<ShopItem> marketItems = new ArrayList<>();
    private List<MarketGUI> marketGuiList = new ArrayList<>();

    public void loadItem(){
        List<ShopItem> items = MarketConnection.getInstance().getItems();
        marketItems = items;
        notifyViewers();
    }

    private void notifyViewers() {
        marketGuiList.forEach(marketGUI -> {
        });
    }

    public void offer(Player player, ItemStack itemStack, double unitPrice){
    }

    public void buy(ShopItem item, int amount){

    }


    public List<ShopItem> getMarketItems() {
        return getMarketItems(null);
    }

    public List<ShopItem> getMarketItems(UUID ownerFilter) {
        Stream<ShopItem> stream = marketItems.stream();
        if (ownerFilter != null){
            stream.filter(shopItem -> shopItem.getOwner().equals(ownerFilter));
        }
        return stream.collect(Collectors.toList());
    }
}

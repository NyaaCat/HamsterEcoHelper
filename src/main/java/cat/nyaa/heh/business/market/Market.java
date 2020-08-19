package cat.nyaa.heh.business.market;

import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.item.ShopItemType;
import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.transaction.TransactionController;
import cat.nyaa.heh.ui.MarketGUI;
import cat.nyaa.heh.ui.UiManager;
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

    public void loadItem(){
        List<ShopItem> items = MarketConnection.getInstance().getItems();
        marketItems = items;
    }

    private void refreshGUI() {
        this.marketItems = MarketConnection.getInstance().getItems();
        UiManager.getInstance().getMarketUis().forEach(marketGUI ->{
            marketGUI.refreshGUI(marketItems);
        });
    }

    private void closeAll(){
        UiManager.getInstance().getMarketUis().forEach(MarketGUI::refreshGUI);
    }

    public void offer(Player player, ItemStack itemStack, double unitPrice){
        ShopItem shopItem = new ShopItem(player.getUniqueId(), ShopItemType.MARKET, itemStack, unitPrice);
        MarketConnection.getInstance().addItem(shopItem);
        refreshGUI();
    }

    public void buy(Player buyer, ShopItem item, int amount){
        TransactionController.getInstance().makeTransaction(buyer.getUniqueId(), item.getOwner(), item, amount);
        refreshGUI();
    }

    public List<ShopItem> getMarketItems() {
        return getMarketItems(null);
    }

    public List<ShopItem> getMarketItems(UUID ownerFilter) {
        Stream<ShopItem> stream = marketItems.stream();
        if (ownerFilter != null){
            stream = stream.filter(shopItem -> shopItem.getOwner().equals(ownerFilter));
        }
        return stream.collect(Collectors.toList());
    }

    public void refreshItem(){

    }

    public void openGUI(Player player) {
        MarketGUI marketGUI = UiManager.getInstance().newMarketGUI();
        marketGUI.refreshGUI();
        player.openInventory(marketGUI.getInventory());
    }
}

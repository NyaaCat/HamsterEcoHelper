package cat.nyaa.heh.business.market;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.BaseShop;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.db.MarketConnection;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.MarketGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Market extends BaseShop {
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

    @Override
    public void loadItems(){
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
        new Message("").append(I18n.format("market.offer", unitPrice), itemStack).broadcast();
        String format = I18n.format("ui.message.open_market");
        new Message("").append(Utils.newMessageButton(format, new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(format)), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heh market"))).broadcast();
        refreshGUI();
    }

    public void buy(Player buyer, ShopItem item, int amount){
        double fee = HamsterEcoHelper.plugin.config.marketFeeBase;
        TransactionController.getInstance().makeTransaction(buyer.getUniqueId(), item.getOwner(), item, amount, fee, TaxReason.REASON_MARKET);
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

    @Override
    public void doBusiness(Player related, ShopItem item, int amount) {
        buy(related, item, amount);
    }

    @Override
    public LocationType getType() {
        return LocationType.MARKET;
    }

}

package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.transaction.Tax;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.business.transaction.TransactionRequest;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.DataModel;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.SystemAccountUtils;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.Message;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SignShopLotto extends BaseSignShop {
    double price;

    public SignShopLotto(UUID owner, double price) {
        super(owner);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public SignShopLotto(LocationDbModel model) {
        super(model);
        LottoData lottoData = DataModel.getGson().fromJson(model.getData(), LottoData.class);
        this.price = lottoData.lottoPrice;
    }

    public LocationDbModel toDbModel() {
        LocationDbModel locationDbModel = new LocationDbModel(this);
        return locationDbModel;
    }

    @Override
    public SignShopData getData() {
        LottoData lottoData = new LottoData();
        lottoData.lores = new ArrayList<>(this.lores);
        lottoData.lottoPrice = price;
        return lottoData;
    }

    @Override
    public List<String> getLores() {
        List<String> lores = super.getLores();
        lores.add(0, buildPriceLore());
        return lores;
    }

    private String buildPriceLore() {
        return I18n.format("ui.lotto.price", price);
    }

    @Override
    public String getTitle() {
        return I18n.format("ui.sign.lotto");
    }

    @Override
    public SignShopGUI newGUI() {
        SignShopGUI signShopGUI = UiManager.getInstance().newSignShopGUI(this);
        signShopGUI.refreshGUI();
        return signShopGUI;
    }

    @Override
    public boolean doBusiness(Player related, ShopItem item, int amount) {
       new BukkitRunnable(){
           @Override
           public void run() {
               try {
                   onLotto(related);
               } catch (Throwable e) {
                   HamsterEcoHelper.plugin.getLogger().log(Level.SEVERE, "error during player " + related.getName() + "'s lotto request: ", e);
               }
           }
       }.runTaskAsynchronously(HamsterEcoHelper.plugin);
       return true;
    }

    private static Cache<UUID, Inventory> inventoryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(10)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private void onLotto(Player related) {
        String name = SystemAccountUtils.isSystemAccount(getOwner()) ? SystemAccountUtils.getSystemName()
                : Bukkit.getOfflinePlayer(getOwner()).getName();
        try {
            List<ShopItem> lottoItems = SignShopConnection.getInstance().getLottoItems(getOwner());
            if(lottoItems == null){
                new Message(I18n.format("sign.error.invalid_sign")).send(related);
                return;
            }

            if (lottoItems.size() <=0){
                new Message("").append(I18n.format("shop.sign.lotto.no_item")).send(related);
                return;
            }
            ShopItem item = Utils.randomSelect(lottoItems);
            if (item == null || item.getItemStack().getType().isAir()) {
                new Message("").append(I18n.format("shop.sign.lotto.no_item")).send(related);
                return;
            }
            ItemStack clone = item.getItemStack().clone();
            clone.setAmount(item.getAmount() - item.getSoldAmount());
            TransactionRequest req = new TransactionRequest.TransactionBuilder()
                    .seller(item.getOwner())
                    .buyer(related.getUniqueId())
                    .reason(TaxReason.REASON_LOTTO)
                    .item(item)
                    .priceOverride(price)
                    .taxRate(Tax.getTaxRate(item))
                    .build();
            new BukkitRunnable(){
                @Override
                public void run() {
                    if (TransactionController.getInstance().makeTransaction(req)) {
                        new Message("").append(I18n.format("shop.sign.lotto.success", name, price)).send(related);
                        new Message("").append(I18n.format("shop.sign.lotto.item"), clone).send(related);
                    } else {
                        new Message("").append(I18n.format("shop.sign.lotto.failed"), clone).send(related);
                    }
                }
            }.runTask(HamsterEcoHelper.plugin);
            item.setSold(item.getAmount());
            ShopItemManager.getInstance().updateShopItem(item);
        } catch (NoLottoChestException e){
            new Message(I18n.format("shop.sign.lotto.no_chest", name)).send(related);
        }catch (InvalidItemException e){
            new Message(I18n.format("shop.sign.lotto.invalid_item", name)).send(related);
        }
    }

    @Override
    public LocationType getType() {
        return LocationType.SIGN_SHOP_LOTTO;
    }

    @Override
    public void loadItems() {
        this.items = SignShopConnection.getInstance().getLottoItems(owner);
    }
}

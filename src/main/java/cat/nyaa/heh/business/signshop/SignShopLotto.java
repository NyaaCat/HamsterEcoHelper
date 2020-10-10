package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.business.transaction.TaxReason;
import cat.nyaa.heh.business.transaction.Transaction;
import cat.nyaa.heh.business.transaction.TransactionController;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.DataModel;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        lores.set(1, getTitle());
        lores.set(3, buildPriceLore());
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
    public void doBusiness(Player related, ShopItem item, int amount) {
        //todo
        try {
            Inventory lottoItems = SignShopConnection.getInstance().getLottoItems(owner);
            if(lottoItems == null){
                new Message(I18n.format("sign.error.invalid_sign")).send(related);
            }
            List<Integer> nonNullContents = new ArrayList<>();
            ItemStack[] contents = lottoItems.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack content = contents[i];
                if (content != null && !content.getType().isAir()) {
                    nonNullContents.add(i);
                }
            }

            if (nonNullContents.size() <=0){
                new Message("").append(I18n.format("shop.sign.lotto.no_item")).send(related);
                return;
            }
            Integer integer = Utils.randomSelect(nonNullContents);
            ItemStack item1 = lottoItems.getItem(integer);
            if (item1 == null || item1.getType().isAir()) {
                new Message("").append(I18n.format("shop.sign.lotto.no_item")).send(related);
                return;
            }
            ItemStack clone = item1.clone();
            lottoItems.setItem(integer, new ItemStack(Material.AIR));
            int transacAmount = clone.getAmount();
            ShopItem shopItem = ShopItemManager.newShopItem(owner, ShopItemType.LOTTO, clone, price / (double) transacAmount);
            ShopItemManager.insertShopItem(shopItem);
            TransactionController.getInstance().makeTransaction(related.getUniqueId(), owner, shopItem, transacAmount, 0, TaxReason.REASON_LOTTO);
            new Message("").append(I18n.format("shop.sign.lotto.item"), shopItem.getItemStack()).send(related);
        } catch (NoLottoChestException e) {
            new Message("").append(I18n.format("shop.sign.lotto.no_item")).send(related);
        }
    }

    @Override
    public LocationType getType() {
        return LocationType.SIGN_SHOP_LOTTO;
    }

    @Override
    public void loadItems() {
        SignShopConnection.getInstance().getLottoItems(owner);
    }
}

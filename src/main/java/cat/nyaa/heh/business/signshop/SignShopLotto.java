package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignShopLotto extends BaseSignShop{
    double price;

    public SignShopLotto(UUID owner, double price) {
        super(owner);
        this.price = price;
    }

    public SignShopLotto(LocationDbModel model, double price) {
        super(model);
        this.price = price;
    }

    @Override
    public String getTitle() {
        return "";
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
        Inventory lottoItems = SignShopConnection.getInstance().getLottoItems(owner);
        List<Integer> nonNullContents = new ArrayList<>();
        ItemStack[] contents = lottoItems.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack content = contents[i];
            if (content != null && !content.getType().isAir()) {
                nonNullContents.add(i);
            }
        }

        Integer integer = Utils.randomSelect(nonNullContents);
        ItemStack item1 = lottoItems.getItem(integer);
        if (item1 == null || item1.getType().isAir()){
            throw new InvalidItemException();
        }
        ItemStack clone = item1.clone();
        lottoItems.setItem(integer, new ItemStack(Material.AIR));

        ShopItemManager.newShopItem(owner, ShopItemType.LOTTO, clone, );
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

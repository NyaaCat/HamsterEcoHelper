package cat.nyaa.heh.business.item;

import cat.nyaa.heh.I18n;

public enum ShopItemType {
    MARKET, SIGN_SHOP_SELL, SIGN_SHOP_BUY, AUCTION, REQUISITION, DIRECT,
    STORAGE, LOTTO;

    public String getDescription() {
        return I18n.format("item.type.description."+name());
    }
}

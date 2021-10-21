package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.signshop.BaseSignShop;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.ui.LottoGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.utils.MessagedThrowable;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.logging.Level;

public class LottoComponent extends BasePagedComponent<ShopItem> {
    private final BaseSignShop signShop;

    public LottoComponent(Inventory inventory, BaseSignShop signShop) {
        super(inventory);
        this.signShop = signShop;
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        onClick(event);
    }

    private void onClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player) || !whoClicked.getUniqueId().equals(signShop.getOwner())) {
            return;
        }
        Player player = (Player) whoClicked;
        ShopItem content = getContent(event);
        if (content == null) return;
        retrieveItem(content, player);
        UiManager.getInstance().getLottoGUIs(signShop.getOwner()).forEach(LottoGUI::refreshGUI);
        //        signShop.updateUi();
    }

    private void retrieveItem(ShopItem content, Player player) {
        ItemStack itemStack = content.getItemStack();
        itemStack.setAmount(content.getAmount() - content.getSoldAmount());
        if (giveToPlayer(player, itemStack)) {
            content.setAmount(content.getSoldAmount());
            content.setAvailable(false);
            ShopItemManager.getInstance().updateShopItem(content);
        } else {
            new Message(I18n.format("item.give.failed")).send(player);
        }
    }

    private boolean giveToPlayer(Player player, ItemStack itemStack) {
        if (player.isOnline()) {
            PlayerInventory inventory = player.getInventory();
            if (giveTo(inventory, itemStack)) {
                new Message(I18n.format("item.give.inventory")).send(player);
                return true;
            } else {
                return giveToTempStorage(itemStack, player);
            }
        } else {
            return giveToTempStorage(itemStack, player);
        }
    }

    private boolean giveToTempStorage(ItemStack itemStack, OfflinePlayer offlinePlayer) {
        try {
            StorageConnection.getInstance().getPlayerStorage(offlinePlayer.getUniqueId()).addItem(itemStack, 0, true);
            new Message(I18n.format("item.give.temp_storage")).send(offlinePlayer);
            return true;
        } catch (Exception e) {
            if (e instanceof MessagedThrowable) {
                ((MessagedThrowable) e).getCustomMessage().send(offlinePlayer);
                return false;
            }
            HamsterEcoHelper.plugin.getLogger().log(Level.WARNING, "exception during giving item to temp storage", e);
            return false;
        }
    }

    private boolean giveTo(Inventory inventory, ItemStack itemStack) {
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)) {
            if (InventoryUtils.addItem(inventory, itemStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        onClick(event);
    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {
        onClick(event);
    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {
        onClick(event);
    }

    @Override
    public void onDrag(InventoryClickEvent event) {
        onClick(event);
    }

    @Override
    public void loadData() {
        this.items = SignShopConnection.getInstance().getLottoItems(this.signShop.getOwner());
    }

    @Override
    public void loadData(List<ShopItem> data) {
        this.items = data;
    }
}

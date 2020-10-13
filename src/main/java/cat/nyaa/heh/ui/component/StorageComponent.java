package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemManager;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class StorageComponent extends BasePagedComponent<StorageItem> {
    private UUID owner;

    public StorageComponent(UUID owner, Inventory inventory) {
        super(inventory);
        this.owner = owner;
    }

    @Override
    public void loadData() {
        List<StorageItem> storage = StorageConnection.getInstance().getStorage(owner);
        this.items = storage;
    }

    @Override
    public void loadData(List<StorageItem> data) {
        this.items = data;
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        retrieve(event);
    }

    private void retrieve(InventoryClickEvent event) {
        StorageItem content = getContent(event);
        if (content == null){
            return;
        }
        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)){
            return;
        }
        Player player = (Player) whoClicked;
        double fee = content.getFee();
        Economy eco = EcoUtils.getInstance().getEco();
        if (!eco.has(player, fee)){
            new Message(I18n.format("storage.retrieve.insufficient_funds")).send(player);
            return;
        }
        eco.withdrawPlayer(player, fee);
        addOnCursor(event, content);
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        retrieve(event);
    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {
        retrieve(event);
    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player){
            Player player = (Player) event.getWhoClicked();
            if(!player.isOp()){
                return;
            }
        }
        StorageItem shopItem = getContent(event);
        ItemStack clone = shopItem.getItemStack().clone();
        clone.setAmount(clone.getMaxStackSize());
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().equals(Material.AIR)){
            event.getView().setCursor(clone);
        }else if (cursor.isSimilar(clone)){
            cursor.setAmount(cursor.getMaxStackSize());
            event.getView().setCursor(cursor);
        }
    }

    protected void addOnCursor(InventoryClickEvent event, StorageItem storageItem) {
        ItemStack itemStack = storageItem.getItemStack().clone();
        ItemStack cursor = event.getCursor();

        if (cursor == null || cursor.getType().isAir()){
            event.getView().setCursor(itemStack);
        }else {
            giveTo(event, storageItem);
        }
        StorageConnection.getInstance().removeStorageItem(storageItem);
    }

    private void giveTo(InventoryClickEvent event, StorageItem storageItem) {
        Inventory inventory = event.getWhoClicked().getInventory();
        ItemStack itemStack = storageItem.getItemStack();
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (!InventoryUtils.addItem(inventory, itemStack)){
                HumanEntity whoClicked = event.getWhoClicked();
                whoClicked.getWorld().dropItemNaturally(whoClicked.getLocation(), itemStack);
            }
        }
    }

    @Override
    public void onDrag(InventoryClickEvent event) {

    }
}

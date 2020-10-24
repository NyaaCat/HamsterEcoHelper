package cat.nyaa.heh.business.item;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.db.StorageConnection;
import cat.nyaa.heh.ui.StorageGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerStorage {
    private UUID owner;
    private List<StorageItem> storageItems;

    public PlayerStorage(UUID owner) {
        this.owner = owner;
    }

    Cache<Long, BasicItemMatcher> itemMatcherCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public boolean addItem(ItemStack itemStack, double fee){
        try{
            List<ChangeTask> changed = new ArrayList<>();
            if (storageItems == null){
                loadItems();
            }

            //find item with same fee and merge them.
            List<StorageItem> similarItems = storageItems.stream()
                    .filter(storageItem -> Math.abs(storageItem.getFee() - fee) < 0.001)
                    .filter(storageItem -> {
                        long uid = storageItem.getUid();
                        BasicItemMatcher matcher = itemMatcherCache.getIfPresent(uid);
                        if (matcher == null) {
                            matcher = new BasicItemMatcher();
                            matcher.itemTemplate = itemStack;
                            itemMatcherCache.put(uid, matcher);
                        }
                        return matcher.matches(itemStack);
                    }).collect(Collectors.toList());

            //settle item until all item put into storage;
            int totalAmount = itemStack.getAmount();

            Iterator<StorageItem> iterator = similarItems.iterator();
            while (iterator.hasNext()){
                StorageItem next = iterator.next();
                if (totalAmount<=0) {
                    break;
                }

                //put item into stored item
                ItemStack storedItem = next.getItemStack();
                int maxDraw = storedItem.getMaxStackSize() - storedItem.getAmount();
                int actualDraw = Math.min(totalAmount, maxDraw);
                totalAmount -= actualDraw;

                changed.add(new ChangeTask(next, storedItem.getAmount() + actualDraw));
            }

            //update database
            changed.forEach(changeTask -> {
                ItemStack itemStack1 = changeTask.item.getItemStack();
                itemStack1.setAmount(changeTask.amount);
                changeTask.item.setItemStack(itemStack1);
                StorageConnection.getInstance().updateStorageItem(changeTask.item);
            });

            //remains
            if (totalAmount > 0){
                ItemStack clone = itemStack.clone();
                clone.setAmount(totalAmount);
                StorageItem storageItem = StorageConnection.getInstance().newStorageItem(owner, clone, fee);
                StorageConnection.getInstance().addStorageItem(storageItem);
            }

            this.loadItems();
            UiManager.getInstance().getStorageUis(owner).forEach(StorageGUI::refreshGUI);
            return true;
        } catch (Exception e) {
            HamsterEcoHelper.plugin.getLogger().log(Level.SEVERE, "error while adding storage item", e);
            return false;
        }
    }

    public boolean retrieveItem(StorageItem item, Player player){
        if (!player.isOp() && !player.getUniqueId().equals(owner)){
            return false;
        }
        giveTo(player, item);
        StorageConnection.getInstance().removeStorageItem(item);
        loadItems();
        return true;
    }

    private void giveTo(Player player, StorageItem storageItem) {
        ItemStack itemStack = storageItem.getItemStack();
        PlayerInventory inventory = player.getInventory();
        if (InventoryUtils.hasEnoughSpace(inventory, itemStack)){
            if (!InventoryUtils.addItem(inventory, itemStack)){
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }
        }else {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    public void loadItems() {
        this.storageItems = StorageConnection.getInstance().getStorageItems(owner);
    }

    public List<StorageItem> getItems() {
        return this.storageItems;
    }

    private class ChangeTask{
        StorageItem item;
        int amount;

        public ChangeTask(StorageItem item, int amount) {
            this.item = item;
            this.amount = amount;

        }
    }

}

package cat.nyaa.heh.db;

import cat.nyaa.heh.business.item.PlayerStorage;
import cat.nyaa.heh.business.item.StorageItem;
import cat.nyaa.heh.db.model.StorageDbModel;
import cat.nyaa.heh.utils.UidUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StorageConnection {
    private static StorageConnection INSTANCE;
    private UidUtils uidManager = UidUtils.create("storage");

    private StorageConnection() {
    }

    public static StorageConnection getInstance() {
        if (INSTANCE == null) {
            synchronized (StorageConnection.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StorageConnection();
                }
            }
        }
        return INSTANCE;
    }


    public List<StorageItem> getStorageItems(UUID owner) {
        List<StorageDbModel> storage = DatabaseManager.getInstance().getStorage(owner);
        List<StorageItem> collect = storage.stream().map(storageDbModel -> new StorageItem(storageDbModel))
                .collect(Collectors.toList());
        return collect;
    }

    public StorageItem newStorageItem(UUID buyer, ItemStack itemStack, double fee) {
        long nextUid = uidManager.getNextUid();
        StorageItem storageItem = new StorageItem(buyer, itemStack, fee);
        storageItem.setUid(nextUid);
        return storageItem;
    }

    public void addStorageItem(StorageItem storageItem) {
        DatabaseManager.getInstance().addStorageItem(new StorageDbModel(storageItem));
    }

    public void updateStorageItem(StorageItem storageItem) {
        DatabaseManager.getInstance().updateStorageItem(new StorageDbModel(storageItem));
    }

    public void removeStorageItem(StorageItem storageItem) {
        DatabaseManager.getInstance().removeStorageItem(new StorageDbModel(storageItem));
    }

    Cache<UUID, PlayerStorage> playerStorageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public PlayerStorage getPlayerStorage(UUID pBuyer) {
        PlayerStorage storage = playerStorageCache.getIfPresent(pBuyer);
        if (storage == null){
            storage = new PlayerStorage(pBuyer);
            storage.loadItems();
            playerStorageCache.put(pBuyer, storage);
        }
        return storage;
    }
}

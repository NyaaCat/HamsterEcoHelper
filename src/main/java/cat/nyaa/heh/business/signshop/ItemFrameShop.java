package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.market.Market;
import cat.nyaa.heh.db.LocationConnection;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.DataModel;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Rotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

import static org.bukkit.event.EventPriority.HIGHEST;

public class ItemFrameShop {
    private static int mUserClickInterval = 20;
    private static int mUserClickTimeout = 100;
    private static int mRefreshInterval = 600;
    private long uid;

    private OfflinePlayer owner;
    private ItemFrame frame;
    private BaseShop baseShop;

    private static Map<UUID, ItemFrameShop> frameMap = new HashMap<>();
    private static FrameListener frameListener = new FrameListener();
    private ItemFrameShopData data;
    private ShopItem displayingItem;

    static {
        Bukkit.getPluginManager().registerEvents(frameListener, HamsterEcoHelper.plugin);
    }

    public ItemFrameShop(UUID owner, ItemFrame frame){
        LocationDbModel locationDbModel = LocationConnection.getInstance().newLocationModel(LocationType.FRAME, owner, frame.getLocation());
        locationDbModel.setEntityUUID(frame.getUniqueId());
        data = new ItemFrameShopData();
        locationDbModel.setData(data);
        this.init(locationDbModel);
        setBaseShop(data);
        makeEmpty(frame);
    }

    public ItemFrameShop(OfflinePlayer owner, ItemFrame frame){
       this(owner.getUniqueId(), frame);
    }

    public ItemFrameShop(LocationDbModel shopFrame) {
        this.init(shopFrame);
        makeEmpty(frame);
    }

    public static void newFrameShop(ItemFrameShop itemFrameShop) {
        LocationConnection.getInstance().insertLocationModel(new LocationDbModel(itemFrameShop));
        addFrame(itemFrameShop);
    }

    private static void addFrame(ItemFrameShop itemFrameShop){
        frameMap.put(itemFrameShop.getFrame().getUniqueId(), itemFrameShop);
        refreshItemFrameNow(itemFrameShop.getFrame());
    }

    public static void reloadFrames() {
        frameMap.clear();
        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntities().stream())
                .filter(entity -> entity instanceof ItemFrame)
                .filter(entity -> SignShopConnection.isShopFrame(entity.getUniqueId()))
                .forEach(entity -> addFrame(SignShopConnection.getInstance().getShopFrame(entity.getUniqueId())));
    }


    private void setBaseShop(ItemFrameShopData data) {
        switch (data.backendType){
            case SIGN_SHOP_SELL:
                baseShop = new SignShopSell(owner.getUniqueId());
                break;
            case SIGN_SHOP_BUY:
                baseShop = new SignShopBuy(owner.getUniqueId());
                break;
            case MARKET:
                baseShop = Market.getInstance();
                break;
            case CHEST_BUY:
            case CHEST_LOTTO:
            case FRAME:
                throw new IllegalStateException("item frame shop cannot have " + data.backendType.name() + " backend");
        }
    }

    private void init(LocationDbModel shopFrame) {
        Entity entity = shopFrame.getEntity();
        if (entity instanceof ItemFrame){
            frame = ((ItemFrame) entity);
        }
        this.uid = shopFrame.getUid();
        this.owner = Bukkit.getOfflinePlayer(shopFrame.getOwner());
        this.data = DataModel.getGson().fromJson(shopFrame.getData(), ItemFrameShopData.class);
        setBaseShop(data);
        refreshItemFrameNow(frame);
    }

    private LocationDbModel toModel(){
        return new LocationDbModel(this);
    }

    private Map<UUID, BuyTask> buyTaskMap = new HashMap<>();

    final class BuyTask extends BukkitRunnable{
        private UUID uuid;
        private long startTime;

        BuyTask(UUID uuid){
            this.uuid = uuid;
            startTime = System.currentTimeMillis();
        }

        BuyTask(UUID uuid, long startTime){
            this.uuid = uuid;
            this.startTime = startTime;
        }

        public void resubmit(){
            try{
                cancel();
            }catch (IllegalStateException e){}

            BuyTask task = new BuyTask(uuid, startTime);
            buyTaskMap.put(uuid, task);
            task.runLater();
        }

        private void runLater() {
            this.runTaskLater(HamsterEcoHelper.plugin, mUserClickInterval);
        }

        public boolean isValidClick(){
            long now = System.currentTimeMillis();
            long userClickInterval = (now - startTime) / 50;
            if (userClickInterval < mUserClickInterval){
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            buyTaskMap.remove(uuid);
        }
    }



    public ItemFrameShopData getData() {
        return this.data;
    }
    static class FrameListener implements Listener{

        private static Map<UUID, FrameListener.RefreshTask> refreshTaskMap = new HashMap<>();

        final class RefreshTask extends BukkitRunnable{
            private UUID frameUuid;
            private long lastUpdate;

            RefreshTask(UUID frameUuid) {
                this.frameUuid = frameUuid;
                lastUpdate = System.currentTimeMillis();
            }

            void update(){
                try {
                    cancel();
                }catch (IllegalStateException e){
                }


                RefreshTask task = new RefreshTask(frameUuid);
                refreshTaskMap.put(frameUuid, task);
                task.runNow();
            }

            void runNow(){
                runTask(HamsterEcoHelper.plugin);
            }

            private void runLater() {
                this.runTaskLater(HamsterEcoHelper.plugin, mRefreshInterval);
            }

            @Override
            public void run() {
                ItemFrameShop.refreshItemFrame(frameUuid);
            }

            public void updateLater() {
                try {
                    cancel();
                }catch (IllegalStateException e){
                }

                RefreshTask task = new RefreshTask(frameUuid);
                refreshTaskMap.put(frameUuid, task);
                task.runLater();
            }
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            Arrays.stream(event.getChunk().getEntities())
                    .filter(entity -> entity instanceof ItemFrame)
                    .map(entity -> ((ItemFrame) entity))
                    .filter(this::isShopFrame)
                    .map(iframe -> SignShopConnection.getInstance().getShopFrame(iframe.getUniqueId()))
                    .forEach(ItemFrameShop::addFrame);
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onPlayerInteractItemFrame(PlayerInteractEntityEvent ev) {
            if (!(ev.getRightClicked() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getRightClicked();
            if (!isShopFrame(f))
                return;
            ev.setCancelled(true);

            Player player = ev.getPlayer();
            ItemFrameShop itemFrameShop = frameMap.get(f.getUniqueId());
            BaseShop baseSignShop = itemFrameShop.getBaseShop();

            BuyTask buyTask = itemFrameShop.getBuyTask(player.getUniqueId());
            ItemStack item = f.getItem().clone();
            ShopItem content = ShopItem.getFromSample(item);

            if (!item.getType().isAir() && content == null) {
                new Message("").append(I18n.format("shop.frame.err_not_shop_item"), item).send(player);
                makeEmpty(f);
                refreshItemFrameNow(f);
                return;
            }
            if (item.getType().isAir() || content == null) {
                new Message(I18n.format("shop.frame.info.empty")).send(ev.getPlayer());
                makeEmpty(f);
                refreshItemFrameNow(f);
                return;
            }
            if ( content.getAmount() - content.getSoldAmount() <= 0){
                new Message(I18n.format("shop.frame.info.out_of_stock")).send(ev.getPlayer());
                refreshItemFrameNow(f);
                return;
            }
            if (content != null) {
                item.setAmount(content.getAmount() - content.getSoldAmount());
            }
            if (buyTask == null) {
                new Message(I18n.format("shop.frame.info")).append(item).send(ev.getPlayer());
                UUID playerUuid = player.getUniqueId();
                itemFrameShop.newBuyTask(playerUuid);
                return;
            }
            buyTask.resubmit();

            baseSignShop.doBusiness(player, content, 1);
            itemFrameShop.updateFrameWith(content);
        }

        void resetRefreshTask(ItemFrame f) {
            FrameListener.RefreshTask refreshTask = FrameListener.refreshTaskMap.computeIfAbsent(f.getUniqueId(), RefreshTask::new);
            refreshTask.updateLater();
        }

        void resetRefreshTaskNow(ItemFrame f) {
            FrameListener.RefreshTask refreshTask = FrameListener.refreshTaskMap.computeIfAbsent(f.getUniqueId(), RefreshTask::new);
            refreshTask.update();
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onPlayerHitItemFrame(EntityDamageByEntityEvent ev) {
            if (!(ev.getEntity() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getEntity();
            if (!isShopFrame(f))
                return;
            ev.setCancelled(true);
            if (ev.getDamager() instanceof Player) {
                ev.getDamager().sendMessage(I18n.format("shop.frame.frame_protected"));
            }
        }

        private boolean isShopFrame(ItemFrame f) {
            return frameMap.containsKey(f.getUniqueId());
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onItemFrameBreak(HangingBreakEvent ev) {
            if (!(ev.getEntity() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getEntity();
            if (!isShopFrame(f))return;
            ev.setCancelled(true);

            if (ev.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) { // Explosion protect
                ev.setCancelled(true);
            }
        }
    }

    private void newBuyTask(UUID playerUuid) {
        BuyTask buyTask = new BuyTask(playerUuid);
        buyTaskMap.put(playerUuid, buyTask);
    }

    private BuyTask getBuyTask(UUID uniqueId) {
        return buyTaskMap.get(uniqueId);
    }


    private static void resetRefreshTask(ItemFrame f) {
        frameListener.resetRefreshTask(f);
    }


    private static void refreshItemFrameNow(ItemFrame f) {
        frameListener.resetRefreshTaskNow(f);
    }

    private static void refreshItemFrame(UUID frameUuid) {
        ItemFrameShop ifs = frameMap.get(frameUuid);
        if (ifs == null){
            Bukkit.getLogger().log(Level.WARNING, "uuid " + frameUuid.toString() + " is not a shop frame");
            return;
        }
        new ArrayList<>(ifs.buyTaskMap.keySet()).forEach(uuid -> {
            BuyTask remove = ifs.buyTaskMap.remove(uuid);
            if (remove != null){
                remove.cancel();
            }
        });

        BaseShop signShop = ifs.getBaseShop();
        signShop.loadItems();
        List<ShopItem> items = signShop.getItems();
        ShopItem shopItem = Utils.randomSelect(items);
        if (shopItem == null) {
            ifs.makeEmpty();
            return;
        }
        ifs.updateFrameWith(shopItem);
    }

    private static void makeEmpty(ItemFrame f) {
        ItemStack item1 = f.getItem().clone();
        if (item1.getType().isAir()){
            return;
        }
        f.setItem(new ItemStack(Material.AIR));
        f.setRotation(Rotation.NONE);
//        f.getWorld().dropItemNaturally(f.getLocation(), item1);
    }

    private void makeEmpty() {
        frame.setItem(new ItemStack(Material.AIR));
        frame.setRotation(Rotation.NONE);
    }

    private static boolean isShopFrame(UUID uniqueId) {
        return getShopFrame(uniqueId) != null;
    }

    private static ItemFrameShop getShopFrame(UUID uniqueId) {
        return SignShopConnection.getInstance().getShopFrame(uniqueId);
    }

    private BaseShop getBaseShop() {
        return baseShop;
    }

    public static int getmUserClickInterval() {
        return mUserClickInterval;
    }

    public static void setmUserClickInterval(int mUserClickInterval) {
        ItemFrameShop.mUserClickInterval = mUserClickInterval;
    }

    public static int getmUserClickTimeout() {
        return mUserClickTimeout;
    }

    public static void setmUserClickTimeout(int mUserClickTimeout) {
        ItemFrameShop.mUserClickTimeout = mUserClickTimeout;
    }

    public static int getmRefreshInterval() {
        return mRefreshInterval;
    }

    public static void setmRefreshInterval(int mRefreshInterval) {
        ItemFrameShop.mRefreshInterval = mRefreshInterval;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public ItemFrame getFrame() {
        return frame;
    }

    public void setFrame(ItemFrame frame) {
        this.frame = frame;
    }

    public void setBaseShop(BaseShop baseShop) {
        this.baseShop = baseShop;
    }

    public void updateFrameWith(ShopItem item){
        resetRefreshTask(frame);
        if (item == null){
            frame.setItem(new ItemStack(Material.AIR));
            refreshItemFrameNow(frame);
            displayingItem = null;
            return;
        }
        ItemStack model = item.getModel().clone();
        if (model.getAmount() <= 0 || model.getType().isAir()){
            refreshItemFrameNow(frame);
            displayingItem = null;
            return;
        }
        displayingItem = item;
        model.setAmount(item.getAmount() - item.getSoldAmount());
        frame.setItem(model);
    }
}

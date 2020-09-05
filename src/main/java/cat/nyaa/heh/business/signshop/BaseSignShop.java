package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.DataModel;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class BaseSignShop extends BaseShop{
    protected long uid;
    protected Location location;
    protected Sign sign;
    protected UUID owner;
    protected boolean signExist = false;
    protected List<String> lores = new ArrayList<>();

    public BaseSignShop(UUID owner) {
        this.owner = owner;
    }

    public BaseSignShop(LocationDbModel model){
        this.uid = model.getUid();
        this.owner = model.getOwner();
        String worldName = model.getWorld();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new NullPointerException("world " + worldName +" don't exist");
        }
        Block blockAt = new Location(world, model.getX(), model.getY(), model.getZ()).getBlock();
        BlockState state = blockAt.getState();
        if (!(state instanceof Sign)){
            throw new IllegalStateException(String.format("block at world:%s, x:%f, y:%f, z:%f is not a sign.", worldName, model.getX(), model.getY(), model.getZ()));
        }
        setSign(((Sign) state));
        try{
            SignShopData signShopData = DataModel.getGson().fromJson(model.getData(), SignShopData.class);
            setData(signShopData);
        } catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, String.format("error loading data for locationDbModel %d", uid), e);
            lores = new ArrayList<>();
        }
        loadItems();
    }

    public abstract String getTitle();

    public void setSign(Sign sign) {
        this.sign = sign;
        Block block = sign.getBlock();
        this.location = block.getLocation();
        signExist = true;
    }

    public void setLores(List<String> lores){
        this.lores = lores;
    }

    public List<String> getLores() {
        return lores;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isSignExist(){
        return signExist;
    }

    public LocationDbModel toDbModel(){
        return new LocationDbModel(this);
    }

    public long offer(Player player, ItemStack itemStack, double unitPrice){
        ShopItem shopItem = new ShopItem(player.getUniqueId(), ShopItemType.SIGN_SHOP_BUY, itemStack, unitPrice);
        this.add(shopItem);
        long uid = SignShopConnection.getInstance().addItem(this, shopItem);
        return uid;
    }

    public void retrieve(ShopItem item, int amount){
        if (item.getAmount() - item.getSoldAmount() - amount <= 0){
            item.setSold(item.getAmount());
        }else {
            item.setSold(item.getSoldAmount() + amount);
        }
        SignShopConnection.getInstance().updateItem(item);
        updateUi();
    }

    public void updateUi(){
        UiManager.getInstance().getSignShopUis(getOwner()).stream()
                .forEach(signShopGUI -> signShopGUI.refreshGUI());
    }

    public void updateSign(){
        sign.setLine(0, getTitle());

        int msgSize = lores.size();
        for (int i = 0; i < 3; i++) {
            String line = i >= msgSize ? "" : lores.get(i);
            sign.setLine(i+1, line);
        }
        sign.update();
    }

    public String getLoreString(){
        return DataModel.getGson().toJson(lores);
    }

    public SignShopData getData() {
        return new SignShopData(lores);
    }

    public void setData(SignShopData data){
        this.lores = data.lores;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public abstract SignShopGUI newGUI();
}

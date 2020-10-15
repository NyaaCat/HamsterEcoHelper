package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.business.item.ShopItemType;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.heh.db.model.DataModel;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.ui.SignShopGUI;
import cat.nyaa.heh.ui.UiManager;
import cat.nyaa.heh.utils.SystemAccountUtils;
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
        this.location = new Location(Bukkit.getWorld(worldName), model.getX(), model.getY(), model.getZ());
        try{
            SignShopData signShopData = DataModel.getGson().fromJson(model.getData(), SignShopData.class);
            setData(signShopData);
        } catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, String.format("error loading data for locationDbModel %d", uid), e);
            lores = new ArrayList<>();
        }
    }


    public void loadSign() {
        if (this.location == null){
            return;
        }
        World world = this.location.getWorld();
        if (world == null)return;
        Block block = this.location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Sign) ){
            return;
        }
        setSign(((Sign) state));
        updateSign();
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
        if (sign == null){
            return;
        }
        String name = SystemAccountUtils.isSystemAccount(owner) ? SystemAccountUtils.getSystemName() : Bukkit.getOfflinePlayer(owner).getName();
        sign.setLine(0, getTitle());
        sign.setLine(1, name == null ? "null" : name);
        List<String> lore = this.getLores();
        int msgSize = lore.size();
        for (int i = 0; i < 2; i++) {
            String line = i >= msgSize ? "" : lore.get(i);
            if (line == null || line.trim().equals("")){
                // don't update empty lines.
                continue;
            }
            sign.setLine(i+2, line);
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

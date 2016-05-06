package cat.nyaa.HamsterEcoHelper.market;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MarketItem {
    private int id;
    private String player_uuid;
    private ItemStack itemStack;
    private int amount;
    private double unit_price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayer_uuid() {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid) {
        this.player_uuid = player_uuid;
    }

    public ItemStack getItemStack() {
        itemStack.setAmount(getAmount());
        return itemStack;
    }

    public ItemStack getItemStack(int amount) {
        itemStack.setAmount(amount);
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPlayerName(){
        return Bukkit.getOfflinePlayer(UUID.fromString(getPlayer_uuid())).getName();
    }

    public OfflinePlayer getPlayer(){
        return Bukkit.getOfflinePlayer(UUID.fromString(getPlayer_uuid()));
    }
}



package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;


public class AuctionItemTemplate implements ISerializable {
    @Serializable
    public ItemStack templateItemStack = null;
    @Serializable
    public int baseAuctionPrice = 1000;
    @Serializable
    public int bidStepPrice = 1000;
    @Serializable
    public double randomWeight = 100;
    @Serializable
    public boolean hideName = false;
    @Serializable
    public int waitTimeTicks = 600; // 30 seconds

    @Override
    public void deserialize(ConfigurationSection s) {
        ISerializable.deserialize(s, this);
        if (templateItemStack == null)
            throw new IllegalArgumentException("AuctionItemTemplate gets `null` item");
    }

    @Override
    public void serialize(ConfigurationSection s) {
        ISerializable.serialize(s, this);
    }

    public ItemStack getItemStack() {
        return templateItemStack.clone();
    }
}

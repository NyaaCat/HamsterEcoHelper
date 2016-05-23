package cat.nyaa.HamsterEcoHelper.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import static cat.nyaa.HamsterEcoHelper.Configuration.*;

public class AuctionItemTemplate implements ISerializable {
    @Serializable
    public ItemStack templateItemStack = null;
    @Serializable
    public int baseAuctionPrice = 1000;
    @Serializable
    public int bidStepPrice = 1000;
    @Serializable
    public double randomWeight = 100;

    @Override
    public void loadFrom(ConfigurationSection s) {
        deserialize(s, this);
        if (templateItemStack == null)
            throw new IllegalArgumentException("AuctionItemTemplate gets `null` item");
    }

    @Override
    public void dumpTo(ConfigurationSection s) {
        serialize(s, this);
    }

    public ItemStack getItemStack() {
        return templateItemStack.clone();
    }

    public static AuctionItemTemplate fromConfig(ConfigurationSection c) {
        AuctionItemTemplate tmp = new AuctionItemTemplate();
        tmp.loadFrom(c);
        return tmp;
    }
}

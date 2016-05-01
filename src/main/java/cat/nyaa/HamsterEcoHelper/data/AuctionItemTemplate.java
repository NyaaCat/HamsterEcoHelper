package cat.nyaa.HamsterEcoHelper.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class AuctionItemTemplate implements ISerializable {
    public ItemStack templateItemStack = null;
    public int baseAuctionPrice = 1000;
    public int bidStepPrice = 0;
    public double randomWeight = 100;

    public AuctionItemTemplate() {
    }

    @Override
    public void loadFrom(ConfigurationSection s) {
        templateItemStack = s.getItemStack("templateItemStack");
        baseAuctionPrice = s.getInt("baseAuctionPrice", 1000);
        bidStepPrice = s.getInt("bidStepPrice", 1000);
        randomWeight = s.getDouble("randomWeight", 100);
        if (templateItemStack == null)
            throw new IllegalArgumentException("AuctionItemTemplate gets `null` item");
    }

    @Override
    public void dumpTo(ConfigurationSection s) {
        s.set("templateItemStack", templateItemStack);
        s.set("baseAuctionPrice", baseAuctionPrice);
        s.set("bidStepPrice", bidStepPrice);
        s.set("randomWeight", randomWeight);
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

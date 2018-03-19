package cat.nyaa.HamsterEcoHelper.signshop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    public int amount;
    public ItemStack itemStack;
    public Double unitPrice;

    public ShopItem(ConfigurationSection section) {
        itemStack = section.getItemStack("item");
        amount = section.getInt("amount");
        unitPrice = section.getDouble("unit_price");
    }

    public ShopItem(ItemStack item, int amount, double unitPrice) {
        this.itemStack = item.clone();
        this.itemStack.setAmount(amount);
        this.amount = amount;
        this.unitPrice = unitPrice;
    }

    public void save(ConfigurationSection section) {
        section.set("item", itemStack);
        section.set("amount", amount);
        section.set("unit_price", unitPrice);
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = itemStack.clone();
        item.setAmount(amount);
        return item;
    }
}

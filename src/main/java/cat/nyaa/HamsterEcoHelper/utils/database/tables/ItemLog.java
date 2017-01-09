package cat.nyaa.HamsterEcoHelper.utils.database.tables;

import cat.nyaa.utils.database.DataColumn;
import cat.nyaa.utils.database.DataTable;
import cat.nyaa.utils.database.PrimaryKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.UUID;

@DataTable("itemlog")
public class ItemLog {
    @DataColumn("id")
    @PrimaryKey
    public long id;
    @DataColumn("owner")
    public String owner;
    @DataColumn("item")
    public String item;
    @DataColumn("amount")
    public long amount;
    @DataColumn("price")
    public double price;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getOwner() {
        return UUID.fromString(owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner.toString();
    }

    public ItemStack getItemStack() {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(new String(Base64.getDecoder().decode(item)));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ItemStack itemStack = yaml.getItemStack("item");
        itemStack.setAmount((int) this.amount);
        return itemStack;
    }

    public void setItemStack(ItemStack item) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("item", item);
        this.item = Base64.getEncoder().encodeToString(yaml.saveToString().getBytes());
        amount = item.getAmount();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = getItemStack();
        item.setAmount(amount);
        return item;
    }

    public int getAmount() {
        return (int) amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
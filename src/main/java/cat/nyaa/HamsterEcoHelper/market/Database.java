package cat.nyaa.HamsterEcoHelper.market;


import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public abstract class Database {
    private Plugin plugin;

    public void init(Plugin pl) {
    }

    public int getItemCount() {
        return 0;
    }

    public int getPlayerItemCount(OfflinePlayer player) {
        return 0;
    }

    public void offer(Player player, ItemStack itemStack, double unit_price, int amount) {
        return;
    }

    public void buy(Player player, int itemId, int amount) {
        return;
    }

    public MarketItem getMarketItem(int id) {
        return null;
    }

    public List<MarketItem> getItems(int offset, int limit, String seller) {
        return null;
    }

    public int getPageCount() {
        return (getItemCount() + 45 - 1) / 45;
    }

    public ItemStack[] getMailbox(Player player) {
        return null;
    }

    public boolean setMailbox(Player player, ItemStack[] inventory) {
        return true;
    }

}

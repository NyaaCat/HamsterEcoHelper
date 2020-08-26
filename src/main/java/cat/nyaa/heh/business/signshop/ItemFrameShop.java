package cat.nyaa.heh.business.signshop;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.item.ShopItem;
import cat.nyaa.heh.db.SignShopConnection;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.event.EventPriority.HIGHEST;

public class ItemFrameShop {
    SignShopConnection connection;
    private OfflinePlayer owner;
    private ItemFrame frame;

    private Map<UUID, ItemFrame> frameList = new HashMap<>();
    private static Listener frameListener = new FrameListener();

    static class FrameListener implements Listener{
        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onPlayerInteractItemFrame(PlayerInteractEntityEvent ev) {
            if (!(ev.getRightClicked() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getRightClicked();
            if (!isShopFrame(f))return;
            if (fr.isSet()) {
                new Message(I18n.format("user.exhibition.looking_at")).append(fr.getItemInFrame()).send(ev.getPlayer());
                ev.getPlayer().sendMessage(I18n.format("user.exhibition.provided_by", fr.getOwnerName()));
                for (String line : fr.getDescriptions()) {
                    ev.getPlayer().sendMessage(line);
                }
                ev.setCancelled(true);
                if (fr.hasItem() && fr.getItemInFrame().getType() == Material.WRITTEN_BOOK) {
                    ev.getPlayer().openBook(fr.getItemInFrame());
                }
            }
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onPlayerHitItemFrame(EntityDamageByEntityEvent ev) {
            if (!(ev.getEntity() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getEntity();
            if (!isShopFrame(f))return;
            ev.setCancelled(true);
            if (ev.getDamager() instanceof Player) {
                    ev.getDamager().sendMessage(I18n.format("shop.frame.frame_protected"));
            }
        }

        private boolean isShopFrame(ItemFrame f) {
            return
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onItemFrameBreak(HangingBreakEvent ev) {
            if (!(ev.getEntity() instanceof ItemFrame)) return;
            ItemFrame f = (ItemFrame) ev.getEntity();
            if (!isShopFrame(f))return;

            if (ev.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) { // Explosion protect
                ev.setCancelled(true);
            } else {
                HamsterEcoHelper.plugin.getLogger().warning(String.format("Exhibition broken: Location: %s, item: %s", f.getLocation().toString(),
                        f.getItem().toString()));
                f.setItem(new ItemStack(Material.AIR));
            }
        }
    }

    public ItemFrameShop(OfflinePlayer owner, ItemFrame frame){
        this.frame = frame;
        connection = SignShopConnection.getInstance();
        this.owner = owner;
    }

    public void loadItems(){

    }

    public void updateFrameWith(ShopItem item){
        ItemStack model = item.getModel();
        frame.setItem(model);
    }

    class ItemUpdateTask extends BukkitRunnable {

        @Override
        public void run() {
            frame.setFixed(true);
        }
    }
}

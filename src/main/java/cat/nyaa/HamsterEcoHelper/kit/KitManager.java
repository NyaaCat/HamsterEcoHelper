package cat.nyaa.HamsterEcoHelper.kit;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.database.Kit;
import cat.nyaa.HamsterEcoHelper.database.KitSign;
import cat.nyaa.HamsterEcoHelper.signshop.SignShopManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KitManager {
    public Set<KitSign> kitSigns = new HashSet<>();
    public HashMap<Location, Block> attachedBlocks = new HashMap<>();
    private HamsterEcoHelper plugin;

    public KitManager(HamsterEcoHelper pl) {
        plugin = pl;
        kitSigns.addAll(plugin.database.getAllKitSign());
        updateAttachedBlocks();
    }

    public boolean createKit(String kitName, List<ItemStack> items, boolean removeOld) {
        if (removeOld && getKit(kitName) != null) {
            plugin.database.removeKit(kitName);
        }
        Kit kit = new Kit();
        kit.id = kitName;
        kit.setItems(items);
        return plugin.database.createKit(kit);
    }

    public Kit getKit(String kitName) {
        Kit kit = plugin.database.getKit(kitName);
        if (kit != null) {
            return kit;
        }
        return null;
    }

    public void createKitSign(Block block, String kitName, int price, KitSign.SignType type) {
        KitSign kitSign = new KitSign();
        kitSign.setLocation(block.getLocation());
        kitSign.kitName = kitName;
        kitSign.cost = (double) price;
        kitSign.type = type;
        plugin.database.createKitSign(kitSign);
        kitSigns.add(kitSign);
        attachedBlocks.put(block.getLocation(), SignShopManager.getAttachedBlock(block));
    }

    public void removeKitSign(Block block) {
        KitSign sign = getKitSign(block);
        plugin.database.removeKitSign(sign);
        kitSigns.remove(sign);
        attachedBlocks.remove(block.getLocation());
    }

    public KitSign getKitSign(Block block) {
        String world = block.getWorld().getName();
        for (KitSign s : kitSigns) {
            if (s.world.equalsIgnoreCase(world) && block.getLocation().equals(s.getLocation())) {
                return s;
            }
        }
        return null;
    }

    public void updateAttachedBlocks() {
        if (kitSigns.isEmpty()) {
            return;
        }
        attachedBlocks = new HashMap<>();
        for (KitSign sign : kitSigns) {
            if (sign.getLocation() != null && SignShopManager.isSign(sign.getLocation().getBlock())) {
                attachedBlocks.put(sign.getLocation().clone(), SignShopManager.getAttachedBlock(sign.getLocation().getBlock()));
            }
        }
    }

    public boolean isAttachedBlock(Block block) {
        return attachedBlocks.containsValue(block);
    }
}


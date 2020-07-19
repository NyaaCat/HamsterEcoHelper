package cat.nyaa.heh.ui.component.button.impl;

import cat.nyaa.heh.ui.component.IPagedUiAccess;
import cat.nyaa.heh.ui.component.button.GUIButton;
import cat.nyaa.heh.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonBack extends GUIButton {
    @Serializable
    String title = "&aBack";
    @Serializable
    List<String> lore = new ArrayList<>();
    @Serializable
    Material material = Material.BARRIER;
    {
        lore.add(Utils.colored("&rBack to former level"));
    }

    @Override
    public String getAction() {
        return "back";
    }

    @Override
    public void doAction(InventoryInteractEvent event, IPagedUiAccess iQueryUiAccess) {
        HumanEntity whoClicked = event.getWhoClicked();
        whoClicked.closeInventory();
    }

    @Override
    public ItemStack getModel() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)return itemStack;
        itemMeta.setDisplayName(Utils.colored(title));
        itemMeta.setLore(lore.stream().map(Utils::colored).collect(Collectors.toList()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

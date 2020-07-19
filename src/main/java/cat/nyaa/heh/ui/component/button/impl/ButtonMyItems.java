package cat.nyaa.heh.ui.component.button.impl;

import cat.nyaa.heh.ui.component.IPagedUiAccess;
import cat.nyaa.heh.ui.component.button.GUIButton;
import cat.nyaa.heh.ui.component.impl.MarketComponent;
import cat.nyaa.heh.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonMyItems extends GUIButton {
    @Serializable
    Material material = Material.PAPER;
    @Serializable
    String title = "&emy items";
    @Serializable
    List<String> lore = new ArrayList<>();

    private boolean isFiltered = false;

    {
        lore.add("&eclick to view your items");
    }

    @Override
    public String getAction() {
        return "myItem";
    }

    @Override
    public void doAction(InventoryInteractEvent event, IPagedUiAccess iQueryUiAccess) {
        if (iQueryUiAccess instanceof MarketComponent) {
            MarketComponent marketUi = (MarketComponent) iQueryUiAccess;
            marketUi.setOwnerFilter(event.getWhoClicked().getUniqueId());
        }
    }

    @Override
    public ItemStack getModel() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)return itemStack;
        itemMeta.setDisplayName(Utils.colored(title));
        itemMeta.setLore(getLore());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private List<String> getLore() {
        return lore.stream().map(Utils::colored).collect(Collectors.toList());
    }
}

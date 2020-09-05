package cat.nyaa.heh.ui.component.button.impl;

import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.IPagedUiAccess;
import cat.nyaa.heh.ui.component.button.GUIButton;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonPreviousPage extends GUIButton {
    @Serializable
    String title = "&aPreviousPage";

    @Serializable
    List<String> lore = new ArrayList<>();

    @Serializable
    Material material = Material.ARROW;

    {
        lore.add("click to jump to previous page");
    }

    @Override
    public String getAction() {
        return "previousPage";
    }

    @Override
    public void doAction(InventoryInteractEvent event, BasePagedComponent iQueryUiAccess) {
        int currentPage = iQueryUiAccess.getCurrentPage();
        int totalPages = getTotalPages(iQueryUiAccess);
        int nextPage = currentPage - 1;

        if (currentPage == 0){
            nextPage = totalPages - 1;
        }
        
        if (nextPage < 0 || nextPage >= totalPages){
            nextPage = 0;
        }

        iQueryUiAccess.setPage(nextPage);
        iQueryUiAccess.updateAsynchronously();
    }

    @Override
    public ItemStack getModel() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',title));
        List<String> collect = lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
        itemMeta.setLore(collect);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

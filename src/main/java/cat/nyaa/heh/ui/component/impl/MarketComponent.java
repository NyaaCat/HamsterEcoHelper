package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.item.ShopItem;
import cat.nyaa.heh.item.ShopItemManager;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.button.GUIButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarketComponent extends BasePagedComponent {
    public MarketComponent(Inventory inventory) {
        super(inventory);
    }

    private List<ItemStack> items = new ArrayList<>();

    private List<ItemStack> loadItems() {
        List<ShopItem> marketItems;
        if (ownerFilter == null){
            marketItems = ShopItemManager.getInstance().getMarketItems();
        }else {
            marketItems = ShopItemManager.getInstance().getMarketItems(ownerFilter);
        }
        List<ItemStack> collect = marketItems.stream()
                .map(item -> {
                    ItemStack model = item.getModel();
                    return model;
                }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void refreshUi() {
        List<ItemStack> collect = items.stream().skip(getPageSize() * getCurrentPage())
                .limit(getPageSize()).collect(Collectors.toList());
        int size = collect.size();
        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i < getPageSize(); i++) {
            if (i >= size) {
                setItemAt(i, air);
                continue;
            }
            setItemAt(i, collect.get(i));
        }
    }

    @Override
    public void loadData() {
        items = loadItems();
    }

    @Override
    public void onButtonClicked(GUIButton button, InventoryClickEvent event) {

    }

    @Override
    public Map<String, String> getInfo() {
        Map<String, String> info = super.getInfo();
        return info;
    }

    private UUID ownerFilter = null;

    public void setOwnerFilter(UUID whoClicked) {
        ownerFilter = whoClicked;
    }

    public void removeOwnerFilter(){
        ownerFilter = null;
    }

    public boolean isFiltered() {
        return ownerFilter != null;
    }
}

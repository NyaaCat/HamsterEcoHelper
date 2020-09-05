package cat.nyaa.heh.ui.component.button.impl;

import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.IPagedUiAccess;
import cat.nyaa.heh.ui.component.InfoHolder;
import cat.nyaa.heh.ui.component.button.GUIButton;
import cat.nyaa.heh.utils.Utils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cat.nyaa.heh.utils.Utils.colored;
import static cat.nyaa.heh.utils.Utils.replacePlaceHolder;

public class ButtonInfo extends GUIButton {

    @Serializable
    Material material = Material.DARK_OAK_SIGN;

    @Serializable
    String title = "&aInfo";

    @Serializable(name = "default")
    List<String> default_ = new ArrayList<>();

    private IPagedUiAccess holder;

    {
        default_.add(colored("&eMarket"));
        default_.add(colored("{currentPage} / {totalPage}"));
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public void doAction(InventoryInteractEvent event, BasePagedComponent iQueryUiAccess) {

    }

    @Override
    public void init(IPagedUiAccess iPagedUiAccess) {
        this.holder = iPagedUiAccess;
    }

    @Override
    public ItemStack getModel() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)return itemStack;

        if (holder == null)throw new IllegalStateException("holder not inited");
        Map<String, String> info = holder.getInfo();

        String t = replacePlaceHolder(info, this.title);
        itemMeta.setDisplayName(colored(t));
        itemMeta.setLore(getLore(default_, info));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private List<String> getLore(List<String> lore, Map<String, String> info) {
        return lore.stream().map(s -> {
            s = replacePlaceHolder(info, s);
            return colored(s);
        }).collect(Collectors.toList());
    }

    private String getItemName(ItemStack queryItem) {
        ItemMeta itemMeta = queryItem.getItemMeta();
        String itemName = "";
        if (itemMeta == null){
            itemName = Material.AIR.name();
        }else {
            if (itemMeta.hasDisplayName()){
                itemName = itemMeta.getDisplayName();
            }else if (itemMeta.hasLocalizedName()){
                itemName = itemMeta.getLocalizedName();
            }else {
                itemName = queryItem.getType().name();
            }
        }
        return itemName;
    }
}

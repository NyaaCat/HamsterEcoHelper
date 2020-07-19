package cat.nyaa.heh.ui.component.button;

import cat.nyaa.heh.ui.component.IPagedUiAccess;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GUIButton implements ISerializable, Cloneable{
    ItemStack itemStack;

    public abstract String getAction();

    public abstract void doAction(InventoryInteractEvent event, IPagedUiAccess iQueryUiAccess);

    protected int getTotalPages(IPagedUiAccess iQueryUiAccess){
        int size = iQueryUiAccess.getSize();
        int pageSize = iQueryUiAccess.getPageSize();
        int totalPages = (int) Math.ceil((double)size/(double) Math.max(pageSize, 0.1));
        return totalPages;
    }

    public abstract ItemStack getModel();

    public void init(IPagedUiAccess iPagedUiAccess){}

    @Override
    public GUIButton clone() {
        try {
            return (GUIButton) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

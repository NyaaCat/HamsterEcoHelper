package cat.nyaa.heh.ui;

import cat.nyaa.heh.business.item.ModelableItem;
import cat.nyaa.heh.ui.component.BaseComponent;
import cat.nyaa.heh.ui.component.BasePagedComponent;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.ui.component.button.GUIButton;
import cat.nyaa.heh.ui.component.impl.ButtonComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public abstract class BaseUi<E extends ModelableItem> implements InventoryHolder {
    private UUID uiuid;
    protected Inventory uiInventory;
    protected BasePagedComponent<E> pagedComponent;
    protected ButtonComponent buttonComponent;

    public BaseUi() {
        uiInventory = Bukkit.createInventory(this, 54);
        createComponents();
        this.uiuid = UUID.randomUUID();
    }

    public void createComponents(){
        pagedComponent = newPagedComponent();
        buttonComponent = new ButtonComponent(5, 0, pagedComponent);
        initButtons();
    }

    protected BasePagedComponent<E> getPagedComponent(){
        return pagedComponent == null ? newPagedComponent() : pagedComponent;
    }

    protected abstract BasePagedComponent<E> newPagedComponent();

    private void initButtons() {
        ButtonRegister instance = ButtonRegister.getInstance();
        GUIButton buttonPrevious = instance.PREVIOUS_PAGE.clone();
        GUIButton buttonNextPage = instance.NEXT_PAGE.clone();
        buttonComponent.setButtonAt(buttonComponent.access(0, 0), buttonPrevious);
        buttonComponent.setButtonAt(buttonComponent.access(0, buttonComponent.columns() - 1), buttonNextPage);
    }

    protected abstract String getTitle();

    private static List<InventoryAction> deniedBackpackAction = Arrays.asList(
            InventoryAction.MOVE_TO_OTHER_INVENTORY,
            InventoryAction.UNKNOWN,
            InventoryAction.NOTHING,
            InventoryAction.COLLECT_TO_CURSOR
            );

    public void onClickRawSlot(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (event.getClickedInventory() == null || (!event.getClickedInventory().equals(event.getView().getTopInventory()))){
            if (deniedBackpackAction.contains(event.getAction())){
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        List<? extends BaseComponent> components = Arrays.asList(pagedComponent, buttonComponent);
        BaseComponent comp = components.stream().filter(com -> com.containsRawSlot(slot)).findFirst().orElse(null);

        if (comp == null) return;
        switch (event.getClick()) {
            case LEFT:
                comp.onLeftClick(event);
                break;
            case SHIFT_LEFT:
                comp.onShiftLeftClick(event);
                break;
            case RIGHT:
                comp.onRightClick(event);
                break;
            case MIDDLE:
                comp.onMiddleClick(event);
                break;
            default:
                break;
        }
    }

    public UUID getUid(){
        return uiuid;
    }

    public void onDragRawSlot(InventoryDragEvent event){
        Inventory clickedInventory = event.getInventory();

        Set<Integer> rawSlots = event.getRawSlots();
        int size = event.getView().getTopInventory().getSize();
        if (rawSlots.size() == 1){
            if (rawSlots.iterator().next() >= size){
                return;
            }
        }
        event.setCancelled(true);
        boolean related = rawSlots.stream().mapToInt(Integer::intValue)
                .anyMatch(integer -> integer < size);
        if (!related){
            event.setCancelled(false);
            return;
        }
    }

    protected boolean isContentClicked(int integer){
        return pagedComponent.containsRawSlot(integer) || buttonComponent.containsRawSlot(integer);
    }

    @Override
    public Inventory getInventory() {
        return uiInventory;
    }

    private Player opened;
    public void open(Player player){
        this.opened = player;
        player.openInventory(uiInventory);
    }

    public void close() {
        if (opened != null) {
            if (opened.getOpenInventory().getTopInventory().equals(uiInventory)) {
                opened.closeInventory();
            }
        }
    }

    public abstract void refreshGUI();
    public abstract void refreshGUI(List<E> items);

}

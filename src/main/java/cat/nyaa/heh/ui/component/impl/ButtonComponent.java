package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.business.item.ModelableItem;
import cat.nyaa.heh.ui.component.*;
import cat.nyaa.heh.ui.component.button.ButtonHandler;
import cat.nyaa.heh.ui.component.button.ButtonHolder;
import cat.nyaa.heh.ui.component.button.GUIButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ButtonComponent extends BaseComponent<ModelableItem> implements ButtonHolder, ButtonHandler {
    private Inventory uiInventory;
    private BasePagedComponent basePagedComponent;
    private Map<Integer, GUIButton> buttonMap = new HashMap<>();

    public ButtonComponent(int startRow, int startCol, BasePagedComponent pagedUiAccess) {
        super(startRow, startCol, 1, 9);
        this.uiInventory = pagedUiAccess.getInventory();
        this.basePagedComponent = pagedUiAccess;
    }

    @Override
    public void onButtonClicked(GUIButton button, InventoryClickEvent event) {
        button.doAction(event, basePagedComponent);
    }

    @Override
    public void refreshUi() {
        preUpdate();
        buttonMap.entrySet().stream()
                .forEach(guiButton -> uiInventory.setItem(guiButton.getKey(), guiButton.getValue().getModel()));
    }

    @Override
    public void loadData() {

    }

    @Override
    public void loadData(List<ModelableItem> data) {

    }

    @Override
    public void updateAsynchronously() {

    }

    @Override
    public void preUpdate() {
        ItemStack itemStack = new ItemStack(Material.AIR);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                uiInventory.setItem(access(i, j), itemStack);
            }
        }
    }

    @Override
    public GUIButton getButtonAt(int index) {
        return buttonMap.get(index);
    }

    @Override
    public void setButtonAt(int index, GUIButton button) {
        buttonMap.put(index, button);
    }

    @Override
    public BasePagedComponent getControlled() {
        return basePagedComponent;
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        GUIButton buttonAt = getButtonAt(event.getSlot());
        if (buttonAt == null){
            return;
        }
        onButtonClicked(buttonAt, event);
    }

    @Override
    public void onRightClick(InventoryClickEvent event) {
        onLeftClick(event);
    }

    @Override
    public void onShiftLeftClick(InventoryClickEvent event) {
        onLeftClick(event);
    }

    @Override
    public void onMiddleClick(InventoryClickEvent event) {
        onLeftClick(event);
    }

    @Override
    public void onDrag(InventoryClickEvent event) {
        onLeftClick(event);
    }
}

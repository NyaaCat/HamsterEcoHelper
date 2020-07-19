package cat.nyaa.heh.ui.component.impl;

import cat.nyaa.heh.ui.component.*;
import cat.nyaa.heh.ui.component.button.ButtonHandler;
import cat.nyaa.heh.ui.component.button.ButtonHolder;
import cat.nyaa.heh.ui.component.button.GUIButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;


public class ButtonComponent extends BaseComponent implements ButtonHolder, ButtonHandler {
    private Inventory uiInventory;
    private IPagedUiAccess pagedUiAccess;
    private Map<Integer, GUIButton> buttonMap = new HashMap<>();

    public ButtonComponent(int startRow, int startCol, IPagedUiAccess pagedUiAccess) {
        super(startRow, startCol, 1, 9);
        this.uiInventory = pagedUiAccess.getInventory();
        this.pagedUiAccess = pagedUiAccess;
    }

    @Override
    public void onButtonClicked(GUIButton button, InventoryClickEvent event) {
        button.doAction(event, pagedUiAccess);
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
    public void preUpdate() {
        ItemStack itemStack = new ItemStack(Material.AIR);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                uiInventory.setItem(access(i, j), itemStack);
            }
        }
    }

    @Override
    public void postUpdate() {
        refreshUi();
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
    public IPagedUiAccess getControlled() {
        return pagedUiAccess;
    }

    @Override
    public void onLeftClick(InventoryClickEvent event) {
        GUIButton buttonAt = getButtonAt(event.getSlot());
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

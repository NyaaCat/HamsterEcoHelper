package cat.nyaa.heh.ui;

import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.ui.component.button.GUIButton;
import cat.nyaa.heh.ui.component.impl.ButtonComponent;
import cat.nyaa.heh.ui.component.impl.MarketComponent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseUi implements InventoryHolder {
    protected Inventory uiInventory;
    protected MarketComponent marketComponent;
    protected ButtonComponent buttonComponent;

    public BaseUi() {
        uiInventory = Bukkit.createInventory(this, 54, getTitle());
        marketComponent = new MarketComponent(uiInventory);
        buttonComponent = new ButtonComponent(5, 0, marketComponent);
        initButtons();
    }

    private void initButtons() {
        ButtonRegister instance = ButtonRegister.getInstance();
        GUIButton buttonPrevious = instance.PREVIOUS_PAGE.clone();
        GUIButton buttonNextPage = instance.NEXT_PAGE.clone();
        buttonComponent.setButtonAt(buttonComponent.access(0,0), buttonPrevious);
        buttonComponent.setButtonAt(buttonComponent.access(0,buttonComponent.columns()-1), buttonNextPage);
    }


    protected abstract String getTitle();
}

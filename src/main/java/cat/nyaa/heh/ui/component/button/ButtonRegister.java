package cat.nyaa.heh.ui.component.button;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.ui.component.button.impl.ButtonBack;
import cat.nyaa.heh.ui.component.button.impl.ButtonInfo;
import cat.nyaa.heh.ui.component.button.impl.ButtonNextPage;
import cat.nyaa.heh.ui.component.button.impl.ButtonPreviousPage;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.plugin.java.JavaPlugin;

public class ButtonRegister extends FileConfigure implements ISerializable {
    private static ButtonRegister INSTANCE;
    @Serializable
    public ButtonNextPage NEXT_PAGE = new ButtonNextPage();

    @Serializable
    public ButtonPreviousPage PREVIOUS_PAGE = new ButtonPreviousPage();

    @Serializable
    public ButtonInfo MARKET_INFO = new ButtonInfo();

    @Serializable
    public ButtonInfo SIGNSHOP_INFO = new ButtonInfo();

    @Serializable
    public ButtonBack BACK = new ButtonBack();

    private ButtonRegister(){}

    @Override
    protected String getFileName() {
        return "buttons.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return HamsterEcoHelper.plugin;
    }

    public static ButtonRegister getInstance(){
        if (INSTANCE == null){
            synchronized (ButtonRegister.class){
                if (INSTANCE ==null){
                    INSTANCE = new ButtonRegister();
                }
            }
        }
        return INSTANCE;
    }
}

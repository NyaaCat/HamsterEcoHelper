package cat.nyaa.heh;

import cat.nyaa.heh.item.ShopItemManager;
import cat.nyaa.heh.transaction.TransactionControler;
import cat.nyaa.heh.ui.component.button.ButtonRegister;
import cat.nyaa.heh.utils.EcoUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class HamsterEcoHelper extends JavaPlugin {
    public static HamsterEcoHelper plugin;
    public Configuration config;
    I18n i18n;

    @Override
    public void onEnable() {
        plugin = this;
        reload();
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public void reload() {
        config = new Configuration();
        config.load();
        i18n = new I18n(plugin, config.language);
        i18n.load();
        ShopItemManager.getInstance().loadUid();
        TransactionControler.getInstance().updateUid();
        EcoUtils.getInstance();
        ButtonRegister.getInstance();
    }
}





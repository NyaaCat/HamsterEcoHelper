package cat.nyaa.hamsterecohelper;

import cat.nyaa.hamsterecohelper.item.ShopItemManager;
import cat.nyaa.hamsterecohelper.transaction.TransactionControler;
import cat.nyaa.hamsterecohelper.utils.EcoUtils;
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
    }
}





package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.HamsterEcoHelper;
import org.bukkit.Bukkit;

import java.util.List;

public interface RefreshableUi<T> {
    /**
     * refresh UI without updating data
     * @see this#updateAsynchronously()
     */
    void refreshUi();

    /**
     * load data without refreshing UI
     * @see this#updateAsynchronously()
     */
    void loadData();

    void loadData(List<T> data);

    /**
     * Asynchronously load data and refresh UI
     * if there's thing to do before updating data(like initializing indicator),
     * please override {@link this#preUpdate()}
     * @see this#preUpdate()
     * @see this#postUpdate()
     */
    default void updateAsynchronously(){
        preUpdate();
        Bukkit.getScheduler().runTaskAsynchronously(HamsterEcoHelper.plugin, () -> {
            loadData();
            // call sync
            Bukkit.getScheduler().runTaskLater(HamsterEcoHelper.plugin, this::postUpdate,1);
        });

    }

    default void preUpdate(){}
    default void postUpdate(){refreshUi();}
}

package cat.nyaa.heh.ui.component;

import cat.nyaa.heh.utils.Utils;

public interface RefreshableUi {
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

    /**
     * Asynchronously load data and refresh UI
     * if there's thing to do before updating data(like initializing indicator),
     * please override {@link this#preUpdate()}
     * @see this#preUpdate()
     * @see this#postUpdate()
     */
    default void updateAsynchronously(){
        Utils.newChain()
                .sync(this::preUpdate)
                .async(this::loadData)
                .sync(this::postUpdate)
                .execute();
    }

    default void preUpdate(){}
    default void postUpdate(){refreshUi();}
}

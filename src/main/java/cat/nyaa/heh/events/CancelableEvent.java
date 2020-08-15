package cat.nyaa.heh.events;

import org.bukkit.event.Event;

public abstract class CancelableEvent extends Event {
    private boolean canceled = false;

    public void setCanceled(boolean canceled){
        this.canceled = canceled;
    }

    public boolean isCanceled(){
        return canceled;
    }
}

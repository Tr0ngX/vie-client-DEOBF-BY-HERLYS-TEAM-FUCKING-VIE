package me.drake.viecheats.core.event;

public abstract class CancellableEvent extends Event {
    private boolean cancelled;

    public CancellableEvent() {
        this.cancelled = false;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}

package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;

public class TickEvent extends Event {
    public static final TickEvent PRE;
    public static final TickEvent POST;
    private final TickEvent.Phase phase;

    public TickEvent(TickEvent.Phase phase) {
        this.phase = phase;
    }

    public TickEvent.Phase getPhase() {
        return this.phase;
    }

    static {
        PRE = new TickEvent(TickEvent.Phase.PRE);
        POST = new TickEvent(TickEvent.Phase.POST);
    }


    public enum Phase {
        PRE,
        POST;


        private Phase() {
        }

    }
}

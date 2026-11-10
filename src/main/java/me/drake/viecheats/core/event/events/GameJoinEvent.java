package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;

public class GameJoinEvent extends Event {
    public static final GameJoinEvent INSTANCE;

    static {
        INSTANCE = new GameJoinEvent();
    }

}

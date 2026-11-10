package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;
import net.minecraft.class_437;

public class ScreenChangeEvent extends Event {
    private final class_437 screen;

    public ScreenChangeEvent(class_437 screen) {
        this.screen = screen;
    }

    public class_437 getScreen() {
        return this.screen;
    }

}

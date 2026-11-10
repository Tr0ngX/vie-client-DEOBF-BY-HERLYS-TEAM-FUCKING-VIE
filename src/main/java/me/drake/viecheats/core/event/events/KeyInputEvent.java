package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;
import net.minecraft.class_11908;

public class KeyInputEvent extends Event {
    private final class_11908 input;
    private final int action;

    public KeyInputEvent(class_11908 input, int action) {
        this.input = input;
        this.action = action;
    }

    public class_11908 getInput() {
        return this.input;
    }

    public int getAction() {
        return this.action;
    }

}

package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;
import net.minecraft.class_4587;

public class Render3DEvent extends Event {
    private final float tickDelta;
    private final class_4587 matrices;

    public Render3DEvent() {
        this.tickDelta = 1.0f;
        this.matrices = new class_4587();
    }

    public Render3DEvent(float tickDelta) {
        this.tickDelta = tickDelta;
        this.matrices = new class_4587();
    }

    public Render3DEvent(float tickDelta, class_4587 matrices) {
        this.tickDelta = tickDelta;
        this.matrices = matrices;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public class_4587 getMatrices() {
        return this.matrices;
    }

}

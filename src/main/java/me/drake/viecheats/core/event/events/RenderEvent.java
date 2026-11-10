package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;
import net.minecraft.class_332;

public class RenderEvent extends Event {
    private final RenderEvent.Stage stage;
    private final float tickDelta;
    private class_332 drawContext;

    public RenderEvent(RenderEvent.Stage stage, float tickDelta) {
        this.stage = stage;
        this.tickDelta = tickDelta;
    }

    public RenderEvent.Stage getStage() {
        return this.stage;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public class_332 getDrawContext() {
        return this.drawContext;
    }

    public void setDrawContext(class_332 ctx) {
        this.drawContext = ctx;
    }


    public enum Stage {
        HUD_2D,
        WORLD_3D;


        private Stage() {
        }

    }

    public enum Type {
        HUD,
        WORLD;


        private Type() {
        }

    }
}

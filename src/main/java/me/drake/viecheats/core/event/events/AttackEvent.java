package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.CancellableEvent;
import net.minecraft.class_1297;

public class AttackEvent extends CancellableEvent {
    private final class_1297 target;

    public AttackEvent(class_1297 target) {
        this.target = target;
    }

    public class_1297 getTarget() {
        return this.target;
    }

}

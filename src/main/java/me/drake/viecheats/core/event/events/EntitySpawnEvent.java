package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.CancellableEvent;
import net.minecraft.class_1297;
import net.minecraft.class_1299;

public class EntitySpawnEvent extends CancellableEvent {
    private final class_1297 entity;

    public EntitySpawnEvent(class_1297 entity) {
        this.entity = entity;
    }

    public class_1297 getEntity() {
        return this.entity;
    }

    public class_1299 getEntityType() {
        return this.entity.method_5864();
    }

}

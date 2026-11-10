package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;
import net.minecraft.class_1923;

public class ChunkLoadEvent extends Event {
    private final class_1923 chunkPos;
    private final boolean loaded;

    public ChunkLoadEvent(class_1923 chunkPos, boolean loaded) {
        this.chunkPos = chunkPos;
        this.loaded = loaded;
    }

    public class_1923 getChunkPos() {
        return this.chunkPos;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isUnloaded() {
        return !this.loaded;
    }

}

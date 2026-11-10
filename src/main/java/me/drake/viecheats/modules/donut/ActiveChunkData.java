package me.drake.viecheats.modules.donut;

import net.minecraft.class_1923;

public record ActiveChunkData(class_1923 chunkPos, long lastActivityMs, int totalActivity) {

    public ActiveChunkData(class_1923 chunkPos, long lastActivityMs, int totalActivity) {
        this.chunkPos = chunkPos;
        this.lastActivityMs = lastActivityMs;
        this.totalActivity = totalActivity;
    }

    public class_1923 chunkPos() {
        return this.chunkPos;
    }

    public long lastActivityMs() {
        return this.lastActivityMs;
    }

    public int totalActivity() {
        return this.totalActivity;
    }

}

package me.drake.viecheats.modules.donut;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1923;
import net.minecraft.class_2338;

public final class ActiveChunkActivityStore {
    private static final Map ACTIVITY;
    private static volatile long currentPlayerChunkKey;

    public static synchronized void setPlayerChunk(long chunkKey) {
        currentPlayerChunkKey = chunkKey;
    }

    public static synchronized void recordBlockUpdate(class_2338 pos) {
        if (pos == null) return;
        ((ActiveChunkActivityStore.ActivityEntry) ACTIVITY.computeIfAbsent(chunkKey(pos), (k) -> new ActiveChunkActivityStore.ActivityEntry())).record();
    }

    public static synchronized void recordScheduledTick(class_2338 pos) {
        if (pos == null) return;
        ((ActiveChunkActivityStore.ActivityEntry) ACTIVITY.computeIfAbsent(chunkKey(pos), (k) -> new ActiveChunkActivityStore.ActivityEntry())).record();
    }

    public static synchronized void recordLightingUpdate(int chunkX, int chunkZ) {
        ((ActiveChunkActivityStore.ActivityEntry) ACTIVITY.computeIfAbsent(class_1923.method_8331(chunkX, chunkZ), (k) -> new ActiveChunkActivityStore.ActivityEntry())).record();
    }

    public static synchronized void recordBlockEntityTick(class_2338 pos) {
        if (pos == null) return;
        ((ActiveChunkActivityStore.ActivityEntry) ACTIVITY.computeIfAbsent(chunkKey(pos), (k) -> new ActiveChunkActivityStore.ActivityEntry())).record();
    }

    public static synchronized void recordEntityTick(int chunkX, int chunkZ) {
        ((ActiveChunkActivityStore.ActivityEntry) ACTIVITY.computeIfAbsent(class_1923.method_8331(chunkX, chunkZ), (k) -> new ActiveChunkActivityStore.ActivityEntry())).record();
    }

    public static synchronized Map drain() {
        if (ACTIVITY.isEmpty()) return Collections.emptyMap();
        if (currentPlayerChunkKey == -9223372036854775808L) {
            Map result = new HashMap(ACTIVITY);
            ACTIVITY.clear();
            return result;
        }
        result = new HashMap(ACTIVITY.size());
        Object var1 = ACTIVITY.entrySet().iterator();
        while (var1.hasNext()) {
            Map.Entry e = ((Map.Entry) var1.next());
            if (((Long) e.getKey()).longValue() != currentPlayerChunkKey) {
                result.put(((Long) e.getKey()), ((ActiveChunkActivityStore.ActivityEntry) e.getValue()));
            }
        }
        ACTIVITY.clear();
        return result;
    }

    public static synchronized void clear() {
        ACTIVITY.clear();
    }

    private static long chunkKey(class_2338 pos) {
        return class_1923.method_8331((pos.method_10263() >> 4), (pos.method_10260() >> 4));
    }

    static {
        ACTIVITY = new HashMap();
        currentPlayerChunkKey = -9223372036854775808L;
    }


    public final class ActivityEntry {
        private int total;

        public void record() {
            this.total = (this.total + 1);
        }

        public int total() {
            return this.total;
        }

    }
}

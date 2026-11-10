package me.drake.viecheats.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2338;

public final class SpawnerEntityCache {
    private static final Map CACHE;

    public static void put(class_2338 pos, String entityId) {
        if (entityId != null && !(entityId.isEmpty())) {
            CACHE.put(pos.method_10063(), entityId);
        }
    }

    public static String get(class_2338 pos) {
        return ((String) CACHE.get(pos.method_10063()));
    }

    public static void remove(class_2338 pos) {
        CACHE.remove(pos.method_10063());
    }

    public static void clear() {
        CACHE.clear();
    }

    static {
        CACHE = new ConcurrentHashMap();
    }

}

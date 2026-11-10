package me.drake.viecheats.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import org.joml.Matrix4f;

public final class EntityCache {
    private static final double MAX_TRACK_RANGE = 192.0;
    private static final double MAX_TRACK_RANGE_SQ = 36864.0;
    private static volatile boolean active;
    private static final Int2ObjectOpenHashMap cache;
    private static volatile List snapshot;
    private static int lastUpdateTick;
    private static final IntOpenHashSet staleIdsScratch;
    private static class_4604 frustum;

    public static void setActive(boolean enabled) {
        boolean wasActive = active;
        active = enabled;
        if (!(!wasActive) && !enabled) {
            clear();
            frustum = null;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static void updateFrustum(Matrix4f viewMatrix, Matrix4f projectionMatrix, class_4184 camera) {
        if (!(!active)) {
            if (camera == null) {
            }
            frustum = null;
            return;
        }
        frustum = new class_4604(viewMatrix, projectionMatrix);
        class_243 camPos = camera.method_71156();
        frustum.method_23088(camPos.field_1352, camPos.field_1351, camPos.field_1350);
    }

    public static boolean isVisible(class_238 box) {
        if (frustum != null) {
            if (frustum.method_23093(box)) {
            }
        } else {
        }
        return false;
    }

    public static void update() {
        if (!active) return;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) break label_3;
        if (mc.field_1687 == null) {
            label_3:
            cache.clear();
            snapshot = Collections.emptyList();
            return;
        }
        int currentTick = mc.field_1724.field_6012;
        switch (EntityCache.1.$SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.tier().ordinal()]) {
            case 1 -> { break label_9; }
            case 2 -> { break label_9; }
            case 3 -> { break label_10; }
            case 4 -> { break label_11; }
            case 5 -> { break label_12; }
            default -> { break label_8; }
        }
        label_8:
        throw new MatchException(null, null);
        label_9:
        break label_13;
        label_10:
        break label_13;
        label_11:
        break label_13;
        label_12:
        label_13:
        int intervalTicks = 4;
        if (lastUpdateTick >= 0 && (currentTick - lastUpdateTick) < intervalTicks) return;
        lastUpdateTick = currentTick;
        double px = mc.field_1724.method_23317();
        double py = mc.field_1724.method_23318();
        double pz = mc.field_1724.method_23321();
        staleIdsScratch.clear();
        staleIdsScratch.addAll(cache.keySet());
        List sorted = mc.field_1687.method_18112().iterator();
        while (sorted.hasNext()) {
            class_1297 entity = ((class_1297) sorted.next());
            if (entity == mc.field_1724) {
                break label_22;
            }
            if (!((entity instanceof class_1309))) break label_22;
            class_1309 living = ((class_1309) entity);
            if (!(living.method_5805())) {
                break label_22;
            }
            int id = entity.method_5628();
            double ex = entity.method_23317();
            double ey = entity.method_23318();
            double ez = entity.method_23321();
            double dx = (ex - px);
            double dy = (ey - py);
            double dz = (ez - pz);
            double currentDistSq = (((dx * dx) + (dy * dy)) + (dz * dz));
            if (currentDistSq > 36864.0) {
                break label_22;
            }
            staleIdsScratch.remove(id);
            EntityCache.CachedEntity existing = ((EntityCache.CachedEntity) cache.get(id));
            if (existing != null && existing.lastAge == entity.field_6012) {
                break label_22;
            }
            double distSq = currentDistSq;
            cache.put(id, new EntityCache.CachedEntity(id, entity, entity.method_5829(), distSq, entity.field_6012));
        }
        sorted = staleIdsScratch.iterator();
        while (sorted.hasNext()) {
            int staleId = ((Integer) sorted.next()).intValue();
            cache.remove(staleId);
        }
        if (cache.isEmpty()) {
            snapshot = Collections.emptyList();
            break label_55;
        }
        if (cache.size() == 1) {
            snapshot = Collections.singletonList(((EntityCache.CachedEntity) cache.values().iterator().next()));
            break label_55;
        }
        sorted = new ArrayList(cache.values());
        sorted.sort((a, b) -> Double.compare(a.distanceSq, b.distanceSq));
        snapshot = Collections.unmodifiableList(sorted);
        label_55:
    }

    public static List getEntities() {
        if (!active) return Collections.emptyList();
        return snapshot;
    }

    public static List getEntitiesInRange(double range, boolean cullFrustum) {
        if (!active) return Collections.emptyList();
        double rangeSq = (range * range);
        List result = new ArrayList();
        Object var6 = snapshot.iterator();
        while (var6.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var6.next());
            if (ce.distanceSq > rangeSq) {
                break label_9;
            }
            if (!(!cullFrustum) && !(isVisible(ce.boundingBox))) {
                break label_4;
            }
            result.add(ce);
        }
        label_9:
        return result;
    }

    public static EntityCache.CachedEntity getById(int entityId) {
        return ((EntityCache.CachedEntity) cache.get(entityId));
    }

    public static void clear() {
        cache.clear();
        snapshot = Collections.emptyList();
        lastUpdateTick = -1;
    }

    public static float fastSin(float x) {
        return class_3532.method_15374(((double) x));
    }

    public static float fastCos(float x) {
        return class_3532.method_15362(((double) x));
    }

    static {
        active = false;
        cache = new Int2ObjectOpenHashMap();
        snapshot = Collections.emptyList();
        lastUpdateTick = -1;
        staleIdsScratch = new IntOpenHashSet();
    }


    class 1 {

        static {
            $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier = new int[PerformanceHelper.Tier.values().length];
            try {
                $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.Tier.ULTRA.ordinal()] = 1;
            } catch (NoSuchFieldError ex3) {
                Object var0 = ex3;
                try {
                }
                $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.Tier.HIGH.ordinal()] = 2;
            } catch (NoSuchFieldError ex6) {
                var0 = ex6;
                try {
                }
                $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.Tier.MEDIUM.ordinal()] = 3;
            } catch (NoSuchFieldError ex9) {
                var0 = ex9;
                try {
                }
                $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.Tier.LOW.ordinal()] = 4;
            } catch (NoSuchFieldError ex12) {
                var0 = ex12;
                try {
                }
                $SwitchMap$me$drake$viecheats$util$PerformanceHelper$Tier[PerformanceHelper.Tier.CRITICAL.ordinal()] = 5;
            } catch (NoSuchFieldError ex15) {
                var0 = ex15;
            }
        }

    }

    public record CachedEntity(int id, class_1297 entity, class_238 boundingBox, double distanceSq, int lastAge) {

        public CachedEntity(int id, class_1297 entity, class_238 boundingBox, double distanceSq, int lastAge) {
            this.id = id;
            this.entity = entity;
            this.boundingBox = boundingBox;
            this.distanceSq = distanceSq;
            this.lastAge = lastAge;
        }

        public int id() {
            return this.id;
        }

        public class_1297 entity() {
            return this.entity;
        }

        public class_238 boundingBox() {
            return this.boundingBox;
        }

        public double distanceSq() {
            return this.distanceSq;
        }

        public int lastAge() {
            return this.lastAge;
        }

    }
}

package me.drake.viecheats.util;

import net.minecraft.class_310;

public final class PerformanceHelper {
    private static final long SAMPLE_INTERVAL_MS = 250L;
    private static long lastSampleAtMs;
    private static int cachedFps;
    private static PerformanceHelper.Tier cachedTier;

    public static PerformanceHelper.Tier tier() {
        sampleIfNeeded();
        return cachedTier;
    }

    public static int fps() {
        sampleIfNeeded();
        return cachedFps;
    }

    public static int scaleBudget(int base) {
        if (base <= 1) return 1;
        switch (tier().ordinal()) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            default -> { break label_2; }
        }
        label_2:
        throw new MatchException(null, null);
        label_3:
        break label_8;
        label_4:
        break label_8;
        label_5:
        break label_8;
        label_6:
        break label_8;
        label_7:
        label_8:
        return Math.max(1, ((int) Math.floor((((double) base) * 0.4))));
    }

    public static int scaleRenderCap(int base) {
        if (base <= 1) return 1;
        switch (tier().ordinal()) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            default -> { break label_2; }
        }
        label_2:
        throw new MatchException(null, null);
        label_3:
        break label_8;
        label_4:
        break label_8;
        label_5:
        break label_8;
        label_6:
        break label_8;
        label_7:
        label_8:
        return Math.max(1, ((int) Math.floor((((double) base) * 0.45))));
    }

    public static int scaleInterval(int baseTicks) {
        int base = Math.max(1, baseTicks);
        switch (tier().ordinal()) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            default -> { break label_2; }
        }
        label_2:
        throw new MatchException(null, null);
        label_3:
        break label_8;
        label_4:
        break label_8;
        label_5:
        break label_8;
        label_6:
        break label_8;
        label_7:
        label_8:
        return (base + 6);
    }

    public static double scaleRange(double baseRange) {
        double base = Math.max(1.0, baseRange);
        switch (tier().ordinal()) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            default -> { break label_2; }
        }
        label_2:
        throw new MatchException(null, null);
        label_3:
        break label_8;
        label_4:
        break label_8;
        label_5:
        break label_8;
        label_6:
        break label_8;
        label_7:
        label_8:
        return (base * 0.66);
    }

    public static boolean shouldRenderWorld3DThisFrame() {
        return true;
    }

    private static void sampleIfNeeded() {
        long now = System.currentTimeMillis();
        if ((now - lastSampleAtMs) < 250L) return;
        lastSampleAtMs = now;
        class_310 mc = class_310.method_1551();
        if (mc == null) return;
        int fps = mc.method_47599();
        if (fps <= 0) {
            fps = cachedFps;
        }
        cachedFps = Math.max(1, fps);
        if (cachedFps >= 200) {
            cachedTier = PerformanceHelper.Tier.ULTRA;
            break label_13;
        }
        if (cachedFps >= 150) {
            cachedTier = PerformanceHelper.Tier.HIGH;
            break label_13;
        }
        if (cachedFps >= 110) {
            cachedTier = PerformanceHelper.Tier.MEDIUM;
            break label_13;
        }
        if (cachedFps >= 75) {
            cachedTier = PerformanceHelper.Tier.LOW;
            break label_13;
        }
        cachedTier = PerformanceHelper.Tier.CRITICAL;
        label_13:
    }

    static {
        lastSampleAtMs = 0L;
        cachedFps = 120;
        cachedTier = PerformanceHelper.Tier.ULTRA;
    }


    public enum Tier {
        ULTRA,
        HIGH,
        MEDIUM,
        LOW,
        CRITICAL;


        private Tier() {
        }

    }
}

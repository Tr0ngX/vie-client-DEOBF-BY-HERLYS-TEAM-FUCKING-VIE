package me.drake.viecheats.chunkfinder;

import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2680;

public final class SeedSimulationEngine {
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 150;
    private final long seed;

    public SeedSimulationEngine(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return this.seed;
    }

    public class_2248 expectedBaseStoneAt(int x, int y, int z) {
        if (y >= 0) {
            if (y > 150) {
            }
            return class_2246.field_10340;
        }
        double deep = fractalNoise((this.seed ^ -7046029254386353131L), x, y, z, 32, 3);
        double detail = (fractalNoise((this.seed ^ -2960836687051489901L), x, y, z, 14, 2) - 0.5);
        double selector = ((deep + (detail * 0.35)) + ((((double) y) - 24.0) * 0.0022));
        if (y <= 34) {
            double tuffNoise = fractalNoise((this.seed ^ -6752110988234923001L), x, y, z, 20, 2);
            if (tuffNoise > (0.8 + (((double) y) * 0.0016))) return class_2246.field_27165;
        }
        if (selector < 0.22) return class_2246.field_10340;
        if (selector < 0.42) return class_2246.field_10474;
        if (selector < 0.6) return class_2246.field_10508;
        if (selector < 0.79) return class_2246.field_10115;
        return class_2246.field_10340;
    }

    public class_2248 expectedLayerBlockAt(int x, int y, int z) {
        class_2248 base = expectedBaseStoneAt(x, y, z);
        if ((y < 0) || (y > 150)) {
            return base;
        }
        if (isExpectedDirtPocket(x, y, z, base)) return class_2246.field_10566;
        return base;
    }

    public class_2248 expectedSecondaryStoneAt(int x, int y, int z) {
        class_2248 base = expectedBaseStoneAt(x, y, z);
        if ((((base == class_2246.field_10474) || (base == class_2246.field_10508)) || (base == class_2246.field_10115)) || (base == class_2246.field_27165)) {
            return base;
        }
        return null;
    }

    public class_2680 expectedStateAt(int x, int y, int z) {
        return expectedLayerBlockAt(x, y, z).method_9564();
    }

    public class_2680 sampleWithVanillaNoiseTemplate(int x, int y, int z) {
        return expectedStateAt(x, y, z);
    }

    private boolean isExpectedDirtPocket(int x, int y, int z, class_2248 base) {
        if ((y < 24) || (y > 150)) {
            return false;
        }
        if (base == class_2246.field_27165) return false;
        double pocket = fractalNoise((this.seed ^ 2611923443488327891L), x, y, z, 12, 2);
        double spread = fractalNoise((this.seed ^ 1376283091369227076L), x, y, z, 26, 2);
        double threshold = (0.94 - (((double) (y - 24)) * 7.0E-4));
        if (pocket <= threshold) break label_7;
        return spread > 0.58;
    }

    private static double fractalNoise(long localSeed, int x, int y, int z, int baseScale, int octaves) {
        double value = 0.0;
        double weightSum = 0.0;
        double amp = 1.0;
        int scale = Math.max(2, baseScale);
        int i = 0;
        while (i < octaves) {
            value = (value + (smoothValueNoise((localSeed + (((long) i) * -7046029254386353131L)), x, y, z, scale) * amp));
            weightSum = (weightSum + amp);
            amp = (amp * 0.5);
            scale = Math.max(2, (scale / 2));
            i++;
        }
        if (weightSum <= 0.0) return 0.0;
        return (value / weightSum);
    }

    private static double smoothValueNoise(long localSeed, int x, int y, int z, int scale) {
        int x0 = Math.floorDiv(x, scale);
        int y0 = Math.floorDiv(y, scale);
        int z0 = Math.floorDiv(z, scale);
        int x1 = (x0 + 1);
        int y1 = (y0 + 1);
        int z1 = (z0 + 1);
        double tx = smoothStep((((double) (x - (x0 * scale))) / ((double) scale)));
        double ty = smoothStep((((double) (y - (y0 * scale))) / ((double) scale)));
        double tz = smoothStep((((double) (z - (z0 * scale))) / ((double) scale)));
        double v000 = hashToUnit(localSeed, x0, y0, z0);
        double v100 = hashToUnit(localSeed, x1, y0, z0);
        double v010 = hashToUnit(localSeed, x0, y1, z0);
        double v110 = hashToUnit(localSeed, x1, y1, z0);
        double v001 = hashToUnit(localSeed, x0, y0, z1);
        double v101 = hashToUnit(localSeed, x1, y0, z1);
        double v011 = hashToUnit(localSeed, x0, y1, z1);
        double v111 = hashToUnit(localSeed, x1, y1, z1);
        double x00 = lerp(v000, v100, tx);
        double x10 = lerp(v010, v110, tx);
        double x01 = lerp(v001, v101, tx);
        double x11 = lerp(v011, v111, tx);
        double y0v = lerp(x00, x10, ty);
        double y1v = lerp(x01, x11, ty);
        return lerp(y0v, y1v, tz);
    }

    private static double hashToUnit(long localSeed, int x, int y, int z) {
        long h = localSeed;
        h = (h ^ mix64((((long) x) * -7046029254386353131L)));
        h = (h ^ mix64((((long) y) * -4417276706812531889L)));
        h = (h ^ mix64((((long) z) * 1609587929392839161L)));
        h = mix64(h);
        long bits = ((h >>> 11) & 9007199254740991L);
        return (((double) bits) * 1.1102230246251565E-16);
    }

    private static long mix64(long z) {
        z = ((z ^ (z >>> 30)) * -4658895280553007687L);
        z = ((z ^ (z >>> 27)) * -7723592293110705685L);
        return (z ^ (z >>> 31));
    }

    private static double smoothStep(double t) {
        double clamped = clamp01(t);
        return ((clamped * clamped) * (3.0 - (2.0 * clamped)));
    }

    private static double clamp01(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }

    private static double lerp(double a, double b, double t) {
        return (a + ((b - a) * t));
    }

}

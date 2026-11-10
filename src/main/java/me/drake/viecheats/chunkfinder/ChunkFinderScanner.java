package me.drake.viecheats.chunkfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChunkFinderScanner {
    private static final Logger LOGGER;
    private static final long WORLD_SEED = 6608149111735331168L;
    private static final int MAX_RESULTS = 4096;
    private static final int MIN_SCAN_Y = 0;
    private static final int MAX_SCAN_Y = 64;
    private static final int DEEP_UNDERGROUND_Y = 40;
    private static final int DEFAULT_MIN_SEED_SAMPLES_PER_CHUNK = 96;
    private static final double DEFAULT_MAX_SEED_MISMATCH_RATIO = 0.88;
    private static final int DEFAULT_MIN_FLAGS_PER_CHUNK = 1;
    private static final int DEFAULT_MAX_FLAGS_PER_CHUNK = 48;
    private static final int DEFAULT_HOST_SEED_SUPPORT = 2;
    private static final Set NATURAL_HOSTS;
    private static final Set CENTER_FILL_CANDIDATES;
    private final ExecutorService scanExecutor;
    private final AtomicBoolean scanInFlight;
    private final AtomicReference findings;
    private volatile long lastScanAtMs;
    private volatile int minSeedSamplesPerChunk;
    private volatile double maxSeedMismatchRatio;
    private volatile int minFlagsPerChunk;
    private volatile int maxFlagsPerChunk;
    private volatile int hostSeedSupportRequired;

    public ChunkFinderScanner() {
        this.scanExecutor = Executors.newSingleThreadExecutor((r) -> {
    Thread t = new Thread(r, "viecheats-chunkfinder-scan");
    t.setDaemon(true);
    return t;
});
        this.scanInFlight = new AtomicBoolean(false);
        this.findings = new AtomicReference(ChunkFinderScanner.ScanSnapshot.EMPTY);
        this.lastScanAtMs = 0L;
        this.minSeedSamplesPerChunk = 96;
        this.maxSeedMismatchRatio = 0.88;
        this.minFlagsPerChunk = 1;
        this.maxFlagsPerChunk = 48;
        this.hostSeedSupportRequired = 2;
    }

    public List getFindingsSnapshot() {
        return ((ChunkFinderScanner.ScanSnapshot) this.findings.get()).blocks();
    }

    public Set getCoreChunksSnapshot() {
        return ((ChunkFinderScanner.ScanSnapshot) this.findings.get()).coreChunks();
    }

    public Set getFlaggedChunksSnapshot() {
        return ((ChunkFinderScanner.ScanSnapshot) this.findings.get()).flaggedChunks();
    }

    public boolean isScanning() {
        return this.scanInFlight.get();
    }

    public void resetCooldown() {
        this.lastScanAtMs = 0L;
    }

    public void setMinSeedSamplesPerChunk(int value) {
        this.minSeedSamplesPerChunk = Math.max(24, Math.min(512, value));
    }

    public void setMaxSeedMismatchRatio(double value) {
        this.maxSeedMismatchRatio = Math.max(0.35, Math.min(0.98, value));
    }

    public void setMinFlagsPerChunk(int value) {
        this.minFlagsPerChunk = Math.max(1, Math.min(12, value));
    }

    public void setMaxFlagsPerChunk(int value) {
        this.maxFlagsPerChunk = Math.max(8, Math.min(128, value));
    }

    public void setHostSeedSupportRequired(int value) {
        this.hostSeedSupportRequired = Math.max(1, Math.min(5, value));
    }

    public void tick(class_310 client, int configuredRadiusChunks, long scanIntervalMs) {
        if (((client == null) || (client.field_1687 == null)) || (client.field_1724 == null)) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - this.lastScanAtMs) < scanIntervalMs) return;
        this.lastScanAtMs = now;
        scheduleScan(client, configuredRadiusChunks);
    }

    public void triggerImmediateScan(class_310 client, int configuredRadiusChunks) {
        if (((client == null) || (client.field_1687 == null)) || (client.field_1724 == null)) {
            return;
        }
        this.lastScanAtMs = System.currentTimeMillis();
        scheduleScan(client, configuredRadiusChunks);
    }

    public void shutdown() {
        this.scanExecutor.shutdownNow();
        this.findings.set(ChunkFinderScanner.ScanSnapshot.EMPTY);
        this.scanInFlight.set(false);
    }

    private void scheduleScan(class_310 client, int configuredRadiusChunks) {
        if (!(this.scanInFlight.compareAndSet(false, true))) return;
        ChunkFinderScanner.ScanInput input = captureInput(client, configuredRadiusChunks);
        if (input.chunks().isEmpty()) {
            this.findings.set(ChunkFinderScanner.ScanSnapshot.EMPTY);
            this.scanInFlight.set(false);
            return;
        }
        CompletableFuture.supplyAsync(() -> runScan(input), this.scanExecutor).whenComplete((error) -> {
    Runnable apply = () -> {
    if (error != null) {
        LOGGER.warn("[ChunkFinder] Async scan failed: {}", error.toString());
        this.findings.set(ChunkFinderScanner.ScanSnapshot.EMPTY);
    } else {
        this.findings.set((result == null ? ChunkFinderScanner.ScanSnapshot.EMPTY : result));
    }
    this.scanInFlight.set(false);
    break label_10;
    Object var3 = null;
    this.scanInFlight.set(false);
    throw var3;
    label_10:
};
    if (client != null) {
        client.execute(apply);
    } else {
        apply.run();
    }
});
    }

    private ChunkFinderScanner.ScanInput captureInput(class_310 client, int configuredRadiusChunks) {
        if ((client == null) || (client.field_1687 == null)) break label_1;
        if (client.field_1724 == null) {
            label_1:
            return new ChunkFinderScanner.ScanInput(Collections.emptyMap(), 0, -1);
        }
        int effectiveRadius = Math.max(1, Math.min(configuredRadiusChunks, client.field_1690.method_38521()));
        int minY = Math.max(client.field_1687.method_31607(), 0);
        int maxY = Math.min(client.field_1687.method_31600(), 64);
        int playerChunkX = client.field_1724.method_31476().field_9181;
        int playerChunkZ = client.field_1724.method_31476().field_9180;
        Map chunks = new HashMap();
        int cx = (playerChunkX - effectiveRadius);
        while (cx <= (playerChunkX + effectiveRadius)) {
            int cz = (playerChunkZ - effectiveRadius);
            while (cz <= (playerChunkZ + effectiveRadius)) {
                class_2818 chunk = client.field_1687.method_2935().method_12126(cx, cz, false);
                if (chunk == null) {
                } else {
                    class_2826[] copiedSections = ((class_2826[]) Arrays.copyOf(chunk.method_12006(), chunk.method_12006().length));
                    chunks.put(class_1923.method_8331(cx, cz), new ChunkFinderScanner.ChunkSnapshot(cx, cz, chunk.method_31607(), copiedSections));
                }
                cz++;
            }
            cx++;
        }
        return new ChunkFinderScanner.ScanInput(chunks, minY, maxY);
    }

    private ChunkFinderScanner.ScanSnapshot runScan(ChunkFinderScanner.ScanInput input) {
        if (input.maxY() < input.minY()) return ChunkFinderScanner.ScanSnapshot.EMPTY;
        List out = new ArrayList();
        Map chunkScores = new HashMap();
        int minFlags = this.minFlagsPerChunk;
        Set coreChunks = input.chunks().values().iterator();
        while (coreChunks.hasNext()) {
            ChunkFinderScanner.ChunkSnapshot chunk = ((ChunkFinderScanner.ChunkSnapshot) coreChunks.next());
            ChunkFinderScanner.ChunkScanResult chunkResult = scanChunk(input, chunk);
            if (chunkResult.blocks().size() < minFlags) {
                break label_5;
            }
            long chunkKey = class_1923.method_8331(chunk.chunkX(), chunk.chunkZ());
            chunkScores.put(chunkKey, chunkResult.blocks().size());
            Object var10 = chunkResult.blocks().iterator();
            while (var10.hasNext()) {
                ChunkFinderScanner.FlaggedBlock flaggedBlock = ((ChunkFinderScanner.FlaggedBlock) var10.next());
                out.add(flaggedBlock);
                if (out.size() >= 4096) {
                    break label_15;
                }
            }
            label_15:
            if (out.size() >= 4096) {
                break label_17;
            }
        }
        label_17:
        if (out.isEmpty()) return ChunkFinderScanner.ScanSnapshot.EMPTY;
        coreChunks = new HashSet();
        chunk = chunkScores.entrySet().iterator();
        while (chunk.hasNext()) {
            Map.Entry entry = ((Map.Entry) chunk.next());
            if (((Integer) entry.getValue()).intValue() < minFlags) {
                break label_20;
            }
            class_1923 core = new class_1923(class_1923.method_8325(((Long) entry.getKey()).longValue()), class_1923.method_8332(((Long) entry.getKey()).longValue()));
            coreChunks.add(core);
        }
        if (coreChunks.isEmpty()) return ChunkFinderScanner.ScanSnapshot.EMPTY;
        Set flaggedChunks = Set.copyOf(coreChunks);
        return new ChunkFinderScanner.ScanSnapshot(List.copyOf(out), Set.copyOf(coreChunks), flaggedChunks);
    }

    private ChunkFinderScanner.ChunkScanResult scanChunk(ChunkFinderScanner.ScanInput input, ChunkFinderScanner.ChunkSnapshot chunk) {
        int minSeedSamples = this.minSeedSamplesPerChunk;
        double maxMismatch = this.maxSeedMismatchRatio;
        int maxFlags = this.maxFlagsPerChunk;
        int hostSupportRequired = this.hostSeedSupportRequired;
        ChunkFinderScanner.SeedMismatchStats seedStats = evaluateSeedMismatch(input, chunk);
        if (seedStats.compared() >= minSeedSamples && seedStats.mismatchRatio() > maxMismatch) return ChunkFinderScanner.ChunkScanResult.EMPTY;
        int startX = (chunk.chunkX() << 4);
        int startZ = (chunk.chunkZ() << 4);
        class_2826[] sections = chunk.sections();
        List localFindings = new ArrayList();
        Set localDedupe = new HashSet();
        int sectionIndex = 0;
        while (sectionIndex < sections.length) {
            class_2826 section = sections[sectionIndex];
            if (section == null) break label_50;
            if (section.method_38292()) {
                break label_50;
            }
            int sectionMinY = (chunk.bottomY() + (sectionIndex << 4));
            int sectionMaxY = (sectionMinY + 15);
            if (sectionMaxY < input.minY()) break label_50;
            if (sectionMinY > input.maxY()) {
                break label_50;
            }
            int localYMin = Math.max(0, (input.minY() - sectionMinY));
            int localYMax = Math.min(15, (input.maxY() - sectionMinY));
            int ly = localYMin;
            while (ly <= localYMax) {
                int worldY = (sectionMinY + ly);
                int lx = 0;
                while (lx < 16) {
                    int worldX = (startX + lx);
                    int lz = 0;
                    while (lz < 16) {
                        int worldZ = (startZ + lz);
                        class_2248 center = section.method_12254(lx, ly, lz).method_26204();
                        if (!(isCenterCandidate(center, worldY))) {
                            break label_47;
                        }
                        class_2248 host = getUniformHost(input, worldX, worldY, worldZ);
                        if (host == null) break label_47;
                        if (center == host) {
                            break label_47;
                        }
                        ChunkFinderScanner.Lithology hostLithology = lithologyOf(host);
                        if (!(isNaturalLithology(hostLithology))) {
                            break label_47;
                        }
                        if (!(hasVerticalContinuation(input, worldX, worldY, worldZ, center, host))) {
                            break label_47;
                        }
                        if (!(seedSupportsHost(worldX, worldY, worldZ, hostLithology, hostSupportRequired))) {
                            break label_47;
                        }
                        if (isLikelyNaturalBoundary(center, worldX, worldY, worldZ)) {
                            break label_47;
                        }
                        long packed = class_2338.method_10064(worldX, worldY, worldZ);
                        if (!(localDedupe.add(packed))) {
                            break label_47;
                        }
                        localFindings.add(new ChunkFinderScanner.FlaggedBlock(new class_2338(worldX, worldY, worldZ), classify(center)));
                        if (localFindings.size() > maxFlags) return ChunkFinderScanner.ChunkScanResult.EMPTY;
                        label_47:
                        lz++;
                    }
                    lx++;
                }
                ly++;
            }
            label_50:
            sectionIndex++;
        }
        return new ChunkFinderScanner.ChunkScanResult(List.copyOf(localFindings), seedStats);
    }

    private static boolean isCenterCandidate(class_2248 center, int y) {
        if ((center == class_2246.field_10124) || (center == class_2246.field_10543)) {
            return y <= 40;
        }
        if (center == class_2246.field_10566) return y <= 40;
        return CENTER_FILL_CANDIDATES.contains(center);
    }

    private static ChunkFinderScanner.AnomalyType classify(class_2248 center) {
        if ((center == class_2246.field_10124) || (center == class_2246.field_10543)) {
            return ChunkFinderScanner.AnomalyType.AIR_POCKET;
        }
        if (center == class_2246.field_10566) return ChunkFinderScanner.AnomalyType.DIRT_POCKET;
        return ChunkFinderScanner.AnomalyType.BLOCK_MISMATCH;
    }

    private static class_2248 getUniformHost(ChunkFinderScanner.ScanInput input, int x, int y, int z) {
        class_2248 north = getBlock(input, x, y, (z - 1));
        if ((north == null) || (!(NATURAL_HOSTS.contains(north)))) {
            return null;
        }
        class_2248 south = getBlock(input, x, y, (z + 1));
        class_2248 east = getBlock(input, (x + 1), y, z);
        class_2248 west = getBlock(input, (x - 1), y, z);
        if (((south == null) || (east == null)) || (west == null)) {
            return null;
        }
        if (((north != south) || (north != east)) || (north != west)) {
            return null;
        }
        return north;
    }

    private static boolean hasVerticalContinuation(ChunkFinderScanner.ScanInput input, int x, int y, int z, class_2248 center, class_2248 host) {
        if (!(isMismatchAt(input, x, (y - 1), z, center, host))) {
            if (isMismatchAt(input, x, (y + 1), z, center, host)) {
            }
        } else {
        }
        return false;
    }

    private static boolean isMismatchAt(ChunkFinderScanner.ScanInput input, int x, int y, int z, class_2248 center, class_2248 host) {
        if (y < input.minY()) break label_1;
        if (y > input.maxY()) {
            label_1:
            return false;
        }
        class_2248 at = getBlock(input, x, y, z);
        if ((at == null) || (at != center)) {
            return false;
        }
        class_2248 north = getBlock(input, x, y, (z - 1));
        class_2248 south = getBlock(input, x, y, (z + 1));
        class_2248 east = getBlock(input, (x + 1), y, z);
        class_2248 west = getBlock(input, (x - 1), y, z);
        if (((north != host) || (south != host)) || (east != host)) break label_10;
        return west == host;
    }

    private static class_2248 getBlock(ChunkFinderScanner.ScanInput input, int x, int y, int z) {
        ChunkFinderScanner.ChunkSnapshot chunk = ((ChunkFinderScanner.ChunkSnapshot) input.chunks().get(class_1923.method_8331(Math.floorDiv(x, 16), Math.floorDiv(z, 16))));
        if (chunk == null) return null;
        int sectionIndex = ((y - chunk.bottomY()) >> 4);
        if ((sectionIndex < 0) || (sectionIndex >= chunk.sections().length)) {
            return null;
        }
        class_2826 section = chunk.sections()[sectionIndex];
        if ((section == null) || (section.method_38292())) {
            return class_2246.field_10124;
        }
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);
        int sectionBaseY = (chunk.bottomY() + (sectionIndex << 4));
        int localY = (y - sectionBaseY);
        if ((localY < 0) || (localY > 15)) {
            return null;
        }
        return section.method_12254(localX, localY, localZ).method_26204();
    }

    private static boolean seedSupportsHost(int x, int y, int z, ChunkFinderScanner.Lithology hostLithology, int requiredSupport) {
        int support = 0;
        if (predictedMatches(hostLithology, predictLithology(x, y, z), y)) {
            support++;
        }
        if (predictedMatches(hostLithology, predictLithology((x - 1), y, z), y)) {
            support++;
        }
        if (predictedMatches(hostLithology, predictLithology((x + 1), y, z), y)) {
            support++;
        }
        if (predictedMatches(hostLithology, predictLithology(x, y, (z - 1)), y)) {
            support++;
        }
        if (predictedMatches(hostLithology, predictLithology(x, y, (z + 1)), y)) {
            support++;
        }
        return support >= Math.max(1, Math.min(5, requiredSupport));
    }

    private static boolean isLikelyNaturalBoundary(class_2248 center, int x, int y, int z) {
        ChunkFinderScanner.Lithology centerLithology = lithologyOf(center);
        if (!(isNaturalLithology(centerLithology))) return false;
        ChunkFinderScanner.Lithology predictedCenter = predictLithology(x, y, z);
        return predictedMatches(centerLithology, predictedCenter, y);
    }

    private ChunkFinderScanner.SeedMismatchStats evaluateSeedMismatch(ChunkFinderScanner.ScanInput input, ChunkFinderScanner.ChunkSnapshot chunk) {
        int compared = 0;
        int mismatches = 0;
        int startX = (chunk.chunkX() << 4);
        int startZ = (chunk.chunkZ() << 4);
        int sampleMinY = Math.max(input.minY(), 0);
        int sampleMaxY = Math.min(input.maxY(), 64);
        int y = sampleMinY;
        while (y <= sampleMaxY) {
            int lx = 0;
            while (lx < 16) {
                int wx = (startX + lx);
                int lz = 0;
                while (lz < 16) {
                    int wz = (startZ + lz);
                    class_2248 actualBlock = getBlock(input, wx, y, wz);
                    if (actualBlock == null) {
                        break label_22;
                    }
                    ChunkFinderScanner.Lithology actual = lithologyOf(actualBlock);
                    if (!(isNaturalLithology(actual))) {
                        break label_22;
                    }
                    ChunkFinderScanner.Lithology predicted = predictLithology(wx, y, wz);
                    compared++;
                    if (predictedMatches(actual, predicted, y)) break label_22;
                    mismatches++;
                    label_22:
                    lz += 2;
                }
                lx += 2;
            }
            y += 3;
        }
        if (compared <= 0) {
        } else {
        }
        double ratio = (((double) mismatches) / ((double) compared));
        return new ChunkFinderScanner.SeedMismatchStats(compared, mismatches, ratio);
    }

    private static ChunkFinderScanner.Lithology predictLithology(int x, int y, int z) {
        double deepNoise = fractalNoise(-4214343258886220427L, x, y, z, 32, 3);
        double depthBias = clamp01(((8.0 - ((double) y)) / 72.0));
        if (y <= 8 && (deepNoise + (depthBias * 0.7)) > 0.76) {
            double tuffNoise = fractalNoise(-7420117278016355793L, x, y, z, 24, 2);
            if (y <= 36 && tuffNoise > 0.83) return ChunkFinderScanner.Lithology.TUFF;
            return ChunkFinderScanner.Lithology.DEEPSLATE;
        }
        double base = fractalNoise(-3502510645096662901L, x, y, z, 28, 3);
        double warp = (fractalNoise(-8260690080946596621L, x, y, z, 14, 2) - 0.5);
        double selector = ((base + (warp * 0.34)) + (((double) (y - 24)) * 0.0022));
        if (y <= 40) {
            double tuffPatch = fractalNoise(-432501352926360217L, x, y, z, 18, 2);
            if (tuffPatch > 0.86 && selector > 0.26) return ChunkFinderScanner.Lithology.TUFF;
        }
        if (selector < 0.24) return ChunkFinderScanner.Lithology.STONE;
        if (selector < 0.41) return ChunkFinderScanner.Lithology.ANDESITE;
        if (selector < 0.6) return ChunkFinderScanner.Lithology.DIORITE;
        if (selector < 0.8) return ChunkFinderScanner.Lithology.GRANITE;
        return ChunkFinderScanner.Lithology.STONE;
    }

    private static double fractalNoise(long seed, int x, int y, int z, int baseScale, int octaves) {
        double value = 0.0;
        double weightSum = 0.0;
        double amp = 1.0;
        int scale = Math.max(2, baseScale);
        int i = 0;
        while (i < octaves) {
            value = (value + (smoothValueNoise((seed + (((long) i) * -7046029254386353131L)), x, y, z, scale) * amp));
            weightSum = (weightSum + amp);
            amp = (amp * 0.5);
            scale = Math.max(2, (scale / 2));
            i++;
        }
        if (weightSum <= 0.0) return 0.0;
        return (value / weightSum);
    }

    private static double smoothValueNoise(long seed, int x, int y, int z, int scale) {
        int x0 = Math.floorDiv(x, scale);
        int y0 = Math.floorDiv(y, scale);
        int z0 = Math.floorDiv(z, scale);
        int x1 = (x0 + 1);
        int y1 = (y0 + 1);
        int z1 = (z0 + 1);
        double tx = smoothStep((((double) (x - (x0 * scale))) / ((double) scale)));
        double ty = smoothStep((((double) (y - (y0 * scale))) / ((double) scale)));
        double tz = smoothStep((((double) (z - (z0 * scale))) / ((double) scale)));
        double v000 = hashToUnit(seed, x0, y0, z0);
        double v100 = hashToUnit(seed, x1, y0, z0);
        double v010 = hashToUnit(seed, x0, y1, z0);
        double v110 = hashToUnit(seed, x1, y1, z0);
        double v001 = hashToUnit(seed, x0, y0, z1);
        double v101 = hashToUnit(seed, x1, y0, z1);
        double v011 = hashToUnit(seed, x0, y1, z1);
        double v111 = hashToUnit(seed, x1, y1, z1);
        double x00 = lerp(v000, v100, tx);
        double x10 = lerp(v010, v110, tx);
        double x01 = lerp(v001, v101, tx);
        double x11 = lerp(v011, v111, tx);
        double y0v = lerp(x00, x10, ty);
        double y1v = lerp(x01, x11, ty);
        return lerp(y0v, y1v, tz);
    }

    private static double hashToUnit(long seed, int x, int y, int z) {
        long h = seed;
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

    private static double lerp(double a, double b, double t) {
        return (a + ((b - a) * t));
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

    private static boolean predictedMatches(ChunkFinderScanner.Lithology actual, ChunkFinderScanner.Lithology predicted, int y) {
        if (actual == predicted) return true;
        if (actual == ChunkFinderScanner.Lithology.STONE) {
            if (predicted != ChunkFinderScanner.Lithology.DEEPSLATE) {
            }
            if (actual == ChunkFinderScanner.Lithology.DEEPSLATE && predicted == ChunkFinderScanner.Lithology.STONE) {
            }
            if (y < -16) break label_4;
            return y <= 12;
        }
        if (actual != ChunkFinderScanner.Lithology.TUFF) break label_9;
        if (predicted != ChunkFinderScanner.Lithology.STONE) {
            if (predicted != ChunkFinderScanner.Lithology.DEEPSLATE) break label_9;
        }
        if (y <= 32) return true;
        label_9:
        return false;
    }

    private static ChunkFinderScanner.Lithology lithologyOf(class_2248 block) {
        if (block == class_2246.field_10340) return ChunkFinderScanner.Lithology.STONE;
        if (block == class_2246.field_10474) return ChunkFinderScanner.Lithology.GRANITE;
        if (block == class_2246.field_10508) return ChunkFinderScanner.Lithology.DIORITE;
        if (block == class_2246.field_10115) return ChunkFinderScanner.Lithology.ANDESITE;
        if (block == class_2246.field_27165) return ChunkFinderScanner.Lithology.TUFF;
        if ((block == class_2246.field_28888) || (block == class_2246.field_29031)) {
            return ChunkFinderScanner.Lithology.DEEPSLATE;
        }
        return ChunkFinderScanner.Lithology.OTHER;
    }

    private static boolean isNaturalLithology(ChunkFinderScanner.Lithology lithology) {
        if (lithology != ChunkFinderScanner.Lithology.STONE && lithology != ChunkFinderScanner.Lithology.GRANITE && lithology != ChunkFinderScanner.Lithology.DIORITE && lithology != ChunkFinderScanner.Lithology.ANDESITE && lithology != ChunkFinderScanner.Lithology.TUFF) {
            if (lithology == ChunkFinderScanner.Lithology.DEEPSLATE) {
            }
        } else {
        }
        return false;
    }

    static {
        LOGGER = LoggerFactory.getLogger("VieCheats-ChunkFinder");
        NATURAL_HOSTS = Set.of(class_2246.field_10340, class_2246.field_28888, class_2246.field_10474, class_2246.field_10508, class_2246.field_10115, class_2246.field_27165, class_2246.field_27114);
        CENTER_FILL_CANDIDATES = Set.of(class_2246.field_10340, class_2246.field_28888, class_2246.field_10445, class_2246.field_10566, class_2246.field_10474, class_2246.field_10508, class_2246.field_10115, class_2246.field_27165);
    }


    public record FlaggedBlock(class_2338 pos, ChunkFinderScanner.AnomalyType type) {

        public FlaggedBlock(class_2338 pos, ChunkFinderScanner.AnomalyType type) {
            this.pos = pos;
            this.type = type;
        }

        public class_2338 pos() {
            return this.pos;
        }

        public ChunkFinderScanner.AnomalyType type() {
            return this.type;
        }

    }

    public enum AnomalyType {
        BLOCK_MISMATCH,
        AIR_POCKET,
        DIRT_POCKET;

        private final int color;

        private AnomalyType(int color) {
            this.color = color;
        }

        public int color() {
            return this.color;
        }

    }

    record ChunkScanResult(List blocks, ChunkFinderScanner.SeedMismatchStats seedStats) {
        private static final ChunkFinderScanner.ChunkScanResult EMPTY;

        private ChunkScanResult(List blocks, ChunkFinderScanner.SeedMismatchStats seedStats) {
            this.blocks = blocks;
            this.seedStats = seedStats;
        }

        public List blocks() {
            return this.blocks;
        }

        public ChunkFinderScanner.SeedMismatchStats seedStats() {
            return this.seedStats;
        }

        static {
            EMPTY = new ChunkFinderScanner.ChunkScanResult(List.of(), new ChunkFinderScanner.SeedMismatchStats(0, 0, 1.0));
        }

    }

    record ScanSnapshot(List blocks, Set coreChunks, Set flaggedChunks) {
        private static final ChunkFinderScanner.ScanSnapshot EMPTY;

        private ScanSnapshot(List blocks, Set coreChunks, Set flaggedChunks) {
            this.blocks = blocks;
            this.coreChunks = coreChunks;
            this.flaggedChunks = flaggedChunks;
        }

        public List blocks() {
            return this.blocks;
        }

        public Set coreChunks() {
            return this.coreChunks;
        }

        public Set flaggedChunks() {
            return this.flaggedChunks;
        }

        static {
            EMPTY = new ChunkFinderScanner.ScanSnapshot(List.of(), Set.of(), Set.of());
        }

    }

    record ScanInput(Map chunks, int minY, int maxY) {

        private ScanInput(Map chunks, int minY, int maxY) {
            this.chunks = chunks;
            this.minY = minY;
            this.maxY = maxY;
        }

        public Map chunks() {
            return this.chunks;
        }

        public int minY() {
            return this.minY;
        }

        public int maxY() {
            return this.maxY;
        }

    }

    record ChunkSnapshot(int chunkX, int chunkZ, int bottomY, class_2826[] sections) {

        private ChunkSnapshot(int chunkX, int chunkZ, int bottomY, class_2826[] sections) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.bottomY = bottomY;
            this.sections = sections;
        }

        public int chunkX() {
            return this.chunkX;
        }

        public int chunkZ() {
            return this.chunkZ;
        }

        public int bottomY() {
            return this.bottomY;
        }

        public class_2826[] sections() {
            return this.sections;
        }

    }

    enum Lithology {
        STONE,
        GRANITE,
        DIORITE,
        ANDESITE,
        TUFF,
        DEEPSLATE,
        OTHER;


        private Lithology() {
        }

    }

    record SeedMismatchStats(int compared, int mismatches, double mismatchRatio) {

        private SeedMismatchStats(int compared, int mismatches, double mismatchRatio) {
            this.compared = compared;
            this.mismatches = mismatches;
            this.mismatchRatio = mismatchRatio;
        }

        public int compared() {
            return this.compared;
        }

        public int mismatches() {
            return this.mismatches;
        }

        public double mismatchRatio() {
            return this.mismatchRatio;
        }

    }
}

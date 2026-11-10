package me.drake.viecheats.modules.donut;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.drake.viecheats.chunkfinder.SeedSimulationEngine;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.core.setting.StringSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2818;
import net.minecraft.class_2826;

public final class ChunkFinder extends Module {
    private static final int FLAG_PLATE_Y = 63;
    private static final int MIN_RENDER_DISTANCE_RADIUS = 2;
    private static final Set TARGET_STONES;
    private final StringSetting worldSeed;
    private final IntSetting minY;
    private final IntSetting maxY;
    private final IntSetting sensitivity;
    private final IntSetting alpha;
    private final ColorSetting chunkColor;
    private final BoolSetting tracers;
    private final IntSetting scanIntervalMs;
    private final AtomicBoolean scanRunning;
    private final Set flaggedChunks;
    private final Set tracerPoints;
    private final Set persistentFlaggedChunkKeys;
    private final Map stickyTracerByChunk;
    private final ExecutorService scannerExecutor;
    private final AtomicInteger phaseCounter;
    private volatile long lastScanAtMs;
    private String lastWorldKey;

    public ChunkFinder() {
        super("ChunkFinder", "Seed-based near-surface stone mismatch finder", Category.DONUT_SMP);
        this.worldSeed = ((StringSetting) addSetting(((StringSetting) hidden(new StringSetting("Seed", "World seed used for simulation", "6608149111735331168")))));
        this.minY = ((IntSetting) addSetting(new IntSetting("MinY", "Scan minimum Y (near surface)", 40, 0, 200)));
        this.maxY = ((IntSetting) addSetting(new IntSetting("MaxY", "Scan maximum Y (near surface)", 100, 1, 255)));
        this.sensitivity = ((IntSetting) addSetting(new IntSetting("Sensitivity", "Detection sensitivity", 12, 1, 25)));
        this.alpha = ((IntSetting) addSetting(new IntSetting("Alpha", "Flagged chunk opacity", 95, 10, 255)));
        this.chunkColor = ((ColorSetting) addSetting(new ColorSetting("ChunkColor", "Flagged chunk color", -11744433)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracers to flagged chunks", true)));
        this.scanIntervalMs = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("ScanIntervalMs", "Milliseconds between scans", 900, 300, 5000)))));
        this.scanRunning = new AtomicBoolean(false);
        this.flaggedChunks = ConcurrentHashMap.newKeySet();
        this.tracerPoints = ConcurrentHashMap.newKeySet();
        this.persistentFlaggedChunkKeys = ConcurrentHashMap.newKeySet();
        this.stickyTracerByChunk = new ConcurrentHashMap();
        this.scannerExecutor = Executors.newSingleThreadExecutor((r) -> {
    Thread t = new Thread(r, "vie-chunkfinder-seed-scan");
    t.setDaemon(true);
    return t;
});
        this.phaseCounter = new AtomicInteger(0);
        this.lastWorldKey = "";
    }

    private static Setting hidden(Setting setting) {
        setting.setVisibleInGui(false);
        return setting;
    }

    public void onEnable() {
        super.onEnable();
        this.flaggedChunks.clear();
        this.tracerPoints.clear();
        this.persistentFlaggedChunkKeys.clear();
        this.stickyTracerByChunk.clear();
        this.phaseCounter.set(0);
        this.lastScanAtMs = 0L;
        this.lastWorldKey = currentWorldKey();
    }

    public void onDisable() {
        super.onDisable();
        this.flaggedChunks.clear();
        this.tracerPoints.clear();
        this.persistentFlaggedChunkKeys.clear();
        this.stickyTracerByChunk.clear();
        this.lastWorldKey = "";
    }

    public String getInfo() {
        return Integer.toString(this.flaggedChunks.size());
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if ((event.getPhase() != TickEvent.Phase.PRE) || (!(isEnabled()))) break label_1;
        if (!(isInGame())) {
            label_1:
            return;
        }
        String worldKey = currentWorldKey();
        if (!(worldKey.equals(this.lastWorldKey))) {
            this.flaggedChunks.clear();
            this.tracerPoints.clear();
            this.persistentFlaggedChunkKeys.clear();
            this.stickyTracerByChunk.clear();
            this.phaseCounter.set(0);
            this.lastWorldKey = worldKey;
        }
        long now = System.currentTimeMillis();
        if ((now - this.lastScanAtMs) < ((long) ((Integer) this.scanIntervalMs.get()).intValue())) return;
        this.lastScanAtMs = now;
        scheduleAsyncScan();
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if ((event.getStage() != RenderEvent.Stage.WORLD_3D) || (!(isEnabled()))) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (this.flaggedChunks.isEmpty()) return;
        int color = withAlpha(((Integer) this.chunkColor.get()).intValue(), ((Integer) this.alpha.get()).intValue());
        class_1923 playerChunk = mc.field_1724.method_31476();
        int renderRadius = Math.max(2, (mc.field_1690.method_38521() + 2));
        Object var5 = this.flaggedChunks.iterator();
        while (var5.hasNext()) {
            class_1923 chunkPos = ((class_1923) var5.next());
            if (Math.abs((chunkPos.field_9181 - playerChunk.field_9181)) > renderRadius) break label_7;
            if (Math.abs((chunkPos.field_9180 - playerChunk.field_9180)) > renderRadius) {
                break label_7;
            }
            double x1 = ((double) chunkPos.method_8326());
            double z1 = ((double) chunkPos.method_8328());
            RenderUtil.drawFlatPlane(x1, z1, (x1 + 16.0), (z1 + 16.0), 63.0, color);
            if (((Boolean) this.tracers.get()).booleanValue()) {
                int tracerColor = withAlpha(((Integer) this.chunkColor.get()).intValue(), Math.min(255, (((Integer) this.alpha.get()).intValue() + 70)));
                RenderUtil.drawTracerToPos((x1 + 8.0), 63.5, (z1 + 8.0), tracerColor, event.getTickDelta());
            }
        }
    }

    private void scheduleAsyncScan() {
        if (!(this.scanRunning.compareAndSet(false, true))) return;
        ChunkFinder.ScanInput input = captureScanInput();
        if (input.chunks.isEmpty()) {
            this.scanRunning.set(false);
            return;
        }
        CompletableFuture.supplyAsync(() -> runScan(input), this.scannerExecutor).whenComplete((throwable) -> {
    Runnable apply = () -> {
    if (!(isEnabled())) {
        this.scanRunning.set(false);
        return;
    }
    if (throwable == null) {
        if (output == null) {
        }
        updateStickyFlags(ChunkFinder.ScanOutput.EMPTY);
        this.scanRunning.set(false);
        return;
    }
    updateStickyFlags(output);
    this.scanRunning.set(false);
    break label_12;
    Object var3 = null;
    this.scanRunning.set(false);
    throw var3;
    label_12:
};
    if (mc != null) {
        mc.execute(apply);
    } else {
        apply.run();
    }
});
    }

    private void updateStickyFlags(ChunkFinder.ScanOutput output) {
        Object var2 = output.flaggedChunks.iterator();
        while (var2.hasNext()) {
            class_1923 chunkPos = ((class_1923) var2.next());
            long key = class_1923.method_8331(chunkPos.field_9181, chunkPos.field_9180);
            this.persistentFlaggedChunkKeys.add(key);
        }
        var2 = output.tracerPoints.iterator();
        while (var2.hasNext()) {
            class_2338 pos = ((class_2338) var2.next());
            key = class_1923.method_8331(Math.floorDiv(pos.method_10263(), 16), Math.floorDiv(pos.method_10260(), 16));
            this.stickyTracerByChunk.put(key, pos);
        }
        this.flaggedChunks.clear();
        this.tracerPoints.clear();
        var2 = this.persistentFlaggedChunkKeys.iterator();
        while (var2.hasNext()) {
            key = ((Long) var2.next()).longValue();
            int cx = class_1923.method_8325(key);
            int cz = class_1923.method_8332(key);
            this.flaggedChunks.add(new class_1923(cx, cz));
            class_2338 tracer = ((class_2338) this.stickyTracerByChunk.get(key));
            if (tracer != null) {
                this.tracerPoints.add(tracer);
            }
        }
    }

    private String currentWorldKey() {
        if ((mc == null) || (mc.field_1687 == null)) {
            return "";
        }
        return mc.field_1687.method_27983().method_29177().toString();
    }

    private ChunkFinder.ScanInput captureScanInput() {
        if (!(isInGame())) return ChunkFinder.ScanInput.EMPTY;
        int yA = ((Integer) this.minY.get()).intValue();
        int yB = ((Integer) this.maxY.get()).intValue();
        int scanMinY = Math.min(yA, yB);
        int scanMaxY = Math.max(yA, yB);
        scanMinY = Math.max(scanMinY, mc.field_1687.method_31607());
        scanMaxY = Math.min(scanMaxY, mc.field_1687.method_31600());
        if (scanMinY > scanMaxY) return ChunkFinder.ScanInput.EMPTY;
        int radius = Math.max(2, (mc.field_1690.method_38521() + 1));
        class_1923 playerChunk = mc.field_1724.method_31476();
        Map snapshots = new HashMap();
        int cx = (playerChunk.field_9181 - radius);
        while (cx <= (playerChunk.field_9181 + radius)) {
            int cz = (playerChunk.field_9180 - radius);
            while (cz <= (playerChunk.field_9180 + radius)) {
                class_2818 chunk = mc.field_1687.method_2935().method_12126(cx, cz, false);
                if (chunk == null) {
                } else {
                    class_2826[] sections = ((class_2826[]) Arrays.copyOf(chunk.method_12006(), chunk.method_12006().length));
                    snapshots.put(class_1923.method_8331(cx, cz), new ChunkFinder.ChunkSnapshot(cx, cz, chunk.method_31607(), sections));
                }
                cz++;
            }
            cx++;
        }
        return new ChunkFinder.ScanInput(snapshots, parseSeed(((String) this.worldSeed.get())), scanMinY, scanMaxY, (this.phaseCounter.getAndIncrement() & 7));
    }

    private ChunkFinder.ScanOutput runScan(ChunkFinder.ScanInput input) {
        SeedSimulationEngine sim = new SeedSimulationEngine(input.seed);
        Set outChunks = new HashSet();
        Set outTracers = new HashSet();
        int sens = ((Integer) this.sensitivity.get()).intValue();
        int minMismatch = 1;
        int maxMismatch = Math.max(2, (6 + (sens / 2)));
        int minCompared = 24;
        double maxMismatchRatio = Math.min(0.3, (0.08 + (((double) sens) * 0.008)));
        Object var11 = input.chunks.values().iterator();
        while (var11.hasNext()) {
            ChunkFinder.ChunkSnapshot chunk = ((ChunkFinder.ChunkSnapshot) var11.next());
            ChunkFinder.ChunkAnalysis analysis = analyzeChunk(input, chunk, sim);
            if (analysis.compared < minCompared) {
                break label_9;
            }
            if (analysis.mismatches < minMismatch) {
                break label_9;
            }
            if (analysis.mismatches > maxMismatch) {
                break label_9;
            }
            if (analysis.mismatchRatio > maxMismatchRatio) {
                break label_9;
            }
            class_1923 cp = new class_1923(chunk.chunkX, chunk.chunkZ);
            outChunks.add(cp);
            if (analysis.firstMismatch != null) {
                outTracers.add(analysis.firstMismatch);
            }
        }
        return new ChunkFinder.ScanOutput(outChunks, outTracers);
    }

    private ChunkFinder.ChunkAnalysis analyzeChunk(ChunkFinder.ScanInput input, ChunkFinder.ChunkSnapshot chunk, SeedSimulationEngine sim) {
        int compared = 0;
        int mismatches = 0;
        class_2338 firstMismatch = null;
        int startX = (chunk.chunkX << 4);
        int startZ = (chunk.chunkZ << 4);
        int yParity = (input.phase & 1);
        int xParity = ((input.phase >> 1) & 1);
        int zParity = ((input.phase >> 2) & 1);
        int yStart = (input.minY + Math.floorMod((yParity - input.minY), 2));
        int y = yStart;
        while (y <= input.maxY) {
            int lx = xParity;
            while (lx < 16) {
                int wx = (startX + lx);
                int lz = zParity;
                while (lz < 16) {
                    int wz = (startZ + lz);
                    class_2248 actual = getBlockFast(input, wx, y, wz);
                    if (actual == null) break label_26;
                    if (!(TARGET_STONES.contains(actual))) {
                        break label_26;
                    }
                    class_2248 expected = sim.expectedBaseStoneAt(wx, y, wz);
                    if (!(TARGET_STONES.contains(expected))) {
                        break label_26;
                    }
                    compared++;
                    if (actual == expected) {
                        break label_26;
                    }
                    mismatches++;
                    if (firstMismatch != null) break label_26;
                    firstMismatch = new class_2338(wx, y, wz);
                    label_26:
                    lz += 2;
                }
                lx += 2;
            }
            y += 2;
        }
        if (compared <= 0) {
        } else {
        }
        double ratio = (((double) mismatches) / ((double) compared));
        return new ChunkFinder.ChunkAnalysis(compared, mismatches, ratio, firstMismatch);
    }

    private class_2248 getBlockFast(ChunkFinder.ScanInput input, int x, int y, int z) {
        long key = class_1923.method_8331(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        ChunkFinder.ChunkSnapshot chunk = ((ChunkFinder.ChunkSnapshot) input.chunks.get(key));
        if (chunk == null) return null;
        int sectionIndex = ((y - chunk.bottomY) >> 4);
        if ((sectionIndex < 0) || (sectionIndex >= chunk.sections.length)) {
            return null;
        }
        class_2826 section = chunk.sections[sectionIndex];
        if ((section == null) || (section.method_38292())) {
            return class_2246.field_10124;
        }
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);
        int sectionBaseY = (chunk.bottomY + (sectionIndex << 4));
        int localY = (y - sectionBaseY);
        if ((localY < 0) || (localY > 15)) {
            return null;
        }
        return section.method_12254(localX, localY, localZ).method_26204();
    }

    private static long parseSeed(String value) {
        if (value != null) {
            if (value.isBlank()) {
            }
            return 6608149111735331168L;
            try {
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex4) {
            NumberFormatException ignored = ex4;
            return 6608149111735331168L;
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return ((a << 24) | (rgb & 16777215));
    }

    static {
        TARGET_STONES = Set.of(class_2246.field_10340, class_2246.field_10474, class_2246.field_10508, class_2246.field_10115);
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

    record ScanInput(Map chunks, long seed, int minY, int maxY, int phase) {
        private static final ChunkFinder.ScanInput EMPTY;

        private ScanInput(Map chunks, long seed, int minY, int maxY, int phase) {
            this.chunks = chunks;
            this.seed = seed;
            this.minY = minY;
            this.maxY = maxY;
            this.phase = phase;
        }

        public Map chunks() {
            return this.chunks;
        }

        public long seed() {
            return this.seed;
        }

        public int minY() {
            return this.minY;
        }

        public int maxY() {
            return this.maxY;
        }

        public int phase() {
            return this.phase;
        }

        static {
            EMPTY = new ChunkFinder.ScanInput(Map.of(), 6608149111735331168L, 0, -1, 0);
        }

    }

    record ChunkAnalysis(int compared, int mismatches, double mismatchRatio, class_2338 firstMismatch) {

        private ChunkAnalysis(int compared, int mismatches, double mismatchRatio, class_2338 firstMismatch) {
            this.compared = compared;
            this.mismatches = mismatches;
            this.mismatchRatio = mismatchRatio;
            this.firstMismatch = firstMismatch;
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

        public class_2338 firstMismatch() {
            return this.firstMismatch;
        }

    }

    record ScanOutput(Set flaggedChunks, Set tracerPoints) {
        private static final ChunkFinder.ScanOutput EMPTY;

        private ScanOutput(Set flaggedChunks, Set tracerPoints) {
            this.flaggedChunks = flaggedChunks;
            this.tracerPoints = tracerPoints;
        }

        public Set flaggedChunks() {
            return this.flaggedChunks;
        }

        public Set tracerPoints() {
            return this.tracerPoints;
        }

        static {
            EMPTY = new ChunkFinder.ScanOutput(Set.of(), Set.of());
        }

    }
}

package me.drake.viecheats.modules.donut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;

public class SusChunkFinder extends Module {
    private static final int PLATE_Y = 63;
    private static final long SCAN_TICK_MS = 150L;
    private static final long DEFAULT_WORLD_SEED = 6608149111735331168L;
    private static final int GEODE_MIN_Y = -64;
    private static final int GEODE_MAX_Y = 30;
    private static final int MIN_SCAN_RADIUS_CHUNKS = 2;
    private static final int RENDER_RADIUS_MARGIN_CHUNKS = 1;
    private static final class_2350[] DIRECTIONS;
    private final IntSetting alpha;
    private final ColorSetting chunkColor;
    private final IntSetting sensitivity;
    private final IntSetting aggression;
    private final IntSetting distance;
    private final IntSetting minAmethyst;
    private final IntSetting brightDelta;
    private final IntSetting brightRatio;
    private final IntSetting minLight;
    private final BoolSetting tracers;
    private static final Set SUS_CHUNKS;
    private static final Set GEODE_CENTERS;
    private static final Set PENDING_CHUNK_KEYS;
    private static final ExecutorService SCAN_EXECUTOR;
    private static final AtomicBoolean scanRunning;
    private static long lastScheduleMs;

    public SusChunkFinder() {
        super("SusChunkFinder", "Find suspicious chunks by bright amethyst density", Category.DONUT_SMP);
        this.alpha = ((IntSetting) addSetting(new IntSetting("Alpha", "Opacity", 100, 5, 255)));
        this.chunkColor = ((ColorSetting) addSetting(new ColorSetting("ChunkColor", "Color of highlighted suspicious chunks", -57826)));
        this.sensitivity = ((IntSetting) addSetting(new IntSetting("Sensitivity", "Minimum bright amethyst blocks to flag", 6, 1, 64)));
        this.aggression = ((IntSetting) addSetting(new IntSetting("Aggression", "0 = light-only, 100 = strongest seed comparison", 35, 0, 100)));
        this.distance = ((IntSetting) addSetting(new IntSetting("Distance", "Max spread between bright amethyst groups (blocks)", 8, 1, 32)));
        this.minAmethyst = ((IntSetting) addSetting(new IntSetting("MinAmethyst", "Minimum amethyst blocks in chunk before evaluating", 24, 4, 256)));
        this.brightDelta = ((IntSetting) addSetting(new IntSetting("BrightDelta", "Required light-level gap above chunk average", 2, 1, 8)));
        this.brightRatio = ((IntSetting) addSetting(new IntSetting("BrightRatio", "Minimum bright-amethyst percentage in chunk", 8, 1, 80)));
        this.minLight = ((IntSetting) addSetting(new IntSetting("MinLight", "Absolute minimum light level to count as bright", 2, 0, 15)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracer lines", true)));
        this.minAmethyst.setVisibleInGui(false);
        this.brightDelta.setVisibleInGui(false);
        this.brightRatio.setVisibleInGui(false);
        this.minLight.setVisibleInGui(false);
    }

    public void onDisable() {
        super.onDisable();
        SUS_CHUNKS.clear();
        GEODE_CENTERS.clear();
        PENDING_CHUNK_KEYS.clear();
        scanRunning.set(false);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (((e.getPhase() != TickEvent.Phase.PRE) || (!(isInGame()))) || (!(isEnabled()))) {
            return;
        }
        scheduleScan(false);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!(isEnabled())) break label_1;
        if (!(isInGame())) {
            label_1:
            return;
        }
        class_1923 pos = e.getChunkPos();
        long key = class_1923.method_8331(pos.field_9181, pos.field_9180);
        if (e.isLoaded()) {
            PENDING_CHUNK_KEYS.add(key);
            scheduleScan(true);
        } else {
            PENDING_CHUNK_KEYS.remove(key);
            SUS_CHUNKS.remove(pos);
            GEODE_CENTERS.remove(pos);
        }
    }

    @EventHandler
    public void onRender(RenderEvent ev) {
        if ((ev.getStage() != RenderEvent.Stage.WORLD_3D) || (!(isInGame()))) break label_1;
        if (!(isEnabled())) {
            label_1:
            return;
        }
        if (SUS_CHUNKS.isEmpty()) return;
        float td = ev.getTickDelta();
        int baseColor = ((Integer) this.chunkColor.get()).intValue();
        int red = ((baseColor >> 16) & 255);
        int green = ((baseColor >> 8) & 255);
        int blue = (baseColor & 255);
        int a = Math.max(5, Math.min(255, ((Integer) this.alpha.get()).intValue()));
        int fillColor = argb(a, red, green, blue);
        int tracerColor = argb(Math.min(255, (a + 80)), red, green, blue);
        Object var10 = SUS_CHUNKS.iterator();
        while (var10.hasNext()) {
            class_1923 cp = ((class_1923) var10.next());
            double x1 = ((double) cp.method_8326());
            double z1 = ((double) cp.method_8328());
            RenderUtil.drawFlatPlane(x1, z1, (x1 + 16.0), (z1 + 16.0), 63.0, fillColor);
            if (((Boolean) this.tracers.get()).booleanValue() && GEODE_CENTERS.contains(cp)) {
                RenderUtil.drawTracerToPos((((double) cp.method_8326()) + 8.0), 63.0, (((double) cp.method_8328()) + 8.0), tracerColor, td);
            }
        }
    }

    private void scheduleScan(boolean immediate) {
        long now = System.currentTimeMillis();
        if (scanRunning.get()) return;
        if (!immediate && (now - lastScheduleMs) < 150L) return;
        lastScheduleMs = now;
        Set seen = new HashSet();
        List candidates = new ArrayList();
        if (PENDING_CHUNK_KEYS.isEmpty()) break label_19;
        class_1923 playerChunk = new HashSet(PENDING_CHUNK_KEYS).iterator();
        while (playerChunk.hasNext()) {
            long packed = ((Long) playerChunk.next()).longValue();
            int cx = class_1923.method_8325(packed);
            int cz = class_1923.method_8332(packed);
            class_2818 loaded = mc.field_1687.method_2935().method_12126(cx, cz, false);
            if (loaded == null) {
                PENDING_CHUNK_KEYS.remove(packed);
                break label_8;
            }
            if (seen.add(packed)) {
                candidates.add(loaded);
            }
            PENDING_CHUNK_KEYS.remove(packed);
        }
        label_19:
        playerChunk = mc.field_1724.method_31476();
        int scanRadius = resolveScanRadiusChunks();
        cx = (playerChunk.field_9181 - scanRadius);
        while (cx <= (playerChunk.field_9181 + scanRadius)) {
            cz = (playerChunk.field_9180 - scanRadius);
            while (cz <= (playerChunk.field_9180 + scanRadius)) {
                packed = class_1923.method_8331(cx, cz);
                if (!(seen.add(packed))) {
                    break label_30;
                }
                loaded = mc.field_1687.method_2935().method_12126(cx, cz, false);
                if (loaded == null) {
                    break label_30;
                }
                candidates.add(loaded);
                label_30:
                cz++;
            }
            cx++;
        }
        if (candidates.isEmpty()) return;
        int minBrightCount = ((Integer) this.sensitivity.get()).intValue();
        int maxSpread = ((Integer) this.distance.get()).intValue();
        int minAmethystBlocks = ((Integer) this.minAmethyst.get()).intValue();
        int requiredDelta = ((Integer) this.brightDelta.get()).intValue();
        int minBrightPercent = ((Integer) this.brightRatio.get()).intValue();
        int absoluteMinLight = ((Integer) this.minLight.get()).intValue();
        int aggressionLevel = ((Integer) this.aggression.get()).intValue();
        scanRunning.set(true);
        SCAN_EXECUTOR.execute(() -> {
    Set nextSusChunks = new HashSet();
    Set nextGeodeCenters = new HashSet();
    Object var11 = candidates.iterator();
    while (var11.hasNext()) {
        class_2818 chunk = ((class_2818) var11.next());
        if (mc.field_1687 == null) {
            break label_12;
        }
        class_1923 cp = chunk.method_12004();
        SusChunkFinder.ChunkResult result = analyzeChunk(chunk, minBrightCount, maxSpread, minAmethystBlocks, requiredDelta, minBrightPercent, absoluteMinLight, aggressionLevel);
        if (!(!result.flagged)) {
            nextGeodeCenters.add(cp);
            nextSusChunks.add(cp);
            collectFlagRadius(cp, maxSpread, result.aggressionExcess, nextSusChunks);
        }
    }
    label_12:
    SUS_CHUNKS.clear();
    SUS_CHUNKS.addAll(nextSusChunks);
    GEODE_CENTERS.clear();
    GEODE_CENTERS.addAll(nextGeodeCenters);
    scanRunning.set(false);
    break label_21;
    Object var15 = null;
    scanRunning.set(false);
    throw var15;
    label_21:
});
    }

    private int resolveScanRadiusChunks() {
        int renderDistance = mc.field_1690.method_38521();
        return Math.max(2, (renderDistance + 1));
    }

    private SusChunkFinder.ChunkResult analyzeChunk(class_2818 chunk, int minBrightCount, int maxSpread, int minAmethystBlocks, int requiredDelta, int minBrightPercent, int absoluteMinLight, int aggressionLevel) {
        if (mc.field_1687 == null) return new SusChunkFinder.ChunkResult(false, 0, 0);
        int startX = chunk.method_12004().method_8326();
        int startZ = chunk.method_12004().method_8328();
        int chunkBottomY = chunk.method_31607();
        class_2826[] sections = chunk.method_12006();
        class_2338.class_2339 pos = new class_2338.class_2339();
        class_2338.class_2339 adjPos = new class_2338.class_2339();
        List samples = new ArrayList();
        int totalBrightness = 0;
        int sectionIndex = 0;
        while (sectionIndex < sections.length) {
            class_2826 section = sections[sectionIndex];
            if (section == null) break label_36;
            if (section.method_38292()) {
                break label_36;
            }
            int sectionMinY = (chunkBottomY + (sectionIndex * 16));
            int sectionMaxY = (sectionMinY + 15);
            if (sectionMaxY < -64) break label_36;
            if (sectionMinY > 30) {
                break label_36;
            }
            int lx = 0;
            while (lx < 16) {
                int wx = (startX + lx);
                int lz = 0;
                while (lz < 16) {
                    int wz = (startZ + lz);
                    int ly = 0;
                    while (ly < 16) {
                        int wy = (sectionMinY + ly);
                        if (wy < -64) break label_33;
                        if (wy > 30) {
                            break label_33;
                        }
                        class_2680 state = section.method_12254(lx, ly, lz);
                        if (!(state.method_27852(class_2246.field_27159))) {
                            break label_33;
                        }
                        pos.method_10103(wx, wy, wz);
                        int light = sampleAmethystBrightness(chunk, pos, adjPos);
                        totalBrightness = (totalBrightness + light);
                        samples.add(new SusChunkFinder.AmethystSample(pos.method_10062(), light));
                        label_33:
                        ly++;
                    }
                    lz++;
                }
                lx++;
            }
            label_36:
            sectionIndex++;
        }
        if (samples.size() < minAmethystBlocks) return new SusChunkFinder.ChunkResult(false, 0, 0);
        double averageBrightness = (((double) totalBrightness) / ((double) samples.size()));
        int brightThreshold = Math.min(15, Math.max(absoluteMinLight, ((int) Math.ceil((averageBrightness + ((double) requiredDelta))))));
        int brightCount = 0;
        int minX = 2147483647;
        int minY = 2147483647;
        int minZ = 2147483647;
        int maxX = -2147483648;
        int maxY = -2147483648;
        int maxZ = -2147483648;
        state = samples.iterator();
        while (state.hasNext()) {
            SusChunkFinder.AmethystSample sample = ((SusChunkFinder.AmethystSample) state.next());
            if (sample.brightness < brightThreshold) {
                break label_49;
            }
            brightCount++;
            int px = sample.pos.method_10263();
            int py = sample.pos.method_10264();
            int pz = sample.pos.method_10260();
            if (px < minX) {
                minX = px;
            }
            if (py < minY) {
                minY = py;
            }
            if (pz < minZ) {
                minZ = pz;
            }
            if (px > maxX) {
                maxX = px;
            }
            if (py > maxY) {
                maxY = py;
            }
            if (pz > maxZ) {
                maxZ = pz;
            }
        }
        if (brightCount < minBrightCount) return new SusChunkFinder.ChunkResult(false, 0, 0);
        int brightPercent = ((brightCount * 100) / samples.size());
        if (brightPercent < minBrightPercent) return new SusChunkFinder.ChunkResult(false, 0, 0);
        int spreadX = (maxX - minX);
        int spreadY = (maxY - minY);
        int spreadZ = (maxZ - minZ);
        int maxAxisSpread = Math.max(spreadX, Math.max(spreadY, spreadZ));
        boolean denseEnough = maxAxisSpread <= (maxSpread * 2);
        int spreadBonus = Math.max(0, ((maxSpread * 2) - maxAxisSpread));
        int score = (brightCount + (spreadBonus / 2));
        int aggressionExcess = 0;
        if (aggressionLevel > 0) {
            int expectedBrightPercent = expectedSeedBrightPercent(6608149111735331168L, chunk.method_12004().field_9181, chunk.method_12004().field_9180);
            int tolerance = (26 - ((aggressionLevel * 16) / 100));
            int aggressionThreshold = (expectedBrightPercent - tolerance);
            if (brightPercent < aggressionThreshold) return new SusChunkFinder.ChunkResult(false, 0, 0);
            aggressionExcess = (brightPercent - aggressionThreshold);
            int seedExcess = (brightPercent - expectedBrightPercent);
            if (seedExcess > 0) {
                score = (score + ((seedExcess * aggressionLevel) / 120));
            }
        } else {
            aggressionExcess = Math.max(0, (brightCount - minBrightCount));
        }
        return new SusChunkFinder.ChunkResult(denseEnough, score, aggressionExcess);
    }

    private int sampleAmethystBrightness(class_2818 chunk, class_2338.class_2339 amethystPos, class_2338.class_2339 adjPos) {
        int brightest = 0;
        boolean exposedFace = false;
        Object var6 = DIRECTIONS;
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            class_2350 dir = var6[var8];
            adjPos.method_25505(amethystPos, dir);
            class_2680 adjacentState = getBlockStateFast(chunk, adjPos);
            if (!(adjacentState.method_26215()) && adjacentState.method_26227().method_15769()) {
                break label_11;
            }
            exposedFace = true;
            int blockLight = mc.field_1687.method_8314(class_1944.field_9282, adjPos);
            if (blockLight <= brightest) break label_11;
            brightest = blockLight;
            label_11:
            var8++;
        }
        if (!exposedFace) {
            brightest = mc.field_1687.method_8314(class_1944.field_9282, amethystPos);
        }
        return brightest;
    }

    private class_2680 getBlockStateFast(class_2818 baseChunk, class_2338 pos) {
        int cx = (pos.method_10263() >> 4);
        int cz = (pos.method_10260() >> 4);
        if (cx == baseChunk.method_12004().field_9181 && cz == baseChunk.method_12004().field_9180) return baseChunk.method_8320(pos);
        return mc.field_1687.method_8320(pos);
    }

    private void collectFlagRadius(class_1923 geodeChunk, int maxSpreadBlocks, int aggressionExcess, Set output) {
        int mildThreshold = Math.max(2, ((maxSpreadBlocks + 3) / 4));
        int strongThreshold = Math.max(6, ((maxSpreadBlocks + 3) / 2));
        if (aggressionExcess <= 0) {
            int radius = 0;
            break label_12;
        }
        if (aggressionExcess <= mildThreshold) {
            radius = 1;
            break label_12;
        }
        if (aggressionExcess <= strongThreshold) {
            radius = 2;
            break label_12;
        }
        radius = 3;
        label_12:
        int dx = -(radius);
        while (dx <= radius) {
            int dz = -(radius);
            while (dz <= radius) {
                if (dx == 0 && dz == 0) {
                    break label_21;
                }
                if (((dx * dx) + (dz * dz)) > (radius * radius)) {
                    break label_21;
                }
                class_1923 neighbor = new class_1923((geodeChunk.field_9181 + dx), (geodeChunk.field_9180 + dz));
                if (mc.field_1687.method_2935().method_21730(neighbor.field_9181, neighbor.field_9180) == null) {
                    break label_21;
                }
                output.add(neighbor);
                label_21:
                dz++;
            }
            dx++;
        }
    }

    private int argb(int a, int r, int g, int b) {
        a = Math.max(0, Math.min(255, a));
        return ((((a << 24) | ((r & 255) << 16)) | ((g & 255) << 8)) | (b & 255));
    }

    private static int expectedSeedBrightPercent(long seed, int chunkX, int chunkZ) {
        double p = seedPotential(seed, chunkX, chunkZ);
        return (5 + ((int) Math.round((p * 22.0))));
    }

    private static double seedPotential(long seed, int chunkX, int chunkZ) {
        double c = valueNoise((seed ^ 1592639215L), chunkX, chunkZ);
        double n = valueNoise((seed ^ -7046029254386353131L), chunkX, (chunkZ - 1));
        double s = valueNoise((seed ^ -7046029254386353131L), chunkX, (chunkZ + 1));
        double w = valueNoise((seed ^ -2960836687051489901L), (chunkX - 1), chunkZ);
        double e = valueNoise((seed ^ -2960836687051489901L), (chunkX + 1), chunkZ);
        double smooth = ((c * 0.5) + ((((n + s) + w) + e) * 0.125));
        if (smooth < 0.0) {
            smooth = 0.0;
        }
        if (smooth > 1.0) {
            smooth = 1.0;
        }
        return smooth;
    }

    private static double valueNoise(long seed, int x, int z) {
        long h = seed;
        h = (h ^ mix64((((long) x) * -7046029254386353131L)));
        h = (h ^ mix64((((long) z) * -4417276706812531889L)));
        h = mix64(h);
        long bits = ((h >>> 11) & 9007199254740991L);
        return (((double) bits) * 1.1102230246251565E-16);
    }

    private static long mix64(long z) {
        z = ((z ^ (z >>> 30)) * -4658895280553007687L);
        z = ((z ^ (z >>> 27)) * -7723592293110705685L);
        return (z ^ (z >>> 31));
    }

    static {
        DIRECTIONS = class_2350.values();
        SUS_CHUNKS = ConcurrentHashMap.newKeySet();
        GEODE_CENTERS = ConcurrentHashMap.newKeySet();
        PENDING_CHUNK_KEYS = ConcurrentHashMap.newKeySet();
        SCAN_EXECUTOR = Executors.newSingleThreadExecutor((r) -> {
    Thread t = new Thread(r, "viecheats-suschunk-scan");
    t.setDaemon(true);
    return t;
});
        scanRunning = new AtomicBoolean(false);
    }


    record ChunkResult(boolean flagged, int score, int aggressionExcess) {

        private ChunkResult(boolean flagged, int score, int aggressionExcess) {
            this.flagged = flagged;
            this.score = score;
            this.aggressionExcess = aggressionExcess;
        }

        public boolean flagged() {
            return this.flagged;
        }

        public int score() {
            return this.score;
        }

        public int aggressionExcess() {
            return this.aggressionExcess;
        }

    }

    record AmethystSample(class_2338 pos, int brightness) {

        private AmethystSample(class_2338 pos, int brightness) {
            this.pos = pos;
            this.brightness = brightness;
        }

        public class_2338 pos() {
            return this.pos;
        }

        public int brightness() {
            return this.brightness;
        }

    }
}

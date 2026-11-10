package me.drake.viecheats.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import me.drake.viecheats.core.event.events.ReceivePacketEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2666;
import net.minecraft.class_2680;
import net.minecraft.class_2818;

public final class BedrockHoleESP extends Module {
    private static final int[] OVERWORLD_Y_LEVELS;
    private static final int[] NETHER_FLOOR_Y_LEVELS;
    private static final int[] NETHER_ROOF_Y_LEVELS;
    private static final int CELL_MASK = 255;
    private static final int MAX_GROUP_SIZE = 200;
    private final IntSetting minVoidSize;
    private final BoolSetting showEsp;
    private final BoolSetting showTracers;
    private final BoolSetting chatFeedback;
    private final IntSetting maxMessagesPerMinute;
    private final ColorSetting espColor;
    private final EnumSetting shapeMode;
    private final ColorSetting tracerColor;
    private final BoolSetting useThreading;
    private final IntSetting threadPoolSize;
    private final IntSetting rescanTicks;
    private final IntSetting scanBudget;
    private final IntSetting maxRender;
    private final Map chunkVoidBlocks;
    private final Map chunkAnchors;
    private final Map chunkScanSeq;
    private final Set knownAnchors;
    private final AtomicInteger totalVoidBlocks;
    private volatile ExecutorService threadPool;
    private String currentDimension;
    private int scanRingIndex;
    private int tickCounter;
    private long minuteWindow;
    private int messagesInWindow;

    public BedrockHoleESP() {
        super("BedrockHoleESP", "Finds connected non-bedrock holes in bedrock layers", Category.RENDER);
        this.minVoidSize = ((IntSetting) addSetting(new IntSetting("MinVoidSize", "Minimum connected non-bedrock blocks to flag", 2, 1, 50)));
        this.showEsp = ((BoolSetting) addSetting(new BoolSetting("ShowESP", "Render bedrock-hole ESP boxes", true)));
        this.showTracers = ((BoolSetting) addSetting(new BoolSetting("ShowTracers", "Render tracers to bedrock holes", false)));
        this.chatFeedback = ((BoolSetting) addSetting(new BoolSetting("ChatFeedback", "Announce newly found holes in chat", true)));
        this.maxMessagesPerMinute = ((IntSetting) addSetting(new IntSetting("MaxMsgPerMin", "Maximum chat messages per minute (0 = unlimited)", 10, 0, 60)));
        this.espColor = ((ColorSetting) addSetting(new ColorSetting("ESPColor", "Color for bedrock-hole ESP", -2131733168)));
        this.shapeMode = ((EnumSetting) addSetting(new EnumSetting("ShapeMode", "ESP shape mode", BedrockHoleESP.ShapeMode.BOTH)));
        this.tracerColor = ((ColorSetting) addSetting(new ColorSetting("TracerColor", "Tracer color for bedrock holes", -65536)));
        this.useThreading = ((BoolSetting) addSetting(new BoolSetting("Threading", "Use worker threads for chunk scan", true)));
        this.threadPoolSize = ((IntSetting) addSetting(new IntSetting("ThreadPoolSize", "Number of scanner threads", 4, 1, 8)));
        this.rescanTicks = ((IntSetting) addSetting(new IntSetting("RescanTicks", "Ticks between automatic rescans", 20, 5, 200)));
        this.scanBudget = ((IntSetting) addSetting(new IntSetting("ScanBudget", "Chunks scanned each rescan tick", 14, 1, 128)));
        this.maxRender = ((IntSetting) addSetting(new IntSetting("MaxRender", "Maximum rendered hole blocks per frame", 5000, 100, 20000)));
        this.chunkVoidBlocks = new ConcurrentHashMap();
        this.chunkAnchors = new ConcurrentHashMap();
        this.chunkScanSeq = new ConcurrentHashMap();
        this.knownAnchors = ConcurrentHashMap.newKeySet();
        this.totalVoidBlocks = new AtomicInteger(0);
        this.currentDimension = "";
        this.scanRingIndex = 0;
        this.tickCounter = 0;
        this.minuteWindow = -1L;
        this.messagesInWindow = 0;
        this.minVoidSize.onChanged(() -> triggerFullRescan());
        this.useThreading.onChanged(() -> rebuildThreadPoolIfNeeded());
        this.threadPoolSize.onChanged(() -> rebuildThreadPoolIfNeeded());
    }

    public void onEnable() {
        super.onEnable();
        if (!(isInGame())) return;
        this.currentDimension = resolveDimension();
        this.tickCounter = 0;
        this.scanRingIndex = 0;
        this.minuteWindow = -1L;
        this.messagesInWindow = 0;
        clearAllDetections();
        rebuildThreadPoolIfNeeded();
        fullRescan();
    }

    public void onDisable() {
        shutdownThreadPool();
        clearAllDetections();
        super.onDisable();
    }

    public String getInfo() {
        return Integer.toString(this.totalVoidBlocks.get());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if ((!(isEnabled())) || (!(isInGame()))) {
            return;
        }
        if (event.isLoaded()) {
            scheduleChunkScan(event.getChunkPos());
        } else {
            removeChunkData(event.getChunkPos());
        }
    }

    @EventHandler
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!(isEnabled())) return;
        Object var3 = event.getPacket();
        if ((var3 instanceof class_2666)) {
            class_2666 packet = ((class_2666) var3);
            removeChunkData(packet.comp_1726());
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if ((event.getPhase() != TickEvent.Phase.PRE) || (!(isEnabled()))) break label_1;
        if (!(isInGame())) {
            label_1:
            return;
        }
        String dim = resolveDimension();
        if (!(dim.equals(this.currentDimension))) {
            this.currentDimension = dim;
            clearAllDetections();
            fullRescan();
            return;
        }
        this.tickCounter = (this.tickCounter + 1);
        if ((this.tickCounter + 1) >= ((Integer) this.rescanTicks.get()).intValue()) {
            this.tickCounter = 0;
            rescanNearby();
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (!(((Boolean) this.showEsp.get()).booleanValue()) && !(((Boolean) this.showTracers.get()).booleanValue())) return;
        if (this.chunkVoidBlocks.isEmpty()) return;
        BedrockHoleESP.ShapeMode mode = ((BedrockHoleESP.ShapeMode) this.shapeMode.get());
        int fillColor = ((Integer) this.espColor.get()).intValue();
        int tracerCol = ((Integer) this.tracerColor.get()).intValue();
        int renderCap = Math.max(1, ((Integer) this.maxRender.get()).intValue());
        float tickDelta = event.getTickDelta();
        int rendered = 0;
        Object var8 = this.chunkVoidBlocks.values().iterator();
        while (var8.hasNext()) {
            Set positions = ((Set) var8.next());
            Object var10 = positions.iterator();
            while (var10.hasNext()) {
                class_2338 pos = ((class_2338) var10.next());
                if (rendered >= renderCap) return;
                class_238 box = new class_238(pos);
                if (!(((Boolean) this.showEsp.get()).booleanValue())) break label_21;
                if (mode != BedrockHoleESP.ShapeMode.OUTLINE) {
                    RenderUtil.drawFilledBox(box, fillColor);
                }
                if (mode == BedrockHoleESP.ShapeMode.FILL) break label_21;
                RenderUtil.drawBox(box, fillColor);
                label_21:
                if (((Boolean) this.showTracers.get()).booleanValue()) {
                    RenderUtil.drawTracerToPos((((double) pos.method_10263()) + 0.5), (((double) pos.method_10264()) + 0.5), (((double) pos.method_10260()) + 0.5), tracerCol, tickDelta);
                }
                rendered++;
            }
        }
    }

    private void triggerFullRescan() {
        if ((!(isEnabled())) || (!(isInGame()))) {
            return;
        }
        clearAllDetections();
        fullRescan();
    }

    private void rebuildThreadPoolIfNeeded() {
        if (!(isEnabled())) return;
        shutdownThreadPool();
        if (!(((Boolean) this.useThreading.get()).booleanValue())) return;
        int workers = Math.max(1, ((Integer) this.threadPoolSize.get()).intValue());
        this.threadPool = Executors.newFixedThreadPool(workers, (r) -> {
    Thread t = new Thread(r, "vie-bedrock-hole-scan");
    t.setDaemon(true);
    return t;
});
    }

    private void shutdownThreadPool() {
        ExecutorService pool = this.threadPool;
        this.threadPool = null;
        if (pool != null && !(pool.isShutdown())) {
            pool.shutdownNow();
        }
    }

    private void clearAllDetections() {
        this.chunkVoidBlocks.clear();
        this.chunkAnchors.clear();
        this.chunkScanSeq.clear();
        this.knownAnchors.clear();
        this.totalVoidBlocks.set(0);
    }

    private void fullRescan() {
        if (!(isInGame())) return;
        class_1923 playerChunk = mc.field_1724.method_31476();
        int rd = Math.max(1, mc.field_1690.method_38521());
        int cx = (playerChunk.field_9181 - rd);
        while (cx <= (playerChunk.field_9181 + rd)) {
            int cz = (playerChunk.field_9180 - rd);
            while (cz <= (playerChunk.field_9180 + rd)) {
                scheduleChunkScan(new class_1923(cx, cz));
                cz++;
            }
            cx++;
        }
    }

    private void rescanNearby() {
        if (!(isInGame())) return;
        class_1923 playerChunk = mc.field_1724.method_31476();
        int budget = Math.max(1, ((Integer) this.scanBudget.get()).intValue());
        int rd = Math.max(1, mc.field_1690.method_38521());
        int dx = -1;
        while (dx <= 1) {
            int dz = -1;
            while (dz <= 1) {
                if (budget <= 0) {
                    break label_13;
                }
                scheduleChunkScan(new class_1923((playerChunk.field_9181 + dx), (playerChunk.field_9180 + dz)));
                budget--;
                dz++;
            }
            dx++;
        }
        label_13:
        if ((budget <= 0) || (rd <= 1)) {
            return;
        }
        int ringSpan = Math.max(1, (rd - 1));
        int ring = Math.min(rd, ((this.scanRingIndex % ringSpan) + 2));
        this.scanRingIndex = (this.scanRingIndex + 1);
        dx = -(ring);
        while (dx <= ring) {
            if (Math.abs(dx) == ring) {
                dz = -(ring);
                while (dz <= ring) {
                    if (budget <= 0) {
                        break label_35;
                    }
                    scheduleChunkScan(new class_1923((playerChunk.field_9181 + dx), (playerChunk.field_9180 + dz)));
                    budget--;
                    dz++;
                }
            } else {
                if (budget <= 0) {
                    break label_35;
                }
                scheduleChunkScan(new class_1923((playerChunk.field_9181 + dx), (playerChunk.field_9180 - ring)));
                budget--;
                if (budget <= 0) {
                    break label_35;
                }
                scheduleChunkScan(new class_1923((playerChunk.field_9181 + dx), (playerChunk.field_9180 + ring)));
                budget--;
            }
            dx++;
        }
        label_35:
    }

    private void scheduleChunkScan(class_1923 pos) {
        if (!(isInGame())) return;
        class_2818 chunk = mc.field_1687.method_2935().method_12126(pos.field_9181, pos.field_9180, false);
        if (chunk == null) {
            removeChunkData(pos);
            return;
        }
        scheduleChunkScan(chunk);
    }

    private void scheduleChunkScan(class_2818 chunk) {
        if (!(isInGame())) return;
        int[] yLevels = getYLevelsForDimension(this.currentDimension);
        if (yLevels.length == 0) return;
        long key = class_1923.method_8331(chunk.method_12004().field_9181, chunk.method_12004().field_9180);
        int seq = ((Integer) this.chunkScanSeq.merge(key, 1, Integer::sum)).intValue();
        BedrockHoleESP.ChunkSnapshot snapshot = captureSnapshot(chunk, yLevels);
        if (snapshot == null) return;
        Runnable task = () -> {
    BedrockHoleESP.ScanResult result = scanSnapshot(snapshot);
    Runnable apply = () -> applyScanResult(key, seq, result);
    if (mc != null) {
        mc.execute(apply);
    } else {
        apply.run();
    }
};
        ExecutorService pool = this.threadPool;
        if (((Boolean) this.useThreading.get()).booleanValue() && pool != null && !(pool.isShutdown())) {
            pool.execute(task);
        } else {
            task.run();
        }
    }

    private BedrockHoleESP.ChunkSnapshot captureSnapshot(class_2818 chunk, int[] yLevels) {
        if ((mc == null) || (mc.field_1687 == null)) break label_1;
        if (chunk != null) break label_2;
        label_1:
        return null;
        label_2:
        int startX = chunk.method_12004().method_8326();
        int startZ = chunk.method_12004().method_8328();
        boolean[][] bedrock = new boolean[yLevels.length][256];
        class_2338.class_2339 mutable = new class_2338.class_2339();
        int yi = 0;
        while (yi < yLevels.length) {
            int y = yLevels[yi];
            int lx = 0;
            while (lx < 16) {
                int wx = (startX + lx);
                int lz = 0;
                while (lz < 16) {
                    int wz = (startZ + lz);
                    mutable.method_10103(wx, y, wz);
                    class_2680 state = mc.field_1687.method_8320(mutable);
                    bedrock[yi][packCell(lx, lz)] = isBedrock(state);
                    lz++;
                }
                lx++;
            }
            yi++;
        }
        return new BedrockHoleESP.ChunkSnapshot(startX, startZ, yLevels, bedrock);
    }

    private BedrockHoleESP.ScanResult scanSnapshot(BedrockHoleESP.ChunkSnapshot snapshot) {
        int[] yLevels = snapshot.yLevels;
        boolean[][] bedrock = snapshot.bedrock;
        boolean[][] processed = new boolean[yLevels.length][256];
        Map yToIndex = new HashMap((yLevels.length * 2));
        int i = 0;
        while (i < yLevels.length) {
            yToIndex.put(yLevels[i], i);
            i++;
        }
        Set allBlocks = new HashSet();
        List groups = new ArrayList();
        int minSize = Math.max(1, ((Integer) this.minVoidSize.get()).intValue());
        int yi = 0;
        while (yi < yLevels.length) {
            int cell = 0;
            while (cell < 256) {
                if (processed[yi][cell]) break label_34;
                if (bedrock[yi][cell]) {
                    break label_34;
                }
                Set group = floodFill(snapshot, yi, cell, yToIndex, processed);
                if (group.size() < minSize) {
                    break label_34;
                }
                if (!(isVoidEnclosed(snapshot, group, yToIndex))) {
                    break label_34;
                }
                long anchor = 9223372036854775807L;
                class_2338 first = null;
                Object var15 = group.iterator();
                while (var15.hasNext()) {
                    int code = ((Integer) var15.next()).intValue();
                    int layer = unpackLayer(code);
                    int packedCell = unpackCell(code);
                    int lx = unpackLocalX(packedCell);
                    int lz = unpackLocalZ(packedCell);
                    class_2338 pos = new class_2338((snapshot.startX + lx), yLevels[layer], (snapshot.startZ + lz));
                    allBlocks.add(pos);
                    long posLong = pos.method_10063();
                    if (posLong < anchor) {
                        anchor = posLong;
                        first = pos;
                    }
                }
                if ((first == null) || (anchor == 9223372036854775807L)) break label_34;
                groups.add(new BedrockHoleESP.VoidGroup(anchor, first, group.size()));
                label_34:
                cell++;
            }
            yi++;
        }
        return new BedrockHoleESP.ScanResult(allBlocks, groups);
    }

    private Set floodFill(BedrockHoleESP.ChunkSnapshot snapshot, int startLayer, int startCell, Map yToIndex, boolean[][] processed) {
        ArrayDeque queue = new ArrayDeque();
        Set group = new HashSet();
        queue.add(packCode(startLayer, startCell));
        while (!(queue.isEmpty()) && group.size() < 200) {
            int code = ((Integer) queue.pollFirst()).intValue();
            int layer = unpackLayer(code);
            int cell = unpackCell(code);
            if (processed[layer][cell]) {
                break label_3;
            }
            if (snapshot.bedrock[layer][cell]) {
                break label_3;
            }
            processed[layer][cell] = 1;
            group.add(code);
            int lx = unpackLocalX(cell);
            int lz = unpackLocalZ(cell);
            int y = snapshot.yLevels[layer];
            enqueueHorizontal(queue, layer, (lx + 1), lz);
            enqueueHorizontal(queue, layer, (lx - 1), lz);
            enqueueHorizontal(queue, layer, lx, (lz + 1));
            enqueueHorizontal(queue, layer, lx, (lz - 1));
            Integer upLayer = ((Integer) yToIndex.get((y + 1)));
            if (upLayer != null) {
                queue.addLast(packCode(upLayer.intValue(), cell));
            }
            Integer downLayer = ((Integer) yToIndex.get((y - 1)));
            if (downLayer != null) {
                queue.addLast(packCode(downLayer.intValue(), cell));
            }
        }
        return group;
    }

    private boolean isVoidEnclosed(BedrockHoleESP.ChunkSnapshot snapshot, Set group, Map yToIndex) {
        Object var4 = group.iterator();
        while (var4.hasNext()) {
            int code = ((Integer) var4.next()).intValue();
            int layer = unpackLayer(code);
            int cell = unpackCell(code);
            int lx = unpackLocalX(cell);
            int lz = unpackLocalZ(cell);
            int y = snapshot.yLevels[layer];
            if (!(checkHorizontalEnclosure(snapshot, group, layer, (lx + 1), lz))) return false;
            if (!(checkHorizontalEnclosure(snapshot, group, layer, (lx - 1), lz))) return false;
            if (!(checkHorizontalEnclosure(snapshot, group, layer, lx, (lz + 1)))) return false;
            if (!(checkHorizontalEnclosure(snapshot, group, layer, lx, (lz - 1)))) return false;
            Integer upLayer = ((Integer) yToIndex.get((y + 1)));
            if (upLayer != null) {
                int upCode = packCode(upLayer.intValue(), cell);
                if (!(group.contains(upCode)) && !(snapshot.bedrock[upLayer.intValue()][cell])) return false;
            }
            Integer downLayer = ((Integer) yToIndex.get((y - 1)));
            if (downLayer != null) {
                int downCode = packCode(downLayer.intValue(), cell);
                if (!(group.contains(downCode)) && !(snapshot.bedrock[downLayer.intValue()][cell])) return false;
            }
        }
        return true;
    }

    private boolean checkHorizontalEnclosure(BedrockHoleESP.ChunkSnapshot snapshot, Set group, int layer, int lx, int lz) {
        if ((((lx < 0) || (lx > 15)) || (lz < 0)) || (lz > 15)) {
            return true;
        }
        int cell = packCell(lx, lz);
        int code = packCode(layer, cell);
        if (group.contains(code)) return true;
        return snapshot.bedrock[layer][cell];
    }

    private void enqueueHorizontal(ArrayDeque queue, int layer, int lx, int lz) {
        if ((((lx < 0) || (lx > 15)) || (lz < 0)) || (lz > 15)) {
            return;
        }
        queue.addLast(packCode(layer, packCell(lx, lz)));
    }

    private void applyScanResult(long chunkKey, int seq, BedrockHoleESP.ScanResult result) {
        Integer latest = ((Integer) this.chunkScanSeq.get(chunkKey));
        if (latest == null) break label_2;
        if (latest.intValue() == seq) break label_3;
        label_2:
        return;
        label_3:
        Set newBlocks = result.blocks;
        Set oldBlocks = ((Set) this.chunkVoidBlocks.get(chunkKey));
        if (oldBlocks == null) {
        } else {
        }
        int oldSize = oldBlocks.size();
        int newSize = newBlocks.size();
        this.totalVoidBlocks.addAndGet((newSize - oldSize));
        if (newBlocks.isEmpty()) {
            this.chunkVoidBlocks.remove(chunkKey);
        } else {
            this.chunkVoidBlocks.put(chunkKey, newBlocks);
        }
        Set oldAnchors = ((Set) this.chunkAnchors.getOrDefault(chunkKey, Collections.emptySet()));
        Set newAnchors = new HashSet(result.groups.size());
        Object var12 = result.groups.iterator();
        while (var12.hasNext()) {
            BedrockHoleESP.VoidGroup group = ((BedrockHoleESP.VoidGroup) var12.next());
            newAnchors.add(group.anchor);
        }
        if (!(((Boolean) this.chatFeedback.get()).booleanValue())) break label_26;
        var12 = result.groups.iterator();
        while (var12.hasNext()) {
            group = ((BedrockHoleESP.VoidGroup) var12.next());
            if (oldAnchors.contains(group.anchor)) {
                break label_21;
            }
            if (this.knownAnchors.contains(group.anchor)) {
                break label_21;
            }
            sendVoidMessage(group);
        }
        label_26:
        if (!(oldAnchors.isEmpty())) {
            this.knownAnchors.removeAll(oldAnchors);
        }
        if (newAnchors.isEmpty()) {
            this.chunkAnchors.remove(chunkKey);
        } else {
            this.chunkAnchors.put(chunkKey, newAnchors);
            this.knownAnchors.addAll(newAnchors);
        }
    }

    private void removeChunkData(class_1923 pos) {
        long key = class_1923.method_8331(pos.field_9181, pos.field_9180);
        this.chunkScanSeq.remove(key);
        Set removed = ((Set) this.chunkVoidBlocks.remove(key));
        if (removed != null && !(removed.isEmpty())) {
            this.totalVoidBlocks.addAndGet(-(removed.size()));
        }
        Set anchors = ((Set) this.chunkAnchors.remove(key));
        if (anchors != null && !(anchors.isEmpty())) {
            this.knownAnchors.removeAll(anchors);
        }
    }

    private void sendVoidMessage(BedrockHoleESP.VoidGroup group) {
        if ((!(isInGame())) || (mc.field_1724 == null)) {
            return;
        }
        if (!(((Boolean) this.chatFeedback.get()).booleanValue())) return;
        long minute = (System.currentTimeMillis() / 60000L);
        if (minute != this.minuteWindow) {
            this.minuteWindow = minute;
            this.messagesInWindow = 0;
        }
        int limit = ((Integer) this.maxMessagesPerMinute.get()).intValue();
        if (limit > 0 && this.messagesInWindow >= limit) return;
        var tmp0 = new Object[2];
        tmp0[0] = group.size;
        tmp0[1] = group.firstPos.method_23854();
        String msg = String.format("\u00a7d[BedrockHoleESP]\u00a7b Void found: \u00a7f%d blocks \u00a77at \u00a7f%s", tmp0);
        mc.field_1724.method_7353(class_2561.method_43470(msg), false);
        this.messagesInWindow = (this.messagesInWindow + 1);
    }

    private String resolveDimension() {
        if ((mc == null) || (mc.field_1687 == null)) {
            return "";
        }
        return mc.field_1687.method_27983().method_29177().toString();
    }

    private static int[] getYLevelsForDimension(String dimension) {
        if (!(!"minecraft:overworld".equals(dimension))) return OVERWORLD_Y_LEVELS;
        if (!(!"minecraft:the_nether".equals(dimension))) {
            int[] levels = new int[(NETHER_FLOOR_Y_LEVELS.length + NETHER_ROOF_Y_LEVELS.length)];
            System.arraycopy(NETHER_FLOOR_Y_LEVELS, 0, levels, 0, NETHER_FLOOR_Y_LEVELS.length);
            System.arraycopy(NETHER_ROOF_Y_LEVELS, 0, levels, NETHER_FLOOR_Y_LEVELS.length, NETHER_ROOF_Y_LEVELS.length);
            return levels;
        }
        return new int[0];
    }

    private static boolean isBedrock(class_2680 state) {
        if (state == null) break label_1;
        return state.method_27852(class_2246.field_9987);
    }

    private static int packCell(int lx, int lz) {
        return ((lx << 4) | (lz & 15));
    }

    private static int unpackLocalX(int cell) {
        return ((cell >> 4) & 15);
    }

    private static int unpackLocalZ(int cell) {
        return (cell & 15);
    }

    private static int packCode(int layer, int cell) {
        return ((layer << 8) | (cell & 255));
    }

    private static int unpackLayer(int code) {
        return (code >>> 8);
    }

    private static int unpackCell(int code) {
        return (code & 255);
    }

    static {
        var tmp0 = new int[5];
        tmp0[0] = -64;
        tmp0[1] = -63;
        tmp0[2] = -62;
        tmp0[3] = -61;
        tmp0[4] = -60;
        OVERWORLD_Y_LEVELS = tmp0;
        var tmp1 = new int[5];
        tmp1[0] = 0;
        tmp1[1] = 1;
        tmp1[2] = 2;
        tmp1[3] = 3;
        tmp1[4] = 4;
        NETHER_FLOOR_Y_LEVELS = tmp1;
        var tmp2 = new int[5];
        tmp2[0] = 123;
        tmp2[1] = 124;
        tmp2[2] = 125;
        tmp2[3] = 126;
        tmp2[4] = 127;
        NETHER_ROOF_Y_LEVELS = tmp2;
    }


    record ScanResult(Set blocks, List groups) {

        private ScanResult(Set blocks, List groups) {
            this.blocks = blocks;
            this.groups = groups;
        }

        public Set blocks() {
            return this.blocks;
        }

        public List groups() {
            return this.groups;
        }

    }

    record VoidGroup(long anchor, class_2338 firstPos, int size) {

        private VoidGroup(long anchor, class_2338 firstPos, int size) {
            this.anchor = anchor;
            this.firstPos = firstPos;
            this.size = size;
        }

        public long anchor() {
            return this.anchor;
        }

        public class_2338 firstPos() {
            return this.firstPos;
        }

        public int size() {
            return this.size;
        }

    }

    enum ShapeMode {
        BOTH,
        OUTLINE,
        FILL;


        private ShapeMode() {
        }

    }

    record ChunkSnapshot(int startX, int startZ, int[] yLevels, boolean[][] bedrock) {

        private ChunkSnapshot(int startX, int startZ, int[] yLevels, boolean[][] bedrock) {
            this.startX = startX;
            this.startZ = startZ;
            this.yLevels = yLevels;
            this.bedrock = bedrock;
        }

        public int startX() {
            return this.startX;
        }

        public int startZ() {
            return this.startZ;
        }

        public int[] yLevels() {
            return this.yLevels;
        }

        public boolean[][] bedrock() {
            return this.bedrock;
        }

    }
}

package me.drake.viecheats.modules.donut;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2818;
import net.minecraft.class_2826;

public class SpawnersNotifier extends Module {
    private final BoolSetting showBox;
    private final BoolSetting showTracers;
    private final BoolSetting showColumn;
    private final ColorSetting boxColor;
    private final ColorSetting columnColor;
    private final IntSetting columnAlpha;
    private final IntSetting renderDist;
    private final IntSetting maxRender;
    private final Map spawnersByChunk;
    private volatile int totalSpawners;
    private int incrementalScanCursor;

    public SpawnersNotifier() {
        super("SpawnersNotifier", "Detect and render mob spawners", Category.DONUT_SMP);
        this.showBox = ((BoolSetting) addSetting(new BoolSetting("Box", "Render box around spawners", true)));
        this.showTracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracer lines to spawners", false)));
        this.showColumn = ((BoolSetting) addSetting(new BoolSetting("Column", "Draw 1x1 column from spawner to sky", true)));
        this.boxColor = ((ColorSetting) addSetting(new ColorSetting("BoxColor", "Spawner box color", -6946816)));
        this.columnColor = ((ColorSetting) addSetting(new ColorSetting("ColumnColor", "Column color (top half)", -16711936)));
        this.columnAlpha = ((IntSetting) addSetting(new IntSetting("ColumnAlpha", "Column opacity", 80, 5, 255)));
        this.renderDist = ((IntSetting) addSetting(new IntSetting("RenderDist", "Render distance (chunks)", 32, 2, 32)));
        this.maxRender = ((IntSetting) addSetting(new IntSetting("MaxRender", "Max rendered per frame", 2048, 16, 2048)));
        this.spawnersByChunk = new ConcurrentHashMap();
        this.totalSpawners = 0;
        this.incrementalScanCursor = 0;
        this.renderDist.setVisibleInGui(false);
        this.maxRender.setVisibleInGui(false);
    }

    public void onEnable() {
        super.onEnable();
        this.spawnersByChunk.clear();
        this.totalSpawners = 0;
        this.incrementalScanCursor = 0;
        if (isInGame()) {
            scanNearbyChunksFull();
        }
    }

    public void onDisable() {
        super.onDisable();
        this.spawnersByChunk.clear();
        this.totalSpawners = 0;
        this.incrementalScanCursor = 0;
    }

    public String getInfo() {
        return (this.totalSpawners + " spawners");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!(isInGame())) return;
        if (e.isLoaded()) {
            scanChunk(e.getChunkPos(), false);
        } else {
            this.spawnersByChunk.remove(pack(e.getChunkPos()));
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if ((e.getPhase() != TickEvent.Phase.PRE) || (!(isInGame()))) {
            return;
        }
        scanNearbyIncremental();
        cleanupOutOfRangeChunks();
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (e.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (this.spawnersByChunk.isEmpty()) return;
        int boxCol = ((Integer) this.boxColor.get()).intValue();
        int colCol = ((Integer) this.columnColor.get()).intValue();
        int colA = ((Integer) this.columnAlpha.get()).intValue();
        float td = e.getTickDelta();
        int cap = ((Integer) this.maxRender.get()).intValue();
        int chunkRadius = ((Integer) this.renderDist.get()).intValue();
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        double px = mc.field_1724.method_23317();
        double py = mc.field_1724.method_23318();
        double pz = mc.field_1724.method_23321();
        double maxDist = (((double) (chunkRadius + 1)) * 16.0);
        maxDist = (maxDist * maxDist);
        int rendered = 0;
        Object var19 = this.spawnersByChunk.entrySet().iterator();
        while (var19.hasNext()) {
            Map.Entry entry = ((Map.Entry) var19.next());
            class_1923 cp = unpack(((Long) entry.getKey()).longValue());
            if (Math.abs((cp.field_9181 - pcx)) > (chunkRadius + 1)) break label_18;
            if (Math.abs((cp.field_9180 - pcz)) > (chunkRadius + 1)) {
                break label_18;
            }
            List spawners = ((List) entry.getValue());
            if (spawners == null) break label_18;
            if (spawners.isEmpty()) {
                break label_18;
            }
            Object var23 = spawners.iterator();
            while (var23.hasNext()) {
                class_2338 pos = ((class_2338) var23.next());
                if (rendered >= cap) return;
                rendered++;
                double sx = (((double) pos.method_10263()) + 0.5);
                double sy = (((double) pos.method_10264()) + 0.5);
                double sz = (((double) pos.method_10260()) + 0.5);
                double distSq = ((((sx - px) * (sx - px)) + ((sy - py) * (sy - py))) + ((sz - pz) * (sz - pz)));
                if (distSq > maxDist) {
                    break label_24;
                }
                if (((Boolean) this.showBox.get()).booleanValue()) {
                    class_238 box = new class_238(pos);
                    RenderUtil.drawBox(box, boxCol);
                    RenderUtil.drawFilledBox(box, ((boxCol & 16777215) | 1342177280));
                }
                if (((Boolean) this.showTracers.get()).booleanValue()) {
                    RenderUtil.drawTracerToPos(sx, sy, sz, boxCol, td);
                }
                if (((Boolean) this.showColumn.get()).booleanValue()) {
                    int worldTop = mc.field_1687.method_31600();
                    double cx = (((double) pos.method_10263()) + 0.5);
                    double cz = (((double) pos.method_10260()) + 0.5);
                    double spawnerTop = (((double) pos.method_10264()) + 1.0);
                    class_238 column = new class_238((cx - 0.05), spawnerTop, (cz - 0.05), (cx + 0.05), ((double) worldTop), (cz + 0.05));
                    RenderUtil.drawFilledBox(column, ((colCol & 16777215) | (colA << 24)));
                    RenderUtil.drawBox(column, colCol);
                }
            }
        }
    }

    private void scanNearbyIncremental() {
        int rd = ((Integer) this.renderDist.get()).intValue();
        if (rd <= 1) return;
        int budget = 4;
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        int diameter = ((rd * 2) + 1);
        int total = (diameter * diameter);
        if (total <= 0) return;
        if (this.incrementalScanCursor >= total) {
            this.incrementalScanCursor = 0;
        }
        int scanned = 0;
        int attempts = 0;
        while (attempts < total && scanned < budget) {
            int index = ((this.incrementalScanCursor + attempts) % total);
            int dx = ((index % diameter) - rd);
            int dz = ((index / diameter) - rd);
            attempts++;
            int cx = (pcx + dx);
            int cz = (pcz + dz);
            long key = pack(new class_1923(cx, cz));
            List existing = ((List) this.spawnersByChunk.get(key));
            if (existing != null) {
                if (existing.isEmpty()) {
                }
            } else {
            }
            boolean needsRescan = false;
            scanChunk(new class_1923(cx, cz), needsRescan);
            scanned++;
        }
        this.incrementalScanCursor = ((this.incrementalScanCursor + scanned) % total);
    }

    private void scanNearbyChunksFull() {
        int r = ((Integer) this.renderDist.get()).intValue();
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        int dx = -(r);
        while (dx <= r) {
            int dz = -(r);
            while (dz <= r) {
                scanChunk(new class_1923((pcx + dx), (pcz + dz)), true);
                dz++;
            }
            dx++;
        }
    }

    private boolean scanChunk(class_1923 cpos, boolean force) {
        long key = pack(cpos);
        if (!force) {
            List existing = ((List) this.spawnersByChunk.get(key));
            if (existing != null && !(existing.isEmpty())) return false;
        }
        class_2818 chunk = mc.field_1687.method_2935().method_12126(cpos.field_9181, cpos.field_9180, false);
        if (chunk == null) return false;
        List found = findSpawners(chunk);
        List prev = ((List) this.spawnersByChunk.get(key));
        if (prev != null) {
            if (!(prev.equals(found))) {
            }
        } else {
        }
        boolean changed = false;
        if (!(found.isEmpty())) {
            this.spawnersByChunk.put(key, found);
            break label_16;
        }
        if (!changed) break label_16;
        this.spawnersByChunk.remove(key);
        label_16:
        if (!(!changed)) {
            updateTotalCount();
        }
        return changed;
    }

    private List findSpawners(class_2818 chunk) {
        List out = new ArrayList();
        class_1923 cpos = chunk.method_12004();
        class_2826[] sections = chunk.method_12006();
        int bottomY = mc.field_1687.method_31607();
        int i = 0;
        while (i < sections.length) {
            class_2826 sec = sections[i];
            if (sec == null) break label_21;
            if (sec.method_38292()) {
                break label_21;
            }
            if (!(sec.method_19523((state) -> state.method_27852(class_2246.field_10260)))) {
                break label_21;
            }
            int secBaseY = (bottomY + (i << 4));
            int x = 0;
            while (x < 16) {
                int y = 0;
                while (y < 16) {
                    int z = 0;
                    while (z < 16) {
                        if (!(sec.method_12254(x, y, z).method_27852(class_2246.field_10260))) {
                        } else {
                            out.add(new class_2338((cpos.method_8326() + x), (secBaseY + y), (cpos.method_8328() + z)));
                        }
                        z++;
                    }
                    y++;
                }
                x++;
            }
            label_21:
            i++;
        }
        return out;
    }

    private void cleanupOutOfRangeChunks() {
        int rd = (((Integer) this.renderDist.get()).intValue() + 1);
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        var tmp0 = new boolean[1];
        tmp0[0] = 0;
        boolean[] changed = tmp0;
        this.spawnersByChunk.keySet().removeIf((key) -> {
    class_1923 cp = unpack(key.longValue());
    if (Math.abs((cp.field_9181 - pcx)) <= rd) {
        if (Math.abs((cp.field_9180 - pcz)) > rd) {
        }
    } else {
    }
    boolean outOfRange = false;
    if (!(!outOfRange)) {
        changed[0] = 1;
    }
    return outOfRange;
});
        if (changed[0]) {
            updateTotalCount();
        }
    }

    private void updateTotalCount() {
        int total = 0;
        Object var2 = this.spawnersByChunk.values().iterator();
        while (var2.hasNext()) {
            List list = ((List) var2.next());
            if (list != null) {
                total = (total + list.size());
            }
        }
        this.totalSpawners = total;
    }

    private static long pack(class_1923 pos) {
        return class_1923.method_8331(pos.field_9181, pos.field_9180);
    }

    private static class_1923 unpack(long key) {
        return new class_1923(class_1923.method_8325(key), class_1923.method_8332(key));
    }

}

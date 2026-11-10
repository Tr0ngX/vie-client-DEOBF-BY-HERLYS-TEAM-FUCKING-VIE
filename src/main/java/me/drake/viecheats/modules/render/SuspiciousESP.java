package me.drake.viecheats.modules.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3989;

public class SuspiciousESP extends Module {
    private static final int COLOR_ACTIVE_PISTON = -16711936;
    private static final int COLOR_WANDERING = -16711681;
    private static final int COLOR_ACTIVE_BLOCK = -256;
    private static final long SCAN_INTERVAL_MS = 150L;
    private static final int MIN_RENDER_CHUNK_RADIUS = 2;
    private static final ExecutorService SCAN_EXECUTOR;
    private final BoolSetting tracers;
    private final BoolSetting flagWandering;
    private final BoolSetting flagPistons;
    private final BoolSetting flagActiveBlocks;
    private final IntSetting alpha;
    private volatile List blockMarkers;
    private final AtomicBoolean scanRunning;
    private long lastScanMs;

    public SuspiciousESP() {
        super("SuspiciousESP", "Flags suspicious base activity", Category.RENDER);
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracers to suspicious targets", true)));
        this.flagWandering = ((BoolSetting) addSetting(new BoolSetting("FlagWandering", "Flag wandering trader", true)));
        this.flagPistons = ((BoolSetting) addSetting(new BoolSetting("FlagPistons", "Flag active piston/sticky piston", true)));
        this.flagActiveBlocks = ((BoolSetting) addSetting(new BoolSetting("FlagActiveBlocks", "Flag active observer/dispenser/dropper/lamp", true)));
        this.alpha = ((IntSetting) addSetting(new IntSetting("Alpha", "Overlay opacity", 100, 0, 255)));
        this.blockMarkers = Collections.emptyList();
        this.scanRunning = new AtomicBoolean(false);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if ((event.getPhase() != TickEvent.Phase.PRE) || (!(isInGame()))) {
            return;
        }
        if (!(((Boolean) this.flagPistons.get()).booleanValue()) && !(((Boolean) this.flagActiveBlocks.get()).booleanValue())) {
            this.blockMarkers = Collections.emptyList();
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - this.lastScanMs) < 150L) return;
        this.lastScanMs = now;
        scheduleBlockScan();
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (e.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        int fillAlpha = clamp(((Integer) this.alpha.get()).intValue(), 0, 255);
        float tickDelta = e.getTickDelta();
        List markers = mc.field_1687.method_18112().iterator();
        while (markers.hasNext()) {
            class_1297 ent = ((class_1297) markers.next());
            if (!(((Boolean) this.flagWandering.get()).booleanValue())) break label_5;
            if (!(isWanderingTrader(ent))) {
                break label_5;
            }
            int tracerColor = withAlpha(-16711681, 255);
            class_238 interpolated = RenderUtil.getInterpolatedBox(ent, tickDelta);
            RenderUtil.drawFilledBox(interpolated, withAlpha(-16711681, fillAlpha));
            if (((Boolean) this.tracers.get()).booleanValue()) {
                RenderUtil.drawTracer(ent, tracerColor, tickDelta);
            }
        }
        markers = this.blockMarkers;
        ent = markers.iterator();
        while (ent.hasNext()) {
            SuspiciousESP.BlockMarker marker = ((SuspiciousESP.BlockMarker) ent.next());
            tracerColor = withAlpha(marker.color, 255);
            RenderUtil.drawFilledBox(new class_238(marker.pos), withAlpha(marker.color, fillAlpha));
            if (((Boolean) this.tracers.get()).booleanValue()) {
                RenderUtil.drawTracerToPos((((double) marker.pos.method_10263()) + 0.5), (((double) marker.pos.method_10264()) + 0.5), (((double) marker.pos.method_10260()) + 0.5), tracerColor, tickDelta);
            }
        }
    }

    private void scheduleBlockScan() {
        if (!(this.scanRunning.compareAndSet(false, true))) return;
        SCAN_EXECUTOR.execute(() -> {
    if (mc.field_1687 != null) {
        if (mc.field_1724 == null) {
        }
        this.blockMarkers = Collections.emptyList();
        this.scanRunning.set(false);
        return;
    }
    this.blockMarkers = scanSuspiciousBlocksSnapshot();
    this.scanRunning.set(false);
    break label_9;
    Object var1 = null;
    this.scanRunning.set(false);
    throw var1;
    label_9:
});
    }

    private List scanSuspiciousBlocksSnapshot() {
        List found = new ArrayList();
        int r = resolveAutoRangeBlocks();
        int maxDistSq = (r * r);
        class_2338 playerPos = mc.field_1724.method_24515();
        class_1923 playerChunk = mc.field_1724.method_31476();
        int chunkRadius = Math.max(1, ((r + 15) / 16));
        int worldBottomY = mc.field_1687.method_31607();
        int worldTopY = mc.field_1687.method_31600();
        boolean checkPiston = ((Boolean) this.flagPistons.get()).booleanValue();
        boolean checkActiveBlocks = ((Boolean) this.flagActiveBlocks.get()).booleanValue();
        class_2338.class_2339 pos = new class_2338.class_2339();
        int cx = (playerChunk.field_9181 - chunkRadius);
        while (cx <= (playerChunk.field_9181 + chunkRadius)) {
            int cz = (playerChunk.field_9180 - chunkRadius);
            while (cz <= (playerChunk.field_9180 + chunkRadius)) {
                class_2818 chunk = mc.field_1687.method_2935().method_21730(cx, cz);
                if (chunk == null) {
                    break label_56;
                }
                class_2826[] sections = chunk.method_12006();
                int startX = chunk.method_12004().method_8326();
                int startZ = chunk.method_12004().method_8328();
                int baseBottomY = chunk.method_31607();
                int sectionIndex = 0;
                while (sectionIndex < sections.length) {
                    class_2826 section = sections[sectionIndex];
                    if (section == null) break label_55;
                    if (section.method_38292()) {
                        break label_55;
                    }
                    int sectionMinY = (baseBottomY + (sectionIndex * 16));
                    int sectionMaxY = (sectionMinY + 15);
                    if (sectionMaxY < worldBottomY) break label_55;
                    if (sectionMinY > worldTopY) {
                        break label_55;
                    }
                    int lx = 0;
                    while (lx < 16) {
                        int wx = (startX + lx);
                        int dx = (wx - playerPos.method_10263());
                        int dxSq = (dx * dx);
                        if (dxSq > maxDistSq) {
                            break label_54;
                        }
                        int lz = 0;
                        while (lz < 16) {
                            int wz = (startZ + lz);
                            int dz = (wz - playerPos.method_10260());
                            if ((dxSq + (dz * dz)) > maxDistSq) {
                                break label_53;
                            }
                            int ly = 0;
                            while (ly < 16) {
                                int wy = (sectionMinY + ly);
                                if (wy < worldBottomY) break label_52;
                                if (wy > worldTopY) {
                                    break label_52;
                                }
                                class_2680 state = section.method_12254(lx, ly, lz);
                                if (state.method_26215()) {
                                    break label_52;
                                }
                                if (!(!checkPiston) && isActivePiston(state)) {
                                    pos.method_10103(wx, wy, wz);
                                    found.add(new SuspiciousESP.BlockMarker(pos.method_10062(), -16711936));
                                    break label_52;
                                }
                                if ((!checkActiveBlocks) || (!(isActiveAutomationBlock(state)))) break label_52;
                                pos.method_10103(wx, wy, wz);
                                found.add(new SuspiciousESP.BlockMarker(pos.method_10062(), -256));
                                label_52:
                                ly++;
                            }
                            label_53:
                            lz++;
                        }
                        label_54:
                        lx++;
                    }
                    label_55:
                    sectionIndex++;
                }
                label_56:
                cz++;
            }
            cx++;
        }
        return found;
    }

    private int resolveAutoRangeBlocks() {
        if ((mc == null) || (mc.field_1690 == null)) {
            return 64;
        }
        int chunkRadius = Math.max(2, mc.field_1690.method_38521());
        return (chunkRadius * 16);
    }

    private static boolean isActivePiston(class_2680 state) {
        if (!(state.method_27852(class_2246.field_10560)) && !(state.method_27852(class_2246.field_10615))) return false;
        if (!(state.method_28498(class_2741.field_12552))) break label_2;
        return ((Boolean) state.method_11654(class_2741.field_12552)).booleanValue();
    }

    private static boolean isActiveAutomationBlock(class_2680 state) {
        if (state.method_27852(class_2246.field_10282)) {
            if (!(state.method_28498(class_2741.field_12484))) break label_2;
            return ((Boolean) state.method_11654(class_2741.field_12484)).booleanValue();
        }
        if (!(state.method_27852(class_2246.field_10200))) {
            if (state.method_27852(class_2246.field_10228)) {
            }
            if (!(state.method_28498(class_2741.field_12522))) break label_6;
            return ((Boolean) state.method_11654(class_2741.field_12522)).booleanValue();
        }
        if (state.method_27852(class_2246.field_10524)) {
            if (!(state.method_28498(class_2741.field_12548))) break label_10;
            return ((Boolean) state.method_11654(class_2741.field_12548)).booleanValue();
        }
        return false;
    }

    private static boolean isWanderingTrader(class_1297 entity) {
        if ((entity instanceof class_3989)) return true;
        return entity.getClass().getSimpleName().contains("WanderingTrader");
    }

    private static int withAlpha(int rgbColor, int alpha) {
        return ((clamp(alpha, 0, 255) << 24) | (rgbColor & 16777215));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static {
        SCAN_EXECUTOR = Executors.newSingleThreadExecutor((r) -> {
    Thread t = new Thread(r, "viecheats-suspicious-scan");
    t.setDaemon(true);
    return t;
});
    }


    record BlockMarker(class_2338 pos, int color) {

        private BlockMarker(class_2338 pos, int color) {
            this.pos = pos;
            this.color = color;
        }

        public class_2338 pos() {
            return this.pos;
        }

        public int color() {
            return this.color;
        }

    }
}

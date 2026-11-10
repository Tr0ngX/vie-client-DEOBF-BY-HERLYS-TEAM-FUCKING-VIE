package me.drake.viecheats.modules.donut;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;

public class AmethystActivityFinder extends Module {
    private static final int PLATE_Y = 63;
    private static final long CHUNK_RESCAN_MS = 3000L;
    private static final long SCAN_TICK_MS = 200L;
    private static final int MAX_CHUNKS_PER_SCAN = 6;
    private final IntSetting alpha;
    private final IntSetting simDist;
    private final IntSetting sensitivity;
    private final BoolSetting kelp;
    private final BoolSetting caveVines;
    private final BoolSetting vines;
    private final BoolSetting bamboo;
    private final BoolSetting beeNest;
    private final BoolSetting rotatedDeepslate;
    private final BoolSetting tracers;
    private static final Set SUS_CHUNKS;
    private static final Map LAST_SCAN_AT;
    private static final AtomicBoolean scanRunning;
    private static long lastScheduleMs;

    public AmethystActivityFinder() {
        super("AmethystActivityFinder", "Find player activity near amethyst", Category.DONUT_SMP);
        this.alpha = ((IntSetting) addSetting(new IntSetting("Alpha", "Opacity", 100, 20, 255)));
        this.simDist = ((IntSetting) addSetting(new IntSetting("SimDistance", "Simulation distance", 4, 1, 16)));
        this.sensitivity = ((IntSetting) addSetting(new IntSetting("Sensitivity", "Detection sensitivity", 5, 1, 10)));
        this.kelp = ((BoolSetting) addSetting(new BoolSetting("Kelp", "Detect kelp farms", true)));
        this.caveVines = ((BoolSetting) addSetting(new BoolSetting("Cave Vines", "Detect cave vines", true)));
        this.vines = ((BoolSetting) addSetting(new BoolSetting("Vines", "Detect vines", true)));
        this.bamboo = ((BoolSetting) addSetting(new BoolSetting("Bamboo", "Detect bamboo", false)));
        this.beeNest = ((BoolSetting) addSetting(new BoolSetting("Bee Nest", "Detect bee nest/hive", true)));
        this.rotatedDeepslate = ((BoolSetting) addSetting(new BoolSetting("Rotated Deepslate", "Detect rotated deepslate", false)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracer lines", false)));
    }

    public void onDisable() {
        SUS_CHUNKS.clear();
        LAST_SCAN_AT.clear();
        scanRunning.set(false);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if ((e.getPhase() != TickEvent.Phase.PRE) || (!(isInGame()))) {
            return;
        }
        scheduleScan();
    }

    @EventHandler
    public void onRender(RenderEvent ev) {
        if (ev.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (SUS_CHUNKS.isEmpty()) return;
        class_1923 playerChunk = mc.field_1724.method_31476();
        int sd = ((Integer) this.simDist.get()).intValue();
        SUS_CHUNKS.removeIf((pos) -> {
    if (Math.abs((pos.field_9181 - playerChunk.field_9181)) <= (sd + 1)) {
        if (Math.abs((pos.field_9180 - playerChunk.field_9180)) > (sd + 1)) {
        }
    } else {
    }
    return false;
});
        LAST_SCAN_AT.keySet().removeIf((pos) -> {
    if (Math.abs((pos.field_9181 - playerChunk.field_9181)) <= (sd + 2)) {
        if (Math.abs((pos.field_9180 - playerChunk.field_9180)) > (sd + 2)) {
        }
    } else {
    }
    return false;
});
        float td = ev.getTickDelta();
        int a = Math.max(50, Math.min(170, ((Integer) this.alpha.get()).intValue()));
        int fillColor = argb(a, 255, 30, 30);
        int rimColor = argb(Math.min(255, (a + 80)), 255, 100, 100);
        Object var8 = SUS_CHUNKS.iterator();
        while (var8.hasNext()) {
            class_1923 cp = ((class_1923) var8.next());
            double x1 = ((double) cp.method_8326());
            double z1 = ((double) cp.method_8328());
            double x2 = (((double) cp.method_8326()) + 16.0);
            double z2 = (((double) cp.method_8328()) + 16.0);
            RenderUtil.drawFlatPlane(x1, z1, x2, z2, 63.0, fillColor);
            RenderUtil.drawFlatPlaneBorder(x1, z1, x2, z2, 63.0, rimColor);
            if (((Boolean) this.tracers.get()).booleanValue()) {
                RenderUtil.drawTracerToPos((((double) cp.method_8326()) + 8.0), 63.0, (((double) cp.method_8328()) + 8.0), rimColor, td);
            }
        }
    }

    private void scheduleScan() {
        long now = System.currentTimeMillis();
        if (scanRunning.get()) return;
        if ((now - lastScheduleMs) < 200L) return;
        lastScheduleMs = now;
        int sd = ((Integer) this.simDist.get()).intValue();
        class_1923 playerChunk = mc.field_1724.method_31476();
        List candidates = new ArrayList();
        int cx = (playerChunk.field_9181 - sd);
        while (cx <= (playerChunk.field_9181 + sd)) {
            int cz = (playerChunk.field_9180 - sd);
            while (cz <= (playerChunk.field_9180 + sd)) {
                if (!(mc.field_1687.method_8393(cx, cz))) {
                    break label_16;
                }
                class_1923 cp = new class_1923(cx, cz);
                long last = ((Long) LAST_SCAN_AT.getOrDefault(cp, 0L)).longValue();
                if (last != 0L) {
                    if ((now - last) < 3000L) break label_16;
                }
                candidates.add(cp);
                label_16:
                cz++;
            }
            cx++;
        }
        if (candidates.isEmpty()) return;
        scanRunning.set(true);
        Thread t = new Thread(() -> {
    int thresh = (Math.max(1, ((Integer) this.sensitivity.get()).intValue()) * 10);
    int minY = -64;
    int maxY = 128;
    int scanCount = 0;
    Object var6 = candidates.iterator();
    while (var6.hasNext()) {
        class_1923 cp = ((class_1923) var6.next());
        if (scanCount >= 6) {
            break label_17;
        }
        if (mc.field_1687 == null) break label_5;
        if (!(mc.field_1687.method_8393(cp.field_9181, cp.field_9180))) {
            break label_5;
        }
        class_2818 chunk = mc.field_1687.method_8497(cp.field_9181, cp.field_9180);
        int score = scoreChunk(chunk, minY, maxY);
        if (score >= thresh) {
            SUS_CHUNKS.add(cp);
            notify(cp);
        } else {
            SUS_CHUNKS.remove(cp);
        }
        LAST_SCAN_AT.put(cp, System.currentTimeMillis());
        scanCount++;
    }
    label_17:
    scanRunning.set(false);
    break label_22;
    Object var10 = null;
    scanRunning.set(false);
    throw var10;
    label_22:
}, "viecheats-amethyst-scan");
        t.setDaemon(true);
        t.start();
    }

    private int scoreChunk(class_2818 chunk, int minY, int maxY) {
        int score = 0;
        int sx = chunk.method_12004().method_8326();
        int sz = chunk.method_12004().method_8328();
        int exposedAmethyst = 0;
        class_2338.class_2339 pos = new class_2338.class_2339();
        int x = sx;
        while (x < (sx + 16)) {
            int z = sz;
            while (z < (sz + 16)) {
                int y = Math.max(minY, -64);
                while (y <= Math.min(maxY, 128)) {
                    pos.method_10103(x, y, z);
                    class_2680 state = chunk.method_8320(pos);
                    if (state.method_26215()) {
                        break label_30;
                    }
                    if (((Boolean) this.kelp.get()).booleanValue() && state.method_27852(class_2246.field_9993) && chunk.method_8320(pos.method_10084()).method_26215()) {
                        score += 3;
                    }
                    if (!(((Boolean) this.caveVines.get()).booleanValue())) break label_18;
                    if (!(state.method_27852(class_2246.field_28675))) {
                        if (!(state.method_27852(class_2246.field_28676))) break label_18;
                    }
                    score += 2;
                    label_18:
                    if (!(((Boolean) this.vines.get()).booleanValue())) break label_22;
                    if (!(state.method_27852(class_2246.field_10597)) && !(state.method_27852(class_2246.field_22123)) && !(state.method_27852(class_2246.field_22124)) && !(state.method_27852(class_2246.field_23078))) {
                        if (!(state.method_27852(class_2246.field_23079))) break label_22;
                    }
                    score++;
                    label_22:
                    if (((Boolean) this.bamboo.get()).booleanValue() && state.method_27852(class_2246.field_10211)) {
                        score++;
                    }
                    if (!(((Boolean) this.beeNest.get()).booleanValue())) break label_26;
                    if (!(state.method_27852(class_2246.field_20421))) {
                        if (!(state.method_27852(class_2246.field_20422))) break label_26;
                    }
                    score += 6;
                    label_26:
                    if (((Boolean) this.rotatedDeepslate.get()).booleanValue() && isRotatedDeepslate(state)) {
                        score += 3;
                    }
                    if (!(isExposedAmethyst(state))) break label_30;
                    exposedAmethyst++;
                    label_30:
                    y++;
                }
                z++;
            }
            x++;
        }
        score = (score + (exposedAmethyst * Math.max(1, ((Integer) this.sensitivity.get()).intValue())));
        return score;
    }

    private boolean isExposedAmethyst(class_2680 state) {
        if (!(state.method_27852(class_2246.field_27161))) {
            if (state.method_27852(class_2246.field_27162)) {
            }
        } else {
        }
        return false;
    }

    private boolean isRotatedDeepslate(class_2680 state) {
        if (!(state.method_27852(class_2246.field_28888)) && !(state.method_27852(class_2246.field_28900))) {
            if (state.method_27852(class_2246.field_28896)) {
            }
        } else {
        }
        boolean isType = false;
        if (!isType) return false;
        if (state.method_28498(class_2741.field_12496)) return state.method_11654(class_2741.field_12496) != class_2350.class_2351.field_11052;
        return state.method_28498(class_2741.field_12529);
    }

    private void notify(class_1923 cp) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance != null && instance.getHudManager() != null) {
            instance.getHudManager().addNotification(("Amethyst Activity at " + cp.field_9181 + ", " + cp.field_9180), -5618433);
        }
    }

    private int argb(int a, int r, int g, int b) {
        a = Math.max(0, Math.min(255, a));
        return ((((a << 24) | ((r & 255) << 16)) | ((g & 255) << 8)) | (b & 255));
    }

    static {
        SUS_CHUNKS = ConcurrentHashMap.newKeySet();
        LAST_SCAN_AT = new ConcurrentHashMap();
        scanRunning = new AtomicBoolean(false);
    }

}

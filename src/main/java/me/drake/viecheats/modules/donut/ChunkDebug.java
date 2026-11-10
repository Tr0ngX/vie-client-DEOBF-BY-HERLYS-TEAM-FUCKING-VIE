package me.drake.viecheats.modules.donut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.Render3DEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1923;
import net.minecraft.class_2561;

public final class ChunkDebug extends Module {
    private static final int SHEET_Y = 63;
    private static final long WARMUP_MS = 3000L;
    private final IntSetting suspiciousTime;
    private final IntSetting sensitivity;
    private final IntSetting opacity;
    private final EnumSetting renderMode;
    private final BoolSetting tracers;
    private final BoolSetting chatNotification;
    private final Map activityAccumulator;
    private final Map lastActivityTime;
    private final Set notifiedChunks;
    private final List renderChunks;
    private String lastWorldId;
    private long warmupUntilMs;

    public ChunkDebug() {
        super("ChunkDebug", "Highlight cac chunk suspicious (co update)", Category.DONUT_SMP);
        this.suspiciousTime = ((IntSetting) addSetting(new IntSetting("Suspicious", "Thoi gian coi la suspicious (giay)", 10, 1, 60)));
        this.sensitivity = ((IntSetting) addSetting(new IntSetting("Sensitivity", "Do nhay (1=it, 100=nhieu)", 15, 1, 100)));
        this.opacity = ((IntSetting) addSetting(new IntSetting("Opacity", "Do duc cua sheet (0-255)", 120, 0, 255)));
        this.renderMode = ((EnumSetting) addSetting(new EnumSetting("RenderMode", "Kieu ve", RenderMode.SHEET)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Ve duong noi den chunk", true)));
        this.chatNotification = ((BoolSetting) addSetting(new BoolSetting("ChatNotif", "Thong bao chat khi phat hien", true)));
        this.activityAccumulator = new HashMap();
        this.lastActivityTime = new HashMap();
        this.notifiedChunks = new HashSet();
        this.renderChunks = new ArrayList();
        this.lastWorldId = "";
        this.warmupUntilMs = 0L;
    }

    public void onEnable() {
        super.onEnable();
        clearState();
        this.warmupUntilMs = (System.currentTimeMillis() + 3000L);
        this.lastWorldId = currentWorldId();
    }

    public void onDisable() {
        ActiveChunkActivityStore.clear();
        clearState();
        super.onDisable();
    }

    public String getInfo() {
        return String.valueOf(this.renderChunks.size());
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        String worldNow = currentWorldId();
        if (!(worldNow.equals(this.lastWorldId))) {
            ActiveChunkActivityStore.clear();
            clearState();
            this.warmupUntilMs = (System.currentTimeMillis() + 3000L);
            this.lastWorldId = worldNow;
        }
        long now = System.currentTimeMillis();
        if (now < this.warmupUntilMs) return;
        if (mc.field_1724 != null) {
        } else {
        }
        ActiveChunkActivityStore.setPlayerChunk(-9223372036854775808L);
        long cutoff = (now - (((long) ((Integer) this.suspiciousTime.get()).intValue()) * 1000L));
        int threshHigh = ((Integer) this.sensitivity.get()).intValue();
        boolean notify = ((Boolean) this.chatNotification.get()).booleanValue();
        Map snapshot = ActiveChunkActivityStore.drain();
        Object var10 = snapshot.entrySet().iterator();
        while (var10.hasNext()) {
            Map.Entry e = ((Map.Entry) var10.next());
            int activity = ((ActiveChunkActivityStore.ActivityEntry) e.getValue()).total();
            if (activity > 0) {
                this.activityAccumulator.merge(((Long) e.getKey()), activity, Integer::sum);
                this.lastActivityTime.put(((Long) e.getKey()), now);
            }
        }
        this.activityAccumulator.entrySet().removeIf(() -> {
    Long last = ((Long) this.lastActivityTime.get(entry.getKey()));
    if (last != null) {
        if (last.longValue() < cutoff) {
        }
    } else {
    }
    return false;
});
        this.lastActivityTime.entrySet().removeIf((entry) -> ((Long) entry.getValue()).longValue() < cutoff);
        this.renderChunks.clear();
        var10 = this.activityAccumulator.entrySet().iterator();
        while (var10.hasNext()) {
            e = ((Map.Entry) var10.next());
            if (((Integer) e.getValue()).intValue() < threshHigh) {
                break label_28;
            }
            class_1923 pos = unpack(((Long) e.getKey()).longValue());
            Long lastTime = ((Long) this.lastActivityTime.get(e.getKey()));
            if (lastTime != null) {
            } else {
            }
            long lastMs = now;
            this.renderChunks.add(new ActiveChunkData(pos, lastMs, ((Integer) e.getValue()).intValue()));
            if (!(!notify) && !(this.notifiedChunks.contains(e.getKey()))) {
                sendChat(pos);
                this.notifiedChunks.add(((Long) e.getKey()));
            }
        }
        this.notifiedChunks.removeIf(() -> {
    Long last = ((Long) this.lastActivityTime.get(key));
    if (last != null) {
        if (last.longValue() < cutoff) {
        }
    } else {
    }
    return false;
});
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if ((this.renderChunks.isEmpty()) || (!(isInGame()))) break label_1;
        if (mc.field_1724 != null) break label_2;
        label_1:
        return;
        label_2:
        RenderMode mode = ((RenderMode) this.renderMode.get());
        int sheetAlpha = Math.max(0, Math.min(255, ((Integer) this.opacity.get()).intValue()));
        int fillColor = ((sheetAlpha << 24) | 16720418);
        int outlineColor = ((Math.min(255, (sheetAlpha + 80)) << 24) | 16737894);
        int tracerColor = ((Math.min(255, (sheetAlpha + 60)) << 24) | 16729156);
        float tickDelta = event.getTickDelta();
        int worldMinY = mc.field_1687.method_31607();
        int worldMaxY = mc.field_1687.method_31600();
        double playerX = mc.field_1724.method_23317();
        double playerY = (mc.field_1724.method_23318() + ((double) mc.field_1724.method_5751()));
        double playerZ = mc.field_1724.method_23321();
        Object var16 = this.renderChunks.iterator();
        while (var16.hasNext()) {
            ActiveChunkData data = ((ActiveChunkData) var16.next());
            class_1923 pos = data.chunkPos();
            double x1 = ((double) pos.method_8326());
            double z1 = ((double) pos.method_8328());
            double centerX = (x1 + 8.0);
            double centerZ = (z1 + 8.0);
            if (mode != RenderMode.SHEET) {
                if (mode == RenderMode.BOTH) {
                }
                ActiveChunkRenderer.drawSheet(x1, z1, (x1 + 16.0), (z1 + 16.0), 63, fillColor, outlineColor);
            }
            if (mode != RenderMode.PILLAR) {
                if (mode == RenderMode.BOTH) {
                }
                ActiveChunkRenderer.drawPillar(x1, z1, (x1 + 16.0), (z1 + 16.0), worldMinY, worldMaxY, fillColor, outlineColor);
            }
            if (((Boolean) this.tracers.get()).booleanValue()) {
                if (mode == RenderMode.SHEET) {
                } else {
                }
                double tracerY = (((double) worldMinY) + 1.0);
                ActiveChunkRenderer.drawTracer(playerX, playerY, playerZ, centerX, tracerY, centerZ, tracerColor, tickDelta);
            }
        }
    }

    private void sendChat(class_1923 pos) {
        if (mc.field_1724 == null) return;
        var tmp0 = new Object[2];
        tmp0[0] = pos.field_9181;
        tmp0[1] = pos.field_9180;
        String msg = String.format("\u00a7c[ChunkDebug]\u00a7f Suspicious chunk at\u00a7c X:\u00a7f %d\u00a7c Z:\u00a7f %d", tmp0);
        mc.field_1724.method_7353(class_2561.method_43470(msg), false);
    }

    private void clearState() {
        this.activityAccumulator.clear();
        this.lastActivityTime.clear();
        this.notifiedChunks.clear();
        this.renderChunks.clear();
    }

    private String currentWorldId() {
        if (mc.field_1687 == null) return "";
        return mc.field_1687.method_27983().method_29177().toString();
    }

    private static class_1923 unpack(long packed) {
        int x = ((int) (packed & 4294967295L));
        int z = ((int) (packed >>> 32));
        return new class_1923(x, z);
    }

}

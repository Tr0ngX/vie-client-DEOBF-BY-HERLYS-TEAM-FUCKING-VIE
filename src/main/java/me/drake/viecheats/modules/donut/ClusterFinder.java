package me.drake.viecheats.modules.donut;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;

public class ClusterFinder extends Module {
    private final BoolSetting tracers;
    private final ConcurrentHashMap clusters;
    private final Set loadedChunkPositions;
    private final ConcurrentLinkedQueue pendingQueue;
    private Thread scannerThread;
    private volatile boolean scanning;
    private static final float CLUSTER_R = 0.5411765f;
    private static final float CLUSTER_G = 0.16862746f;
    private static final float CLUSTER_B = 0.8862745f;
    private static final int CLUSTER_COLOR = -7722014;

    public ClusterFinder() {
        super("ClusterFinder", "Finds Amethyst Clusters continuously", Category.DONUT_SMP);
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw lines from player to clusters", true)));
        this.clusters = new ConcurrentHashMap();
        this.loadedChunkPositions = ConcurrentHashMap.newKeySet();
        this.pendingQueue = new ConcurrentLinkedQueue();
        this.scanning = false;
    }

    public void onEnable() {
        this.clusters.clear();
        this.pendingQueue.clear();
        this.loadedChunkPositions.clear();
        this.scanning = true;
        class_310 mc = class_310.method_1551();
        if ((mc.field_1687 == null) || (mc.field_1724 == null)) break label_19;
        int vd = mc.field_1690.method_38521();
        class_1923 pPos = mc.field_1724.method_31476();
        int x = -(vd);
        while (x <= vd) {
            int z = -(vd);
            while (z <= vd) {
                class_1923 cp = new class_1923((pPos.field_9181 + x), (pPos.field_9180 + z));
                class_2818 chunk = mc.field_1687.method_2935().method_21730(cp.field_9181, cp.field_9180);
                if (chunk != null) {
                    this.loadedChunkPositions.add(cp);
                    this.pendingQueue.offer(cp);
                }
                z++;
            }
            x++;
        }
        label_19:
        this.scannerThread = new Thread(this::scanLoop, "Vie-ClusterScanner");
        this.scannerThread.setPriority(1);
        this.scannerThread.setDaemon(true);
        this.scannerThread.start();
    }

    public void onDisable() {
        this.scanning = false;
        if (this.scannerThread != null) {
            this.scannerThread.interrupt();
            this.scannerThread = null;
        }
        this.clusters.clear();
        this.pendingQueue.clear();
        this.loadedChunkPositions.clear();
    }

    private void scanLoop() {
        while (!(!this.scanning)) {
            try {
                label_1:
                var tmp0 = ((class_1923) this.pendingQueue.poll());
                class_1923 queued = tmp0;
                if (tmp0 != null) {
                    if (!this.scanning) {
                        return;
                        try {
                        }
                        scanChunkAt(queued);
                        break label_1;
                    }
                    Object var2 = this.loadedChunkPositions.iterator();
                    while (var2.hasNext()) {
                        class_1923 cp = ((class_1923) var2.next());
                        if (this.scanning) break label_10;
                        return;
                        try {
                            label_10:
                            var tmp1 = ((class_1923) this.pendingQueue.poll());
                            class_1923 urgent = tmp1;
                            if (tmp1 != null) {
                                if (!this.scanning) {
                                    return;
                                    try {
                                    }
                                    scanChunkAt(urgent);
                                    break label_10;
                                }
                                scanChunkAt(cp);
                            }
                            Thread.sleep(1000L);
                        } catch (InterruptedException ex19) {
                            InterruptedException e = ex19;
                        } catch (Exception ex21) {
                            queued = ex21;
                        }
                    }
                    label_23:
                }
            }
        }
    }

    private void scanChunkAt(class_1923 chunkPos) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) return;
        class_2818 chunk = mc.field_1687.method_2935().method_21730(chunkPos.field_9181, chunkPos.field_9180);
        if (chunk == null) {
            this.clusters.remove(chunkPos);
            return;
        }
        Set found = new HashSet();
        class_2826[] sections = chunk.method_12006();
        class_2338.class_2339 pos = new class_2338.class_2339();
        class_2338.class_2339 adjPos = new class_2338.class_2339();
        class_2350[] directions = class_2350.values();
        int i = 0;
        while (i < sections.length) {
            class_2826 section = sections[i];
            if (section == null) break label_39;
            if (section.method_38292()) {
                break label_39;
            }
            int baseY = (chunk.method_31607() + (i * 16));
            int x = 0;
            while (x < 16) {
                int z = 0;
                while (z < 16) {
                    int y = 0;
                    while (y < 16) {
                        pos.method_10103((chunkPos.method_8326() + x), (baseY + y), (chunkPos.method_8328() + z));
                        class_2680 state = section.method_12254(x, y, z);
                        if (state.method_27852(class_2246.field_27161)) {
                            found.add(pos.method_10062());
                            break label_36;
                        }
                        if (!(state.method_27852(class_2246.field_27159))) {
                            if (!(state.method_27852(class_2246.field_27160))) break label_36;
                        }
                        Object var16 = directions;
                        int var17 = var16.length;
                        int var18 = 0;
                        while (var18 < var17) {
                            class_2350 dir = var16[var18];
                            adjPos.method_25505(pos, dir);
                            class_2680 adjState = getBlockState(adjPos, chunk, mc);
                            if (!(adjState.method_26215())) {
                                if (!(adjState.method_26227().method_15769())) break label_35;
                            }
                            int light = mc.field_1687.method_8314(class_1944.field_9282, adjPos);
                            if ((light < 5) || (light > 6)) break label_35;
                            found.add(adjPos.method_10062());
                            label_35:
                            var18++;
                        }
                        label_36:
                        y++;
                    }
                    z++;
                }
                x++;
            }
            label_39:
            i++;
        }
        if (found.isEmpty()) {
            this.clusters.remove(chunkPos);
            break label_44;
        }
        if (found.equals(this.clusters.get(chunkPos))) break label_44;
        this.clusters.put(chunkPos, Collections.unmodifiableSet(found));
        label_44:
    }

    private class_2680 getBlockState(class_2338 pos, class_2818 chunk, class_310 mc) {
        if ((pos.method_10263() >> 4) == chunk.method_12004().field_9181 && (pos.method_10260() >> 4) == chunk.method_12004().field_9180) return chunk.method_8320(pos);
        return mc.field_1687.method_8320(pos);
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (e.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (this.clusters.isEmpty()) return;
        float tickDelta = e.getTickDelta();
        class_243 cam = mc.field_1724.method_5836(tickDelta);
        float r = 0.5411765f;
        float g = 0.16862746f;
        float b = 0.8862745f;
        if (((Boolean) this.tracers.get()).booleanValue()) {
            class_243 start = getTracerStart();
            Set set = this.clusters.values().iterator();
            while (set.hasNext()) {
                set = ((Set) set.next());
                class_2338 pos = set.iterator();
                while (pos.hasNext()) {
                    pos = ((class_2338) pos.next());
                    class_238 box = new class_238(pos);
                    RenderUtil.drawBox(box, -7722014);
                    RenderUtil.drawFilledBox(box, 814361570);
                    RenderUtil.drawLine(start.field_1352, start.field_1351, start.field_1350, (((double) pos.method_10263()) + 0.5), (((double) pos.method_10264()) + 0.5), (((double) pos.method_10260()) + 0.5), -7722014);
                }
            }
            break label_32;
        }
        start = this.clusters.values().iterator();
        while (start.hasNext()) {
            set = ((Set) start.next());
            set = set.iterator();
            while (set.hasNext()) {
                pos = ((class_2338) set.next());
                box = new class_238(pos);
                RenderUtil.drawBox(box, -7722014);
                RenderUtil.drawFilledBox(box, 814361570);
            }
        }
        label_32:
    }

    private class_243 getTracerStart() {
        if (mc.field_1724 == null) return class_243.field_1353;
        return mc.field_1724.method_5836(1.0f);
    }

    public String getInfo() {
        int total = 0;
        Object var2 = this.clusters.values().iterator();
        while (var2.hasNext()) {
            Set s = ((Set) var2.next());
            total = (total + s.size());
        }
        return String.valueOf(total);
    }

}

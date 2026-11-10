package me.drake.viecheats.modules.render;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2902;
import net.minecraft.class_310;

public class HoleESP extends Module {
    private final IntSetting minY;
    private final IntSetting maxY;
    private final IntSetting minHoleDepth;
    private final BoolSetting detect1x1;
    private final BoolSetting detect1x3;
    private final BoolSetting detectCovered1x1;
    private final BoolSetting detectCovered1x3;
    private final Map cache;
    private boolean registered;
    private static final double MAX_RENDER_DISTANCE_SQ = 65536.0;

    public HoleESP() {
        super("HoleESP", "Highlights safe crystal holes", Category.RENDER);
        this.minY = ((IntSetting) addSetting(new IntSetting("MinY", "Bottom Y offset from world floor", 0, 0, 319)));
        this.maxY = ((IntSetting) addSetting(new IntSetting("MaxY", "Top Y offset from surface", 0, 0, 319)));
        this.minHoleDepth = ((IntSetting) addSetting(new IntSetting("MinDepth", "Minimum hole depth", 4, 1, 20)));
        this.detect1x1 = ((BoolSetting) addSetting(new BoolSetting("1x1Holes", "Detect open 1x1 holes", true)));
        this.detect1x3 = ((BoolSetting) addSetting(new BoolSetting("1x3Holes", "Detect open 1x3 holes", true)));
        this.detectCovered1x1 = ((BoolSetting) addSetting(new BoolSetting("Covered1x1", "Detect covered 1x1 holes", true)));
        this.detectCovered1x3 = ((BoolSetting) addSetting(new BoolSetting("Covered1x3", "Detect covered 1x3 holes", true)));
        this.cache = new ConcurrentHashMap();
        this.registered = false;
    }

    public void onEnable() {
        this.cache.clear();
        VieCheatsMod.getInstance().getEventBus().subscribe(this);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if ((e.getPhase() != TickEvent.Phase.PRE) || (!(isInGame()))) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) return;
        this.cache.keySet().removeIf((pos) -> !(mc.field_1687.method_2935().method_12123(pos.field_9181, pos.field_9180)));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if ((!(isEnabled())) || (!(e.isLoaded()))) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if ((mc == null) || (mc.field_1687 == null)) {
            return;
        }
        class_2818 chunk = mc.field_1687.method_2935().method_21730(e.getChunkPos().field_9181, e.getChunkPos().field_9180);
        if (chunk != null) {
            scan(chunk);
        }
    }

    public void onDisable() {
        this.cache.clear();
        this.registered = false;
    }

    private void scan(class_2818 chunk) {
        class_310 mc = class_310.method_1551();
        if (chunk == null) break label_2;
        if (mc.field_1687 == null) {
            label_2:
            return;
        }
        class_1923 cp = chunk.method_12004();
        HoleESP.ChunkHoleData data = new HoleESP.ChunkHoleData();
        int yMin = (mc.field_1687.method_31607() + ((Integer) this.minY.get()).intValue());
        int yMax = (mc.field_1687.method_8624(class_2902.class_2903.field_13202, cp.method_8326(), cp.method_8328()) - ((Integer) this.maxY.get()).intValue());
        int x = 0;
        while (x < 16) {
            int z = 0;
            while (z < 16) {
                int worldX = (cp.method_8326() + x);
                int worldZ = (cp.method_8328() + z);
                int y = yMin;
                while (y <= yMax) {
                    class_2338 pos = new class_2338(worldX, y, worldZ);
                    if (!(isPassableBlock(pos, mc))) break label_21;
                    if (!(((Boolean) this.detect1x1.get()).booleanValue())) {
                        if (((Boolean) this.detectCovered1x1.get()).booleanValue()) {
                        }
                        check1x1(pos, data, mc);
                    }
                    if (!(((Boolean) this.detect1x3.get()).booleanValue())) {
                        if (!(((Boolean) this.detectCovered1x3.get()).booleanValue())) break label_21;
                    }
                    check1x3(pos, data, mc);
                    label_21:
                    y++;
                }
                z++;
            }
            x++;
        }
        if (data.h1x1.isEmpty() && data.h1x3.isEmpty() && data.c1x1.isEmpty()) {
            if (!(data.c1x3.isEmpty())) {
            }
            this.cache.put(cp, data);
        }
    }

    private void check1x1(class_2338 pos, HoleESP.ChunkHoleData data, class_310 mc) {
        if (!(isValid1x1Section(pos, mc))) return;
        class_2338.class_2339 currentPos = pos.method_25503();
        while (isValid1x1Section(currentPos, mc)) {
            currentPos.method_10098(class_2350.field_11036);
        }
        int depth = (currentPos.method_10264() - pos.method_10264());
        if (depth < ((Integer) this.minHoleDepth.get()).intValue()) break label_13;
        class_238 box = new class_238(((double) pos.method_10263()), ((double) pos.method_10264()), ((double) pos.method_10260()), ((double) (pos.method_10263() + 1)), ((double) currentPos.method_10264()), ((double) (pos.method_10260() + 1)));
        var tmp0 = new Set[2];
        tmp0[0] = data.h1x1;
        tmp0[1] = data.c1x1;
        if (intersectsAny(box, tmp0)) break label_13;
        boolean isCovered = false;
        if (!(((Boolean) this.detectCovered1x1.get()).booleanValue())) break label_11;
        class_2338 top = new class_2338(pos.method_10263(), currentPos.method_10264(), pos.method_10260());
        if (!(isSolidBlock(top, mc))) break label_11;
        isCovered = true;
        label_11:
        if (!(!isCovered)) {
            data.c1x1.add(box);
            break label_13;
        }
        if (!(((Boolean) this.detect1x1.get()).booleanValue())) break label_13;
        data.h1x1.add(box);
        label_13:
    }

    private void check1x3(class_2338 pos, HoleESP.ChunkHoleData data, class_310 mc) {
        boolean isNS = isValid1x3SectionNS(pos, mc);
        if (isNS) break label_2;
        boolean isEW = isValid1x3SectionEW(pos, mc);
        if (!isNS && !isEW) return;
        class_2338.class_2339 currentPos = pos.method_25503();
        label_6:
        if (!(!isNS)) {
            if (!(isValid1x3SectionNS(currentPos, mc))) {
            }
            if (!(!isEW) && isValid1x3SectionEW(currentPos, mc)) {
            }
            currentPos.method_10098(class_2350.field_11036);
            break label_6;
        }
        int depth = (currentPos.method_10264() - pos.method_10264());
        if (depth < ((Integer) this.minHoleDepth.get()).intValue()) break label_41;
        class_238 box = new class_238(((double) pos.method_10263()), ((double) pos.method_10264()), ((double) pos.method_10260()), ((double) (pos.method_10263() + (!(!isNS) ? 1 : 3))), ((double) currentPos.method_10264()), ((double) (pos.method_10260() + (!(!isNS) ? 3 : 1))));
        var tmp0 = new Set[2];
        tmp0[0] = data.h1x3;
        tmp0[1] = data.c1x3;
        if (intersectsAny(box, tmp0)) break label_41;
        boolean isCovered = false;
        if (!(((Boolean) this.detectCovered1x3.get()).booleanValue())) break label_39;
        boolean allCovered = true;
        int i = 0;
        while (i < 3) {
            class_2338 top = new class_2338((pos.method_10263() + (!(!isNS) ? 0 : i)), currentPos.method_10264(), (pos.method_10260() + (!(!isNS) ? i : 0)));
            if (!(isSolidBlock(top, mc))) {
                allCovered = false;
                break label_38;
            }
            i++;
        }
        label_38:
        if (!allCovered) break label_39;
        isCovered = true;
        label_39:
        if (!(!isCovered)) {
            data.c1x3.add(box);
            break label_41;
        }
        if (!(((Boolean) this.detect1x3.get()).booleanValue())) break label_41;
        data.h1x3.add(box);
        label_41:
    }

    @SafeVarargs
    private final boolean intersectsAny(class_238 target, Set... sets) {
        Object var3 = sets;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Set set = var3[var5];
            Object var7 = set.iterator();
            while (var7.hasNext()) {
                class_238 b = ((class_238) var7.next());
                if (b.method_994(target)) return true;
            }
            var5++;
        }
        return false;
    }

    private boolean isValid1x1Section(class_2338 pos, class_310 mc) {
        if ((((!(isPassableBlock(pos, mc))) || (isPassableBlock(pos.method_10095(), mc))) || (isPassableBlock(pos.method_10072(), mc))) || (isPassableBlock(pos.method_10078(), mc))) break label_5;
        return !(isPassableBlock(pos.method_10067(), mc));
    }

    private boolean isValid1x3SectionNS(class_2338 pos, class_310 mc) {
        class_2338 p2 = pos.method_10072();
        class_2338 p3 = pos.method_10077(2);
        if ((((((((((!(isPassableBlock(pos, mc))) || (!(isPassableBlock(p2, mc)))) || (!(isPassableBlock(p3, mc)))) || (isPassableBlock(pos.method_10078(), mc))) || (isPassableBlock(p2.method_10078(), mc))) || (isPassableBlock(p3.method_10078(), mc))) || (isPassableBlock(pos.method_10067(), mc))) || (isPassableBlock(p2.method_10067(), mc))) || (isPassableBlock(p3.method_10067(), mc))) || (isPassableBlock(pos.method_10095(), mc))) break label_13;
        return !(isPassableBlock(p3.method_10072(), mc));
    }

    private boolean isValid1x3SectionEW(class_2338 pos, class_310 mc) {
        class_2338 p2 = pos.method_10078();
        class_2338 p3 = pos.method_10089(2);
        if ((((((((((!(isPassableBlock(pos, mc))) || (!(isPassableBlock(p2, mc)))) || (!(isPassableBlock(p3, mc)))) || (isPassableBlock(pos.method_10095(), mc))) || (isPassableBlock(p2.method_10095(), mc))) || (isPassableBlock(p3.method_10095(), mc))) || (isPassableBlock(pos.method_10072(), mc))) || (isPassableBlock(p2.method_10072(), mc))) || (isPassableBlock(p3.method_10072(), mc))) || (isPassableBlock(pos.method_10067(), mc))) break label_13;
        return !(isPassableBlock(p3.method_10078(), mc));
    }

    private boolean isPassableBlock(class_2338 pos, class_310 mc) {
        class_2680 state = mc.field_1687.method_8320(pos);
        class_265 shape = state.method_26220(mc.field_1687, pos);
        if (!(shape.method_1110())) {
            if (!(class_259.method_1077().equals(shape))) {
            }
        } else {
        }
        return false;
    }

    private boolean isSolidBlock(class_2338 pos, class_310 mc) {
        return mc.field_1687.method_8320(pos).method_26212(mc.field_1687, pos);
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (e.getStage() == RenderEvent.Stage.WORLD_3D) {
            if (isInGame()) break label_2;
        }
        return;
        label_2:
        boolean hasAny = false;
        float tickDelta = this.cache.values().iterator();
        while (tickDelta.hasNext()) {
            HoleESP.ChunkHoleData d = ((HoleESP.ChunkHoleData) tickDelta.next());
            if (d.h1x1.isEmpty() && d.h1x3.isEmpty() && d.c1x1.isEmpty()) {
                if (!(d.c1x3.isEmpty())) {
                }
                hasAny = true;
                break label_9;
            }
        }
        label_9:
        if (!hasAny) return;
        tickDelta = e.getTickDelta();
        class_243 cam = mc.field_1773.method_19418().method_71156();
        float outlineR = 1.0f;
        float outlineG = 0.0f;
        float outlineB = 0.0f;
        int outlineA = 204;
        int fillA = 51;
        int outlineColor = ((((outlineA << 24) | (((int) (outlineR * 255.0f)) << 16)) | (((int) (outlineG * 255.0f)) << 8)) | ((int) (outlineB * 255.0f)));
        int fillColor = ((((fillA << 24) | (((int) (outlineR * 255.0f)) << 16)) | (((int) (outlineG * 255.0f)) << 8)) | ((int) (outlineB * 255.0f)));
        Object var12 = this.cache.entrySet().iterator();
        while (var12.hasNext()) {
            Map.Entry entry = ((Map.Entry) var12.next());
            class_1923 chunkPos = ((class_1923) entry.getKey());
            HoleESP.ChunkHoleData data = ((HoleESP.ChunkHoleData) entry.getValue());
            double dx = (((double) chunkPos.method_33940()) - cam.field_1352);
            double dz = (((double) chunkPos.method_33942()) - cam.field_1350);
            if (((dx * dx) + (dz * dz)) > 65536.0) {
                break label_20;
            }
            Object var20 = data.h1x1.iterator();
            while (var20.hasNext()) {
                class_238 box = ((class_238) var20.next());
                RenderUtil.drawBox(box, outlineColor);
                RenderUtil.drawFilledBox(box, fillColor);
            }
            var20 = data.h1x3.iterator();
            while (var20.hasNext()) {
                box = ((class_238) var20.next());
                RenderUtil.drawBox(box, outlineColor);
                RenderUtil.drawFilledBox(box, fillColor);
            }
            var20 = data.c1x1.iterator();
            while (var20.hasNext()) {
                box = ((class_238) var20.next());
                RenderUtil.drawBox(box, outlineColor);
                RenderUtil.drawFilledBox(box, fillColor);
            }
            var20 = data.c1x3.iterator();
            while (var20.hasNext()) {
                box = ((class_238) var20.next());
                RenderUtil.drawBox(box, outlineColor);
                RenderUtil.drawFilledBox(box, fillColor);
            }
        }
    }

    public String getInfo() {
        int total = 0;
        Object var2 = this.cache.values().iterator();
        while (var2.hasNext()) {
            HoleESP.ChunkHoleData d = ((HoleESP.ChunkHoleData) var2.next());
            total = (total + (((d.h1x1.size() + d.h1x3.size()) + d.c1x1.size()) + d.c1x3.size()));
        }
        return String.valueOf(total);
    }


    class ChunkHoleData {
        final Set h1x1;
        final Set h1x3;
        final Set c1x1;
        final Set c1x3;

        private ChunkHoleData() {
            this.h1x1 = new HashSet();
            this.h1x3 = new HashSet();
            this.c1x1 = new HashSet();
            this.c1x3 = new HashSet();
        }

    }
}

package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_746;

public class Scaffold extends Module {
    private final IntSetting delay;
    private final BoolSetting safeWalk;
    private final BoolSetting rotate;
    private int delayTicks;

    public Scaffold() {
        super("Scaffold", "Places blocks under your feet", Category.MOVEMENT);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between placements", 1, 0, 5)));
        this.safeWalk = ((BoolSetting) addSetting(new BoolSetting("SafeWalk", "Auto sneak at edges", true)));
        this.rotate = ((BoolSetting) addSetting(new BoolSetting("Rotate", "Rotate to place (STRONGLY recommended for bypass)", true)));
        this.delayTicks = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        if (((Boolean) this.safeWalk.get()).booleanValue()) {
            if (p.method_24828()) {
                if (isNearEdge(p)) {
                }
            } else {
            }
            1.method_5660(false);
        }
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        class_2338 below = class_2338.method_49637(p.method_23317(), (p.method_23318() - 0.5), p.method_23321());
        class_2680 state = mc.field_1687.method_8320(below);
        if (!(state.method_26215())) return;
        int slot = InventoryUtil.findBlockInHotbar();
        if (slot == -1) return;
        int prevSlot = p.method_31548().method_67532();
        p.method_31548().method_61496(slot);
        var tmp0 = new class_2350[5];
        tmp0[0] = class_2350.field_11043;
        tmp0[1] = class_2350.field_11035;
        tmp0[2] = class_2350.field_11034;
        tmp0[3] = class_2350.field_11039;
        tmp0[4] = class_2350.field_11033;
        Object var6 = tmp0;
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            class_2350 dir = var6[var8];
            class_2338 neighbor = below.method_10093(dir);
            if (!(mc.field_1687.method_8320(neighbor).method_26215())) {
                class_2350 face = dir.method_10153();
                class_243 hitVec = class_243.method_24953(below).method_1019(class_243.method_24954(face.method_62675()).method_1021(0.5));
                mc.field_1761.method_2896(p, class_1268.field_5808, new class_3965(hitVec, face, neighbor, false));
                p.method_6104(class_1268.field_5808);
                this.delayTicks = ((Integer) this.delay.get()).intValue();
                break label_26;
            }
            var8++;
        }
        label_26:
        p.method_31548().method_61496(prevSlot);
    }

    private boolean isNearEdge(class_746 p) {
        class_2338 pos = p.method_24515();
        int dx = -1;
        while (dx <= 1) {
            int dz = -1;
            while (dz <= 1) {
                if (mc.field_1687.method_8320(pos.method_10069(dx, -1, dz)).method_26215()) return true;
                dz++;
            }
            dx++;
        }
        return false;
    }

}

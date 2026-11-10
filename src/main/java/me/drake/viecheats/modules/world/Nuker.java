package me.drake.viecheats.modules.world;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.RotationUtil;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_746;

public class Nuker extends Module {
    private final EnumSetting mode;
    private final DoubleSetting range;
    private final IntSetting delay;
    private final IntSetting maxPerTick;
    private int delayTicks;

    public Nuker() {
        super("Nuker", "Breaks blocks around you", Category.WORLD);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Break mode", Nuker.Mode.NEAREST, Nuker.Mode.values())));
        this.range = ((DoubleSetting) addSetting(new DoubleSetting("Range", "Break range", 4.0, 1.0, 4.5)));
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between breaks", 1, 0, 10)));
        this.maxPerTick = ((IntSetting) addSetting(new IntSetting("MaxPerTick", "Max blocks broken per tick (1 = safe)", 1, 1, 1)));
        this.delayTicks = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        class_2338 center = p.method_24515();
        int r = ((int) Math.ceil(((Double) this.range.get()).doubleValue()));
        class_2338 targetPos = null;
        double closestDist = 1.7976931348623157E308;
        int x = -(r);
        while (x <= r) {
            int y = -(r);
            while (y <= r) {
                int z = -(r);
                while (z <= r) {
                    class_2338 pos = center.method_10069(x, y, z);
                    double dist = pos.method_40081(p.method_23317(), p.method_23318(), p.method_23321());
                    if (dist > (((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue())) {
                        break label_22;
                    }
                    class_2680 state = mc.field_1687.method_8320(pos);
                    if (state.method_26215()) {
                        break label_22;
                    }
                    if (state.method_26214(mc.field_1687, pos) < 0.0f) {
                        break label_22;
                    }
                    if (dist >= closestDist) break label_22;
                    closestDist = dist;
                    targetPos = pos;
                    label_22:
                    z++;
                }
                y++;
            }
            x++;
        }
        if (targetPos == null) return;
        float[] angles = RotationUtil.getRotations(targetPos, p);
        float sensitivity = ((Double) mc.field_1690.method_42495().method_41753()).floatValue();
        float targetYaw = (p.method_36454() + RotationUtil.applyGCD((angles[0] - p.method_36454()), sensitivity));
        float targetPitch = (p.method_36455() + RotationUtil.applyGCD((angles[1] - p.method_36455()), sensitivity));
        p.method_36456(targetYaw);
        p.method_36457(targetPitch);
        mc.field_1761.method_2902(targetPos, class_2350.field_11036);
        p.method_6104(class_1268.field_5808);
        this.delayTicks = ((Integer) this.delay.get()).intValue();
    }

    public String getInfo() {
        var tmp0 = new Object[1];
        tmp0[0] = this.range.get();
        return String.format("%.1f", tmp0);
    }


    public enum Mode {
        ALL,
        NEAREST;


        private Mode() {
        }

    }
}

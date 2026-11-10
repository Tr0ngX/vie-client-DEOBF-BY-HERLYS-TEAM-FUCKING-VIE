package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1268;
import net.minecraft.class_2242;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2421;
import net.minecraft.class_2680;
import net.minecraft.class_3830;
import net.minecraft.class_746;

public class AutoCrop extends Module {
    private final DoubleSetting radius;
    private final IntSetting delay;
    private int delayTicks;

    public AutoCrop() {
        super("AutoCrop", "Auto harvests and replants crops (DonutSMP)", Category.DONUT_SMP);
        this.radius = ((DoubleSetting) addSetting(new DoubleSetting("Radius", "Harvest radius", 5.0, 1.0, 10.0)));
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between harvests", 2, 0, 10)));
        this.delayTicks = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        class_746 p = mc.field_1724;
        class_2338 center = p.method_24515();
        int r = ((int) Math.ceil(((Double) this.radius.get()).doubleValue()));
        double rSq = (((Double) this.radius.get()).doubleValue() * ((Double) this.radius.get()).doubleValue());
        Object var6 = class_2338.method_25996(center, r, 2, r).iterator();
        while (var6.hasNext()) {
            class_2338 pos = ((class_2338) var6.next());
            if (pos.method_40081(p.method_23317(), p.method_23318(), p.method_23321()) > rSq) {
                break label_7;
            }
            class_2680 state = mc.field_1687.method_8320(pos);
            if (isMatureCrop(state)) {
                mc.field_1761.method_2910(pos, class_2350.field_11036);
                p.method_6104(class_1268.field_5808);
                this.delayTicks = ((Integer) this.delay.get()).intValue();
                return;
            }
        }
    }

    private boolean isMatureCrop(class_2680 state) {
        class_2248 b = state.method_26204();
        if ((b instanceof class_2302)) {
            class_2302 crop = ((class_2302) b);
            return crop.method_9825(state);
        }
        if (b == class_2246.field_10341) return ((Integer) state.method_11654(class_2242.field_9962)).intValue() == 3;
        if (b == class_2246.field_9974) return ((Integer) state.method_11654(class_2421.field_11306)).intValue() == 3;
        if (b == class_2246.field_16999) return ((Integer) state.method_11654(class_3830.field_17000)).intValue() == 3;
        return false;
    }

}

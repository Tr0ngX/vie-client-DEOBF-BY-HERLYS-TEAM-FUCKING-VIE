package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.ServerSafety;
import net.minecraft.class_5134;

public class Step extends Module {
    private final EnumSetting mode;
    private final DoubleSetting height;

    public Step() {
        super("Step", "Step up blocks without jumping", Category.MOVEMENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Step mode", Step.Mode.VANILLA, Step.Mode.values())));
        this.height = ((DoubleSetting) addSetting(new DoubleSetting("Height", "Max step height (capped for bypass)", 1.0, 0.6, 1.25)));
    }

    public void onEnable() {
        super.onEnable();
        applyStepHeight();
    }

    public void onDisable() {
        super.onDisable();
        restoreStepHeight();
    }

    public void onTick() {
        if (!(isInGame())) return;
        applyStepHeight();
    }

    private void applyStepHeight() {
        if (!(isInGame())) return;
        double value = ((Double) this.height.get()).doubleValue();
        if (ServerSafety.isDonutServer()) {
            value = 0.6;
        }
        mc.field_1724.method_5996(class_5134.field_47761).method_6192(value);
    }

    private void restoreStepHeight() {
        if ((mc == null) || (mc.field_1724 == null)) {
            return;
        }
        mc.field_1724.method_5996(class_5134.field_47761).method_6192(0.6);
    }

    public String getInfo() {
        var tmp0 = new Object[1];
        tmp0[0] = this.height.get();
        return String.format("%.1f", tmp0);
    }


    public enum Mode {
        VANILLA,
        PACKET;


        private Mode() {
        }

    }
}

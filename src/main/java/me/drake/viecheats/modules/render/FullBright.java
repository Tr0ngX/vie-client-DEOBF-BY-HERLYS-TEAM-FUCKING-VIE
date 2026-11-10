package me.drake.viecheats.modules.render;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import net.minecraft.class_1293;
import net.minecraft.class_1294;

public class FullBright extends Module {
    private static final double MAX_GAMMA_BOOST = 256.0;
    private static final int NV_DURATION_TICKS = 420;
    private static final int NV_REFRESH_THRESHOLD_TICKS = 120;
    private final EnumSetting mode;
    private double originalGamma;
    private boolean hadNightVisionBefore;

    public FullBright() {
        super("FullBright", "Makes the world fully bright", Category.RENDER);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Brightness mode", FullBright.Mode.GAMMA, FullBright.Mode.values())));
        this.originalGamma = 1.0;
        this.hadNightVisionBefore = false;
    }

    public void onEnable() {
        super.onEnable();
        if (!(isInGame())) return;
        this.originalGamma = ((Double) mc.field_1690.method_42473().method_41753()).doubleValue();
        if (mc.field_1724 == null) break label_4;
        this.hadNightVisionBefore = mc.field_1724.method_6059(class_1294.field_5925);
    }

    public void onDisable() {
        super.onDisable();
        if ((mc == null) || (mc.field_1690 == null)) {
            return;
        }
        mc.field_1690.method_42473().method_41748(this.originalGamma);
        if (mc.field_1724 != null && !this.hadNightVisionBefore) {
            mc.field_1724.method_6016(class_1294.field_5925);
        }
        this.hadNightVisionBefore = false;
    }

    public void onTick() {
        if (!(isInGame())) return;
        switch (((FullBright.Mode) this.mode.get()).ordinal()) {
            case 0 -> { break label_2; }
            case 1 -> { break label_5; }
            default -> { break label_7; }
        }
        label_2:
        mc.field_1690.method_42473().method_41748(256.0);
        ensureNightVisionBoost();
        break label_7;
        label_5:
        mc.field_1690.method_42473().method_41748(this.originalGamma);
        ensureNightVisionBoost();
        label_7:
    }

    public String getInfo() {
        return ((FullBright.Mode) this.mode.get()).name();
    }

    private void ensureNightVisionBoost() {
        if (mc.field_1724 == null) return;
        class_1293 existing = mc.field_1724.method_6112(class_1294.field_5925);
        if (existing != null) {
            if (existing.method_5584() < 120) {
            }
            mc.field_1724.method_6092(new class_1293(class_1294.field_5925, 420, 0, false, false, false));
        }
    }


    public enum Mode {
        GAMMA,
        NIGHT_VISION;


        private Mode() {
        }

    }
}

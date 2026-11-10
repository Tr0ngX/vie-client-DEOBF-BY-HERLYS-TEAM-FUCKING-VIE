package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;

public class AutoSprint extends Module {
    private final BoolSetting stopInWater;
    private final BoolSetting always;

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints", Category.MOVEMENT);
        this.stopInWater = ((BoolSetting) addSetting(new BoolSetting("StopInWater", "Stop sprinting in water", true)));
        this.always = ((BoolSetting) addSetting(new BoolSetting("Always", "Sprint even when not holding forward", false)));
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (((Boolean) this.stopInWater.get()).booleanValue() && mc.field_1724.method_5799()) return;
        if (mc.field_1724.field_3913.field_54155.comp_3159()) break label_5;
        if (!(((Boolean) this.always.get()).booleanValue())) break label_6;
        if (mc.field_1724.field_3913.field_54155.comp_3161()) break label_5;
        if (!(mc.field_1724.field_3913.field_54155.comp_3162())) break label_6;
        label_5:
        break label_7;
        label_6:
        label_7:
        boolean moving = false;
        if (!(!moving)) {
            mc.field_1724.method_5728(true);
        }
    }

}

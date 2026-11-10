package me.drake.viecheats.modules.world;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;

public class FastPlace extends Module {
    private final IntSetting delay;

    public FastPlace() {
        super("FastPlace", "Removes block place delay", Category.WORLD);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Placement cooldown ticks (0 = instant)", 0, 0, 4)));
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (mc.field_1761 != null) {
            mc.field_1690.field_1904.method_23481(mc.field_1690.field_1904.method_1434());
        }
    }

    public int getConfiguredDelay() {
        return ((Integer) this.delay.get()).intValue();
    }

    public String getInfo() {
        return (String.valueOf(this.delay.get()) + "t");
    }

}

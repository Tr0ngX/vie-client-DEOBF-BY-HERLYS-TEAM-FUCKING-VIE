package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_746;

public class FastUse extends Module {
    private final IntSetting speed;

    public FastUse() {
        super("FastUse", "Speeds up item usage", Category.PLAYER);
        this.speed = ((IntSetting) addSetting(new IntSetting("Speed", "Use speed multiplier (ticks skipped per tick)", 1, 1, 5)));
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        if (!(p.method_6115())) return;
        int i = 0;
        while (i < (((Integer) this.speed.get()).intValue() - 1)) {
            mc.field_1761.method_2927();
            i++;
        }
    }

    public String getInfo() {
        return (String.valueOf(this.speed.get()) + "x");
    }

}

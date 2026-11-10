package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;

public class SwingSpeed extends Module {
    private final IntSetting speed;

    public SwingSpeed() {
        super("SwingSpeed", "Adjust hand swing animation speed", Category.PLAYER);
        this.speed = ((IntSetting) addSetting(new IntSetting("Speed", "Swing speed modifier (0 = vanilla)", 0, -20, 20)));
    }

    public int getSwingDuration() {
        int s = ((Integer) this.speed.get()).intValue();
        if (s > 0) return Math.max(2, (6 - (s / 5)));
        return (6 - s);
    }

    public String getInfo() {
        return String.valueOf(this.speed.get());
    }

}

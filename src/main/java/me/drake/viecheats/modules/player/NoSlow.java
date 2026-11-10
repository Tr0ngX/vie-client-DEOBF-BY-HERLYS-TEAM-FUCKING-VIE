package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", "No slowdown while using items", Category.PLAYER);
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (mc.field_1724.method_6115() && mc.field_1724.method_5624()) {
            mc.field_1724.method_5728(true);
        }
    }

}

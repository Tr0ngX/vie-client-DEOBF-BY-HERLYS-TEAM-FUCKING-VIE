package me.drake.viecheats.modules.world;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.mixin.MixinClientPlayerInteractionManagerAccessor;

public class NoBreakDelay extends Module {
    public NoBreakDelay() {
        super("NoBreakDelay", "Removes mining delay between blocks", Category.WORLD);
    }

    public void onTick() {
        if ((!(isInGame())) || (mc.field_1761 == null)) {
            return;
        }
        MixinClientPlayerInteractionManagerAccessor accessor = ((MixinClientPlayerInteractionManagerAccessor) mc.field_1761);
        if (accessor.getBlockBreakingCooldown() > 0) {
            accessor.setBlockBreakingCooldown(0);
        }
    }

}

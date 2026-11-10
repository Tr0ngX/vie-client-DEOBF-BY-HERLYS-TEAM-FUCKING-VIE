package me.drake.viecheats.modules.player;

import me.drake.viecheats.events.eventbus.EventHandler;
import me.drake.viecheats.events.world.TickEvent;
import me.drake.viecheats.mixin.MixinClientPlayerInteractionManagerAccessor;
import me.drake.viecheats.modules.Categories;
import me.drake.viecheats.modules.Module;
import me.drake.viecheats.settings.IntSetting;
import me.drake.viecheats.settings.Setting;
import me.drake.viecheats.settings.SettingGroup;

/**
 * Purified NoBreakDelay Module: Uses semantically restored MixinClientPlayerInteractionManagerAccessor
 * (formerly obfuscated as me.drake.viecheats.hook.ac).
 */
public class NoBreakDelay extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
            .name("cooldown")
            .description("Block breaking cooldown in ticks (0 = instant re-break).")
            .defaultValue(0)
            .min(0)
            .sliderMax(5)
            .build());

    public NoBreakDelay() {
        super(Categories.Player, "no-break-delay", "Removes or reduces the delay between breaking blocks.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.interactionManager == null) {
            return;
        }
        MixinClientPlayerInteractionManagerAccessor accessor =
                (MixinClientPlayerInteractionManagerAccessor) mc.interactionManager;
        if (accessor.getBlockBreakingCooldown() > cooldown.get()) {
            accessor.setBlockBreakingCooldown(cooldown.get());
        }
    }
}

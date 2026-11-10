package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.donut.ActiveChunkActivityStore;
import net.minecraft.class_2338;
import net.minecraft.class_6755;
import net.minecraft.class_6760;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_6755.class)
public class MixinChunkBuilder {
    @Inject(method = "method_39371()Lnet/minecraft/class_6760;", at = @At(value = "RETURN", ordinal = 0))
    private void onPollNextTick(CallbackInfoReturnable cir) {
        class_6760 tick = ((class_6760) cir.getReturnValue());
        if (tick != null) {
            class_2338 pos = tick.comp_253();
            ActiveChunkActivityStore.recordScheduledTick(pos);
        }
    }

}

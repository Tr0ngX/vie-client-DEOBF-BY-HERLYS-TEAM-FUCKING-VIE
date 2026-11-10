package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.donut.ActiveChunkActivityStore;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_2818.class)
public class MixinWorldChunk {
    @Inject(method = "method_12010(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;I)Lnet/minecraft/class_2680;", at = @At("HEAD"))
    private void onSetBlockState(class_2338 pos, class_2680 state, int flags, CallbackInfoReturnable cir) {
        if (pos == null) return;
        ActiveChunkActivityStore.recordBlockUpdate(pos);
    }

}

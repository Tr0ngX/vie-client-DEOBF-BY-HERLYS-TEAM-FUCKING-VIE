package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.donut.ActiveChunkActivityStore;
import net.minecraft.class_2338;
import net.minecraft.class_3558;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3558.class)
public class MixinChunkLightProvider {
    @Inject(method = "method_15513(Lnet/minecraft/class_2338;)V", at = @At("HEAD"))
    private void onCheckBlock(class_2338 pos, CallbackInfo ci) {
        if (pos == null) return;
        int cx = (pos.method_10263() >> 4);
        int cz = (pos.method_10260() >> 4);
        ActiveChunkActivityStore.recordLightingUpdate(cx, cz);
    }

}

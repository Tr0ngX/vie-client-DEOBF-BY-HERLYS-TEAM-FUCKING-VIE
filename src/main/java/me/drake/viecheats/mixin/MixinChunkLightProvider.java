package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.Modules;
import me.drake.viecheats.modules.render.NoRender;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLightProvider.class)
public class MixinChunkLightProvider {
    @Inject(method = "doLightUpdates", at = @At("HEAD"), cancellable = true)
    private void onDoLightUpdates(CallbackInfoReturnable<Integer> cir) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender != null && noRender.noSkylightUpdates()) {
            cir.setReturnValue(0);
        }
    }
}

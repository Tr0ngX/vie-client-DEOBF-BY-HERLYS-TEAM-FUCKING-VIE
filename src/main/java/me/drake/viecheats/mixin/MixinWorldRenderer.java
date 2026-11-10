package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.Modules;
import me.drake.viecheats.modules.render.NoRender;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(CallbackInfo ci) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender != null && noRender.noWeather()) {
            ci.cancel();
        }
    }
}

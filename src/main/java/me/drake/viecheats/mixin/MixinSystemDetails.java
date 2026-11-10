package me.drake.viecheats.mixin;

import java.util.Locale;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/class_6396")
public abstract class MixinSystemDetails {
    @Shadow(remap = false)
    public abstract void method_37122(String arg0, String arg1);

    @Inject(method = "method_37123(Ljava/lang/String;Ljava/util/function/Supplier;)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void viecheats$skipRenderExtensions(String name, Supplier supplier, CallbackInfo ci) {
        if (name == null) return;
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.contains("render") && normalized.contains("extension")) {
            method_37122(name, "N/A");
            ci.cancel();
        }
    }

}

package me.drake.viecheats.mixin;

import java.util.function.Supplier;
import net.minecraft.util.SystemDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SystemDetails.class)
public class MixinSystemDetails {
    @Inject(method = "addSection(Ljava/lang/String;Ljava/util/function/Supplier;)V", at = @At("HEAD"), cancellable = true)
    private void onAddSection(String name, Supplier<String> valueSupplier, CallbackInfo ci) {
        if ("Fabric Mods".equals(name)) {
            // Filter mod metadata when spoofed
        }
    }
}

package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import net.minecraft.class_238;
import net.minecraft.class_4604;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_4604.class)
public class MixinFrustum {
    @Inject(method = "method_23093(Lnet/minecraft/class_238;)Z", at = @At("HEAD"), cancellable = true)
    private void viecheats$forceVisible(class_238 box, CallbackInfoReturnable cir) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isRenderAllActive()) {
            cir.setReturnValue(true);
        }
    }

}

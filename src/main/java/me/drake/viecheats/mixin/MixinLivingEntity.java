package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.modules.player.SwingSpeed;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public class MixinLivingEntity {
    @Inject(method = "method_6028()I", at = @At("RETURN"), cancellable = true)
    private void onGetHandSwingDuration(CallbackInfoReturnable cir) {
        class_1309 self = ((class_1309) this);
        class_310 mc = class_310.method_1551();
        if (self != mc.field_1724 && self.method_5628() != -6969) return;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        SwingSpeed mod = ((SwingSpeed) instance.getModuleManager().getModule(SwingSpeed.class));
        if (mod != null && mod.isEnabled()) {
            cir.setReturnValue(mod.getSwingDuration());
        }
    }

    @Inject(method = "method_5810()Z", at = @At("RETURN"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable cir) {
        class_1309 self = ((class_1309) this);
        if (self.method_5628() != -6969) return;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isActive()) {
            cir.setReturnValue(false);
        }
    }

}

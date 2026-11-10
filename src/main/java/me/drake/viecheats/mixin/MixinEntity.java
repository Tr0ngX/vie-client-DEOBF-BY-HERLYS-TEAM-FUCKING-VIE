package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.modules.movement.FreeLook;
import net.minecraft.class_1297;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class MixinEntity {
    @Inject(method = "method_5872(DD)V", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!((this instanceof class_746))) return;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            freecam.applyMouseDelta(((float) cursorDeltaX), ((float) cursorDeltaY));
            ci.cancel();
            return;
        }
        FreeLook freeLook = ((FreeLook) instance.getModuleManager().getModule(FreeLook.class));
        if (freeLook == null) break label_16;
        if (freeLook.cameraMode()) {
            freeLook.applyMouseDelta(((float) cursorDeltaX), ((float) cursorDeltaY));
            ci.cancel();
            return;
        }
        if (!(freeLook.playerMode())) break label_16;
        freeLook.applyMouseDelta(((float) cursorDeltaX), ((float) cursorDeltaY));
        label_16:
    }

}

package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public abstract class MixinMouse {
    @Inject(method = "method_1601(JLnet/minecraft/class_11910;I)V", at = @At("HEAD"), cancellable = true)
    private void onMouseButtonInput(long window, class_11910 mouseInput, int action, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc == null) return;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        class_11909 click = new class_11909(0.0, 0.0, mouseInput);
        if (freecam != null && freecam.isEnabled() && freecam.handleMouseButtonInput(click, action)) {
            ci.cancel();
            return;
        }
        if (mc.field_1755 != null) return;
        if (action == 1) {
            instance.getModuleManager().onKeyPress((1000 + mouseInput.comp_4801()));
        }
    }

    @Inject(method = "method_1598(JDD)V", at = @At("HEAD"), cancellable = true)
    private void onMouseScrollInput(long window, double horizontal, double vertical, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled() && freecam.handleMouseScroll(vertical)) {
            ci.cancel();
        }
    }

}

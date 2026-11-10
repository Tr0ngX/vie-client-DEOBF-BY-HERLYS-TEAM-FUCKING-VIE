package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.modules.exploit.Freecam;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_309.class)
public class MixinKeyboard {
    @Inject(method = "method_1466(JILnet/minecraft/class_11908;)V", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, class_11908 input, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc == null) return;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled() && freecam.handleKeyboardInput(input, action)) {
            ci.cancel();
            return;
        }
        if (action != 1) break label_16;
        ClientSettings cs = ClientSettings.get();
        if (cs != null) {
        } else {
        }
        int guiKey = 344;
        if (input.comp_4795() != guiKey) break label_16;
        instance.openClickGuiFromKeyPress(mc);
        label_16:
        if (mc.field_1755 != null) return;
        if (action != 1) return;
        instance.getModuleManager().onKeyPress(input.comp_4795());
    }

}

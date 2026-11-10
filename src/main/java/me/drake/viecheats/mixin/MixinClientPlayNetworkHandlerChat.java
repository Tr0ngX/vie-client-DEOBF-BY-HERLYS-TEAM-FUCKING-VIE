package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.command.CommandManager;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public class MixinClientPlayNetworkHandlerChat {
    @Inject(method = "method_45729(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if ((message == null) || (!(message.startsWith(".")))) {
            return;
        }
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        CommandManager cm = instance.getCommandManager();
        if (cm == null) return;
        boolean handled = cm.dispatch(message);
        if (!(!handled)) {
            ci.cancel();
        }
    }

}

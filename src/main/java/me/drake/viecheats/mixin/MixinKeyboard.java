package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.client.KeyEvent;
import me.drake.viecheats.events.common.EventAction;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key != -1) {
            KeyEvent event = VieCheatsMod.EVENT_BUS.post(KeyEvent.get(key, modifiers, EventAction.from(action)));
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}

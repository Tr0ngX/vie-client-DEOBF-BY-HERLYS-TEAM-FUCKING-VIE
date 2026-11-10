package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.client.MouseButtonEvent;
import me.drake.viecheats.events.common.EventAction;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        MouseButtonEvent event = VieCheatsMod.EVENT_BUS.post(MouseButtonEvent.get(button, EventAction.from(action)));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

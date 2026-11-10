package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.game.ChangePerspectiveEvent;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {
    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void onSetPerspective(Perspective perspective, CallbackInfo ci) {
        ChangePerspectiveEvent event = VieCheatsMod.EVENT_BUS.post(ChangePerspectiveEvent.get(perspective));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

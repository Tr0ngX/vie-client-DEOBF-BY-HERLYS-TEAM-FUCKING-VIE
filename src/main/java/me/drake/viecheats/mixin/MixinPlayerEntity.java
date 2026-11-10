package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.entity.player.ClipAtLedgeEvent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void onClipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        ClipAtLedgeEvent event = VieCheatsMod.EVENT_BUS.post(ClipAtLedgeEvent.get());
        if (event.isSet()) {
            cir.setReturnValue(event.isClip());
        }
    }
}

package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.PlayerMoveEvent;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_746.class)
public class MixinClientPlayerEntity {
    @Inject(method = "method_3136()V", at = @At("HEAD"))
    private void onSendMovementPackets(CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        class_746 player = ((class_746) this);
        PlayerMoveEvent event = new PlayerMoveEvent(player.method_23317(), player.method_23318(), player.method_23321(), player.method_36454(), player.method_36455(), player.method_24828());
        instance.getEventBus().post(event);
    }

}

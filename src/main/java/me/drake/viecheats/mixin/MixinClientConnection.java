package me.drake.viecheats.mixin;

import io.netty.channel.ChannelHandlerContext;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.ReceivePacketEvent;
import me.drake.viecheats.core.event.events.SendPacketEvent;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2535.class)
public class MixinClientConnection {
    @Inject(method = "method_10743(Lnet/minecraft/class_2596;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(class_2596 packet, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        SendPacketEvent event = new SendPacketEvent(packet);
        instance.getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "method_10770(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/class_2596;)V", at = @At("HEAD"), cancellable = true)
    private void onChannelRead0(ChannelHandlerContext ctx, class_2596 packet, CallbackInfo ci) {
        try {
            VieCheatsMod instance = VieCheatsMod.getInstance();
            if (instance == null) {
                return;
                try {
                }
                ReceivePacketEvent event = new ReceivePacketEvent(packet);
                instance.getEventBus().post(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
            } catch (Exception ex7) {
                instance = ex7;
            }
        }
    }

}

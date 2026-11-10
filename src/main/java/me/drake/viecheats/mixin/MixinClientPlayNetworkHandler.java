package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.entity.EntityDestroyEvent;
import me.drake.viecheats.events.game.GameJoinedEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(GameJoinedEvent.get());
    }

    @Inject(method = "onEntitiesDestroy", at = @At("HEAD"))
    private void onEntitiesDestroy(EntitiesDestroyS2CPacket packet, CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(EntityDestroyEvent.get(packet.getEntityIds()));
    }
}

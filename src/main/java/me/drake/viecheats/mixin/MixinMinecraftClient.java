package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.game.GameLeftEvent;
import me.drake.viecheats.events.game.OpenScreenEvent;
import me.drake.viecheats.events.game.WindowResizedEvent;
import me.drake.viecheats.events.world.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(TickEvent.Pre.get());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPostTick(CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(TickEvent.Post.get());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent event = VieCheatsMod.EVENT_BUS.post(OpenScreenEvent.get(screen));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(WindowResizedEvent.get());
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        VieCheatsMod.EVENT_BUS.post(GameLeftEvent.get());
    }
}

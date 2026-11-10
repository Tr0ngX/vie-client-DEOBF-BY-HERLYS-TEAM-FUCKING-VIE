package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.util.EntityCache;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public class MixinMinecraftClient {
    @Inject(method = "method_1574()V", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        if (instance.getModuleManager() == null) break label_4;
        boolean needsEntityCache = instance.getModuleManager().hasEnabledEntityCacheConsumers();
        EntityCache.setActive(needsEntityCache);
        if (!(!needsEntityCache)) {
            EntityCache.update();
        }
        instance.getEventBus().post(TickEvent.PRE);
    }

    @Inject(method = "method_1574()V", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        instance.getEventBus().post(TickEvent.POST);
    }

    @Inject(method = "method_1507(Lnet/minecraft/class_437;)V", at = @At("HEAD"))
    private void onSetScreen(class_437 screen, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            freecam.onScreenChanged(screen);
        }
    }

}

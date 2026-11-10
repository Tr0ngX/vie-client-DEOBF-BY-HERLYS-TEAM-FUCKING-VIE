package me.drake.viecheats.mixin;

import java.util.Map;
import java.util.function.Consumer;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import net.minecraft.class_1923;
import net.minecraft.class_2540;
import net.minecraft.class_631;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_631.class)
public class MixinClientChunkManager {
    @Inject(method = "method_16020(IILnet/minecraft/class_2540;Ljava/util/Map;Ljava/util/function/Consumer;)Lnet/minecraft/class_2818;", at = @At("RETURN"))
    private void onChunkLoad(int x, int z, class_2540 buf, Map heightmaps, Consumer consumer, CallbackInfoReturnable cir) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getEventBus() == null)) {
            return;
        }
        instance.getEventBus().post(new ChunkLoadEvent(new class_1923(x, z), true));
    }

    @Inject(method = "method_2859(Lnet/minecraft/class_1923;)V", at = @At("HEAD"))
    private void onChunkUnload(class_1923 pos, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getEventBus() == null)) {
            return;
        }
        instance.getEventBus().post(new ChunkLoadEvent(pos, false));
    }

}

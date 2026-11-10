package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.GameJoinEvent;
import me.drake.viecheats.util.SpawnerEntityCache;
import net.minecraft.class_2487;
import net.minecraft.class_2591;
import net.minecraft.class_2622;
import net.minecraft.class_2678;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "method_11120(Lnet/minecraft/class_2678;)V", at = @At("RETURN"))
    private void onGameJoin(class_2678 packet, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance != null && instance.getEventBus() != null) {
            instance.getEventBus().post(GameJoinEvent.INSTANCE);
        }
    }

    @Inject(method = "method_11094(Lnet/minecraft/class_2622;)V", at = @At("HEAD"))
    private void onBlockEntityUpdate(class_2622 packet, CallbackInfo ci) {
        if (packet.method_11291() != class_2591.field_11889) return;
        class_2487 nbt = packet.method_11290();
        if (nbt == null) return;
        if (!(nbt.method_10545("SpawnData"))) return;
        class_2487 spawnData = nbt.method_68568("SpawnData");
        String id = spawnData.method_68564("id", "");
        if (id != null && !(id.isEmpty())) {
            int dot = id.lastIndexOf(46);
            if (dot >= 0) {
            } else {
            }
            String entityId = id;
            SpawnerEntityCache.put(packet.method_11293(), entityId);
        }
    }

}

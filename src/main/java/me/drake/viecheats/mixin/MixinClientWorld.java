package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.donut.ActiveChunkActivityStore;
import net.minecraft.class_1297;
import net.minecraft.class_1420;
import net.minecraft.class_1421;
import net.minecraft.class_1429;
import net.minecraft.class_1477;
import net.minecraft.class_1480;
import net.minecraft.class_1646;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_638.class)
public class MixinClientWorld {
    @Inject(method = "method_18646(Lnet/minecraft/class_1297;)V", at = @At("HEAD"))
    private void onTickEntity(class_1297 entity, CallbackInfo ci) {
        if (entity == null) return;
        if ((entity instanceof class_1421)) return;
        if ((entity instanceof class_1429)) return;
        if ((entity instanceof class_1646)) return;
        if ((entity instanceof class_1480)) return;
        if ((entity instanceof class_1477)) return;
        if ((entity instanceof class_1420)) return;
        int cx = (entity.method_31477() >> 4);
        int cz = (entity.method_31479() >> 4);
        ActiveChunkActivityStore.recordEntityTick(cx, cz);
    }

}

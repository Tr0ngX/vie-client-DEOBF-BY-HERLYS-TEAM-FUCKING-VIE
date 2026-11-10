package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.ModuleManager;
import net.minecraft.class_315;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_315.class)
public class MixinGameOptions {
    @Inject(method = "method_1636()V", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        ModuleManager mm = instance.getModuleManager();
        if (mm == null) return;
        mm.onGameOptionsLoad();
    }

}

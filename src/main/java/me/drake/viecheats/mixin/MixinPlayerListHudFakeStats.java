package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.client.FakeStats;
import me.drake.viecheats.modules.client.NameProtect;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_355.class)
public class MixinPlayerListHudFakeStats {
    @ModifyVariable(method = "method_1925(Lnet/minecraft/class_2561;)V", at = @At("HEAD"), argsOnly = true, require = 0)
    private class_2561 onRewriteHeader(class_2561 text) {
        class_2561 processed = NameProtect.staticReplaceText(text);
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.TABLIST))) return processed;
        return FakeStats.rewriteText(processed, FakeStats.Surface.TABLIST);
    }

    @ModifyVariable(method = "method_1924(Lnet/minecraft/class_2561;)V", at = @At("HEAD"), argsOnly = true, require = 0)
    private class_2561 onRewriteFooter(class_2561 text) {
        class_2561 processed = NameProtect.staticReplaceText(text);
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.TABLIST))) return processed;
        return FakeStats.rewriteText(processed, FakeStats.Surface.TABLIST);
    }

    @ModifyArg(method = "method_1919(Lnet/minecraft/class_332;ILnet/minecraft/class_269;Lnet/minecraft/class_266;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_27535(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;III)V"), index = 1, require = 0)
    private class_2561 onRewritePlayerName(class_2561 text) {
        class_2561 processed = NameProtect.staticReplaceText(text);
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.TABLIST))) return processed;
        return FakeStats.rewriteText(processed, FakeStats.Surface.TABLIST);
    }

    @ModifyArg(method = "method_1922(Lnet/minecraft/class_266;ILnet/minecraft/class_355$class_9018;IILjava/util/UUID;Lnet/minecraft/class_332;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_27535(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;III)V"), index = 1, require = 0)
    private class_2561 onRewriteObjectiveScore(class_2561 text) {
        class_2561 processed = NameProtect.staticReplaceText(text);
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.TABLIST))) return processed;
        return FakeStats.rewriteText(processed, FakeStats.Surface.TABLIST);
    }

}

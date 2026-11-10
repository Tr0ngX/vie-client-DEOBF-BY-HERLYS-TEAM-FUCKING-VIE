package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.client.FakeStats;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_329;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(class_329.class)
public class MixinInGameHudFakeStats {
    @Shadow
    @Final
    private class_310 field_2035;

    @ModifyVariable(method = "method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V", at = @At("STORE"), ordinal = 0, require = 0)
    private class_2561 onRewriteSidebarTitleAtBuild(class_2561 text) {
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.SIDEBAR))) return text;
        return FakeStats.rewriteSidebarTitle(text);
    }

    @ModifyArgs(method = "method_55439(Lnet/minecraft/class_269;Lnet/minecraft/class_9022;Lnet/minecraft/class_9011;)Lnet/minecraft/class_329$class_9016;", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_329$class_9016;<init>(Lnet/minecraft/class_2561;Lnet/minecraft/class_2561;I)V"), require = 0)
    private void onRewriteSidebarEntryArgs(Args args) {
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.SIDEBAR))) return;
        class_2561 name = ((class_2561) args.get(0));
        class_2561 score = ((class_2561) args.get(1));
        class_2561 rewrittenName = FakeStats.rewriteText(name, FakeStats.Surface.SIDEBAR);
        class_2561 rewrittenScore = FakeStats.rewriteSidebarScoreForName(rewrittenName, score);
        if (rewrittenScore == null) {
        } else {
        }
        int rewrittenScoreWidth = this.field_2035.field_1772.method_27525(rewrittenScore);
        args.set(0, rewrittenName);
        args.set(1, rewrittenScore);
        args.set(2, rewrittenScoreWidth);
    }

    @ModifyArg(method = "method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V", ordinal = 0), index = 1, require = 0)
    private class_2561 onRewriteSidebarTitle(class_2561 text) {
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.SIDEBAR))) return text;
        return FakeStats.rewriteSidebarTitle(text);
    }

    @ModifyArg(method = "method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V", ordinal = 1), index = 1, require = 0)
    private class_2561 onRewriteSidebarName(class_2561 text) {
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.SIDEBAR))) return text;
        return FakeStats.rewriteText(text, FakeStats.Surface.SIDEBAR);
    }

    @ModifyArg(method = "method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V", ordinal = 2), index = 1, require = 0)
    private class_2561 onRewriteSidebarScore(class_2561 text) {
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.SIDEBAR))) return text;
        return FakeStats.rewriteText(text, FakeStats.Surface.SIDEBAR);
    }

}

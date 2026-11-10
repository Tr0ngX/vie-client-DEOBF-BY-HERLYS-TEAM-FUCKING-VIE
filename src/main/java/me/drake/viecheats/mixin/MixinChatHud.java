package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.client.FakeRoles;
import me.drake.viecheats.modules.client.FakeStats;
import me.drake.viecheats.modules.client.NameProtect;
import me.drake.viecheats.util.ChatMessageQueue;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_338.class)
public class MixinChatHud {
    @ModifyVariable(method = "method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V", at = @At("HEAD"), argsOnly = true)
    private class_2561 onRewriteChatStats(class_2561 message) {
        class_2561 processed = message;
        processed = NameProtect.staticReplaceText(processed);
        processed = FakeRoles.replaceInText(processed);
        if (!(FakeStats.isSurfaceActive(FakeStats.Surface.CHAT))) return processed;
        return FakeStats.rewriteText(processed, FakeStats.Surface.CHAT);
    }

    @Inject(method = "method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V", at = @At("HEAD"))
    private void onAddMessage(class_2561 message, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
        if (message == null) return;
        String msgStr = message.getString();
        ChatMessageQueue.push(msgStr);
    }

}

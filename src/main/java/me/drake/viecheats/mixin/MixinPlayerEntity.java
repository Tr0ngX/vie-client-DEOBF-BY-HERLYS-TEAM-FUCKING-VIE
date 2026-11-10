package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.AttackEvent;
import me.drake.viecheats.modules.client.FakeRoles;
import me.drake.viecheats.modules.client.NameProtect;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public class MixinPlayerEntity {
    @Inject(method = "method_7324(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
    private void onAttack(class_1297 target, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        AttackEvent event = new AttackEvent(target);
        instance.getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "method_5477()Lnet/minecraft/class_2561;", at = @At("RETURN"), cancellable = true)
    private void onGetName(CallbackInfoReturnable cir) {
        class_2561 result = ((class_2561) cir.getReturnValue());
        if (result != null) {
            cir.setReturnValue(NameProtect.staticReplaceText(result));
        }
    }

    @Inject(method = "method_5476()Lnet/minecraft/class_2561;", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable cir) {
        class_2561 result = ((class_2561) cir.getReturnValue());
        if (result == null) return;
        if (class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724 == this) {
            result = FakeRoles.replaceInText(result);
        }
        cir.setReturnValue(NameProtect.staticReplaceText(result));
    }

    @Inject(method = "method_5477()Lnet/minecraft/class_2561;", at = @At("RETURN"), cancellable = true)
    private void onGetNameInject(CallbackInfoReturnable cir) {
        class_2561 result = ((class_2561) cir.getReturnValue());
        if (result == null) return;
        if (class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724 == this) {
            result = FakeRoles.replaceInText(result);
        }
    }

}

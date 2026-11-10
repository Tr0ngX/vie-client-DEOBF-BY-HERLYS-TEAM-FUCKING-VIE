package me.drake.viecheats.mixin;

import net.minecraft.class_310;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_742.class)
public abstract class MixinAbstractClientPlayerEntity {
    @Inject(method = "method_52814()Lnet/minecraft/class_8685;", at = @At("HEAD"), cancellable = true)
    private void onGetSkin(CallbackInfoReturnable cir) {
        class_742 self = ((class_742) this);
        if (self.method_5628() != -6969) return;
        class_310 mc = class_310.method_1551();
        if ((mc == null) || (mc.field_1724 == null)) {
            return;
        }
        cir.setReturnValue(mc.field_1724.method_52814());
    }

}

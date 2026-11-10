package me.drake.viecheats.mixin;

import java.util.EnumSet;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import net.minecraft.class_2350;
import net.minecraft.class_852;
import net.minecraft.class_854;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_852.class)
public class MixinChunkOcclusionDataBuilder {
    @Inject(method = "method_3679()Lnet/minecraft/class_854;", at = @At("HEAD"), cancellable = true)
    private void onBuild(CallbackInfoReturnable cir) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) break label_7;
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if ((freecam == null) || (!(freecam.isActive()))) break label_7;
        class_854 data = new class_854();
        data.method_3693(EnumSet.allOf(class_2350.class));
        cir.setReturnValue(data);
        label_7:
    }

}

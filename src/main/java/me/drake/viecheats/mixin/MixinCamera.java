package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.modules.movement.FreeLook;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(class_4184.class)
public class MixinCamera {
    @Shadow
    private boolean field_18709;
    @Unique
    private float vie$tickDelta;

    @Inject(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V", at = @At("HEAD"))
    private void onUpdateHead(class_1937 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        this.vie$tickDelta = tickDelta;
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) mod.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            freecam.onCameraFrame();
        }
    }

    @Inject(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V", at = @At("TAIL"))
    private void onUpdateTail(class_1937 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) mod.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            this.field_18709 = true;
        }
    }

    @ModifyArgs(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19327(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) mod.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            args.set(0, freecam.getInterpolatedX(this.vie$tickDelta));
            args.set(1, freecam.getInterpolatedY(this.vie$tickDelta));
            args.set(2, freecam.getInterpolatedZ(this.vie$tickDelta));
        }
    }

    @ModifyArgs(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19325(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) mod.getModuleManager().getModule(Freecam.class));
        FreeLook freeLook = ((FreeLook) mod.getModuleManager().getModule(FreeLook.class));
        if (freecam != null && freecam.isEnabled()) {
            args.set(0, ((float) freecam.getInterpolatedYaw(this.vie$tickDelta)));
            args.set(1, ((float) freecam.getInterpolatedPitch(this.vie$tickDelta)));
            break label_11;
        }
        if ((freeLook == null) || (!(freeLook.isEnabled()))) break label_11;
        args.set(0, freeLook.cameraYaw);
        args.set(1, freeLook.cameraPitch);
        label_11:
    }

    @Inject(method = "method_19318(F)F", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable cir) {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        FreeLook freeLook = ((FreeLook) mod.getModuleManager().getModule(FreeLook.class));
        if (freeLook != null && freeLook.isEnabled()) {
            cir.setReturnValue(desiredCameraDistance);
        }
    }

    @Inject(method = "method_19333()Z", at = @At("HEAD"), cancellable = true)
    private void onIsThirdPerson(CallbackInfoReturnable cir) {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getModuleManager() == null)) {
            return;
        }
        Freecam freecam = ((Freecam) mod.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

}

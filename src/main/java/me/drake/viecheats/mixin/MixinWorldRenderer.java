package me.drake.viecheats.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.render.VieMeshRenderer;
import me.drake.viecheats.util.EntityCache;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public class MixinWorldRenderer {
    @Inject(method = "method_22710(Lnet/minecraft/class_9922;Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", at = @At("HEAD"))
    private void onRenderHead(class_9922 allocator, class_9779 tickCounter, boolean renderBlockOutline, class_4184 camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f frustumProjectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean spectator, CallbackInfo ci) {
        EntityCache.updateFrustum(positionMatrix, frustumProjectionMatrix, camera);
        VieMeshRenderer.lastPositionMatrix.set(positionMatrix);
        VieMeshRenderer.lastProjectionMatrix.set(projectionMatrix);
        VieMeshRenderer.hasCapturedWorldMatrices = true;
    }

    @ModifyVariable(method = "method_22710(Lnet/minecraft/class_9922;Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private boolean viecheats$forceSpectatorForFreecam(boolean spectator) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return spectator;
        Freecam freecam = ((Freecam) instance.getModuleManager().getModule(Freecam.class));
        if (freecam != null && freecam.isActive()) return true;
        return spectator;
    }

}

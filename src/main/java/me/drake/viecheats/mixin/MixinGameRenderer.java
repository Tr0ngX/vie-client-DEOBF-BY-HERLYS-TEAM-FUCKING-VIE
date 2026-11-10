package me.drake.viecheats.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.render.Render3DEvent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 1) Matrix4f matrix4f, @Local Camera camera) {
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        Render3DEvent event = Render3DEvent.get(matrices, tickCounter.getTickDelta(true), camera.getPos().x, camera.getPos().y, camera.getPos().z);
        VieCheatsMod.EVENT_BUS.post(event);
        RenderSystem.applyModelViewMatrix();
    }
}

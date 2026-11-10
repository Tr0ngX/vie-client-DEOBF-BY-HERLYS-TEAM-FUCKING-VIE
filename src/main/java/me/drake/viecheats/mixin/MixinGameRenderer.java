package me.drake.viecheats.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.Render3DEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.render.VieMeshRenderer;
import me.drake.viecheats.render.ViePipelines;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public abstract class MixinGameRenderer {
    @Shadow
    @Final
    private class_4184 field_18765;

    @Shadow
    protected abstract float method_3196(class_4184 arg0, float arg1, boolean arg2);

    @Shadow
    public abstract Matrix4f method_22973(float arg0);

    @Inject(method = "method_3188(Lnet/minecraft/class_9779;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3695;method_15405(Ljava/lang/String;)V", ordinal = 2))
    private void onRenderWorld(class_9779 tickCounter, CallbackInfo ci) {
        if (ViePipelines.isPrecompiled) break label_2;
        ViePipelines.precompile();
        label_2:
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        if (!(instance.getEventBus().hasSubscribers(RenderEvent.class))) return;
        float tickDelta = tickCounter.method_60637(true);
        try {
            if (!(!VieMeshRenderer.hasCapturedWorldMatrices)) {
                VieMeshRenderer.projection.set(VieMeshRenderer.lastProjectionMatrix);
            } else {
                VieMeshRenderer.projection.set(method_22973(method_3196(this.field_18765, tickDelta, true)));
            }
            VieMeshRenderer.flipFrame();
            if (!(!VieMeshRenderer.hasCapturedWorldMatrices)) {
            } else {
            }
            Matrix4f viewMatrix = new Matrix4f;
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            RenderSystem.getModelViewStack().mul(viewMatrix);
            RenderUtil.begin();
            class_4587 matrices = new class_4587();
            instance.getEventBus().post(new RenderEvent(RenderEvent.Stage.WORLD_3D, tickDelta));
            instance.getEventBus().post(new Render3DEvent(tickDelta, matrices));
            RenderUtil.render(matrices);
            RenderSystem.getModelViewStack().popMatrix();
            break label_27;
            Object var7 = new Matrix4f;
            RenderSystem.getModelViewStack().popMatrix();
            throw var7;
            label_27:
        } catch (Exception ex28) {
            Exception e = ex28;
            VieCheatsMod.LOGGER.error("[VieCheat] Render error", e);
        }
    }

}

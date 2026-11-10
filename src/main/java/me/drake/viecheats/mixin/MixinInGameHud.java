package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.gui.ClickGUI;
import me.drake.viecheats.gui.HudManager;
import me.drake.viecheats.modules.render.SpotiPlayRenderer;
import me.drake.viecheats.modules.render.SpotifyHUD;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_329.class, priority = 1)
public class MixinInGameHud {
    @Inject(method = "method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("TAIL"))
    private void onRender(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) return;
        TessellatorRenderer.setContext(context);
        class_310 mc = class_310.method_1551();
        if (mc != null && (mc.field_1755 instanceof ClickGUI)) return;
        float tickDelta = tickCounter.method_60637(true);
        if (instance.getEventBus().hasSubscribers(RenderEvent.class)) {
            RenderEvent hudEvent = new RenderEvent(RenderEvent.Stage.HUD_2D, tickDelta);
            hudEvent.setDrawContext(context);
            instance.getEventBus().post(hudEvent);
            try {
            }
            SpotifyHUD spotifyHud = ((SpotifyHUD) instance.getModuleManager().getModule(SpotifyHUD.class));
            if (((spotifyHud == null) || (!(spotifyHud.isEnabled()))) || (SpotiPlayRenderer.isEditorActive())) break label_20;
            TessellatorRenderer.setContext(context);
            int sw = mc.method_22683().method_4480();
            int sh = mc.method_22683().method_4507();
            int scaledW = context.method_51421();
            int scaledH = context.method_51443();
            if ((((sw <= 0) || (sh <= 0)) || (scaledW <= 0)) || (scaledH <= 0)) break label_20;
            SpotiPlayRenderer.render(context, sw, sh, scaledW, scaledH, spotifyHud);
            label_20:
        } catch (Throwable ex21) {
            hudEvent = ex21;
        }
        HudManager hud = instance.getHudManager();
        if (hud != null) {
            hud.onHudRender(context, tickDelta);
        }
    }

}

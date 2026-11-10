package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.Modules;
import me.drake.viecheats.modules.misc.FakeStats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHudFakeStats {
    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    private void onRenderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        FakeStats fakeStats = Modules.get().get(FakeStats.class);
        if (fakeStats != null && fakeStats.isActive() && fakeStats.spoofPing()) {
            // Custom ping rendering handled by FakeStats module
        }
    }
}

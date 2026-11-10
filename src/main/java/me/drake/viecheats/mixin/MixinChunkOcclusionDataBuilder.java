package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.world.ChunkOcclusionEvent;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkOcclusionDataBuilder.class)
public class MixinChunkOcclusionDataBuilder {
    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    private void onMarkClosed(BlockPos pos, CallbackInfo ci) {
        ChunkOcclusionEvent event = VieCheatsMod.EVENT_BUS.post(ChunkOcclusionEvent.get());
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

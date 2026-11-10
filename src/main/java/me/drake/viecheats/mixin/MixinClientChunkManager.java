package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.world.ChunkDataEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {
    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onLoadChunkFromPacket(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        WorldChunk chunk = cir.getReturnValue();
        if (chunk != null) {
            VieCheatsMod.EVENT_BUS.post(ChunkDataEvent.get(chunk));
        }
    }
}

package me.drake.viecheats.mixin;

import me.drake.viecheats.modules.Modules;
import me.drake.viecheats.modules.render.NoRender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobSpawnerBlockEntity.class)
public abstract class MixinBlockEntityTickHooks extends BlockEntity {
    public MixinBlockEntityTickHooks(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private static void onSpawnerClientTick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity, CallbackInfo ci) {
        if (world instanceof ClientWorld) {
            NoRender noRender = Modules.get().get(NoRender.class);
            if (noRender != null && noRender.noSpawnerParticles()) {
                ci.cancel();
            }
        }
    }
}

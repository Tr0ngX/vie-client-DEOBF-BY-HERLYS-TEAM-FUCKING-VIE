package me.drake.viecheats.mixin;

import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_636.class)
public interface MixinClientPlayerInteractionManagerAccessor {
    @Accessor("field_3716")
    int getBlockBreakingCooldown();

    @Accessor("field_3716")
    void setBlockBreakingCooldown(int arg0);

}

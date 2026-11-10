package me.drake.viecheats.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_442.class)
public class MixinTitleScreen {
    @Unique
    private static final String BRAND_TEXT = "Vie Client";
    @Unique
    private static final String AUTHOR_TEXT = " by Drake";
    @Unique
    private static final int MARGIN_X = 4;
    @Unique
    private static final int MARGIN_Y = 4;
    @Unique
    private static final int BRAND_COLOR = -38476;
    @Unique
    private static final int AUTHOR_COLOR = -1;
    @Unique
    private static final class_2561 BRAND_LABEL;
    @Unique
    private static final class_2561 AUTHOR_LABEL;

    @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("RETURN"))
    private void vie$renderCredit(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc == null) return;
        class_327 tr = mc.field_1772;
        if (tr == null) return;
        if (mc.field_1755 == null) {
            return;
            try {
            }
            int sw = mc.method_22683().method_4486();
        } catch (IllegalStateException ex7) {
            IllegalStateException ignored = ex7;
            return;
        }
        if (sw <= 0) return;
        int brandWidth = tr.method_1727("Vie Client");
        int authorWidth = tr.method_1727(" by Drake");
        int totalWidth = (brandWidth + authorWidth);
        int x = Math.max(4, ((sw - totalWidth) - 4));
        int y = 4;
        context.method_27535(tr, BRAND_LABEL, x, y, -38476);
        context.method_27535(tr, AUTHOR_LABEL, (x + brandWidth), y, -1);
    }

    static {
        BRAND_LABEL = class_2561.method_43470("Vie Client");
        AUTHOR_LABEL = class_2561.method_43470(" by Drake");
    }

}

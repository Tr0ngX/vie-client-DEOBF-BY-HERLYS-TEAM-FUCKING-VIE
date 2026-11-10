package me.drake.viecheats.modules.render;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.PerformanceHelper;
import me.drake.viecheats.util.RainbowUtil;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1296;
import net.minecraft.class_1308;
import net.minecraft.class_1588;
import net.minecraft.class_238;

public class MobESP extends Module {
    private final EnumSetting filter;
    private static final int HOSTILE_COLOR = -48060;
    private static final int PASSIVE_COLOR = -12255352;
    private static final int NEUTRAL_COLOR = -188;
    private static final int OUTLINE_ALPHA = 180;
    private static final int FILL_ALPHA = 30;

    public MobESP() {
        super("MobESP", "Highlights mobs through walls", Category.RENDER);
        this.filter = ((EnumSetting) addSetting(new EnumSetting("Filter", "Which mobs to show", MobESP.Filter.ALL)));
    }

    public String getInfo() {
        return ((MobESP.Filter) this.filter.get()).name();
    }

    public boolean usesEntityCache() {
        return true;
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.WORLD_3D) return;
        if (!(isInGame())) return;
        float tickDelta = event.getTickDelta();
        int renderCap = PerformanceHelper.scaleRenderCap(96);
        int rendered = 0;
        Object var5 = EntityCache.getEntities().iterator();
        while (var5.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var5.next());
            if (rendered >= renderCap) {
                break label_27;
            }
            if (ce.entity() == mc.field_1724) {
                break label_6;
            }
            boolean isHostile = (ce.entity() instanceof class_1588);
            boolean isPassive = (ce.entity() instanceof class_1296);
            boolean isMob = (ce.entity() instanceof class_1308);
            if (!isMob && !isHostile && !isPassive) {
                break label_6;
            }
            if (this.filter.get() == MobESP.Filter.HOSTILE && !isHostile) {
                break label_6;
            }
            if (this.filter.get() == MobESP.Filter.PASSIVE && !isPassive) {
                break label_6;
            }
            class_238 box = RenderUtil.getInterpolatedBox(ce.entity(), tickDelta);
            if (!(EntityCache.isVisible(box))) {
                break label_6;
            }
            if (!(!isHostile)) {
                int baseColor = -48060;
                break label_22;
            }
            if (!(!isPassive)) {
                baseColor = -12255352;
                break label_22;
            }
            baseColor = -188;
            label_22:
            int outlineCol = RainbowUtil.withAlpha(baseColor, 180);
            RenderUtil.drawBox(box, outlineCol);
            RenderUtil.drawFilledBox(box, RainbowUtil.withAlpha(baseColor, 30));
            rendered++;
        }
        label_27:
    }


    public enum Filter {
        ALL,
        HOSTILE,
        PASSIVE;


        private Filter() {
        }

    }
}

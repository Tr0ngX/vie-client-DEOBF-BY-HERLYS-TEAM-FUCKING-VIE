package me.drake.viecheats.modules.render;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.PerformanceHelper;
import me.drake.viecheats.util.RainbowUtil;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1296;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1657;

public class Tracers extends Module {
    private final EnumSetting filter;
    private final IntSetting range;
    private final IntSetting maxTracers;
    private static final int PLAYER_COLOR = -4160260;
    private static final int HOSTILE_COLOR = -48060;
    private static final int PASSIVE_COLOR = -12255352;
    private static final int OPACITY = 200;

    public Tracers() {
        super("Tracers", "Draws lines to nearby entities", Category.RENDER);
        this.filter = ((EnumSetting) addSetting(new EnumSetting("Filter", "What to draw tracers to", Tracers.Filter.ALL)));
        this.range = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("Range", "Maximum tracer range", 96, 16, 256)))));
        this.maxTracers = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("MaxTracers", "Maximum tracers per frame", 80, 10, 800)))));
    }

    private static Setting hidden(Setting setting) {
        setting.setVisibleInGui(false);
        return setting;
    }

    public String getInfo() {
        return (((Tracers.Filter) this.filter.get()).name() + " " + String.valueOf(this.range.get()) + "m");
    }

    public boolean usesEntityCache() {
        return true;
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.WORLD_3D) return;
        if (!(isInGame())) return;
        float tickDelta = event.getTickDelta();
        double effectiveRange = ((double) ((Integer) this.range.get()).intValue());
        double maxDistSq = (effectiveRange * effectiveRange);
        int drawCap = PerformanceHelper.scaleRenderCap(((Integer) this.maxTracers.get()).intValue());
        int drawn = 0;
        Object var9 = EntityCache.getEntities().iterator();
        while (var9.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var9.next());
            if (ce.distanceSq() > maxDistSq) {
                break label_29;
            }
            if (ce.entity() == mc.field_1724) {
                break label_8;
            }
            if (ce.entity().method_5628() == -6969) {
                break label_8;
            }
            if (!((ce.entity() instanceof class_1309))) {
                break label_8;
            }
            boolean isPlayer = (ce.entity() instanceof class_1657);
            boolean isHostile = (ce.entity() instanceof class_1588);
            boolean isPassive = (ce.entity() instanceof class_1296);
            if (this.filter.get() == Tracers.Filter.PLAYERS && !isPlayer) {
                break label_8;
            }
            if (this.filter.get() == Tracers.Filter.MOBS && !isHostile && !isPassive) {
                break label_8;
            }
            if (!(!isPlayer)) {
                int baseCol = -4160260;
                break label_23;
            }
            if (!(!isHostile)) {
                baseCol = -48060;
                break label_23;
            }
            baseCol = -12255352;
            label_23:
            int col = RainbowUtil.distanceColor(ce.distanceSq(), 16384.0, baseCol, -65536);
            col = RainbowUtil.withAlpha(col, 200);
            RenderUtil.drawTracer(ce.entity(), col, tickDelta);
            drawn++;
            if (drawn >= drawCap) {
                break label_29;
            }
        }
        label_29:
        if ((drawn != 0) || (drawCap <= 0)) break label_43;
        var9 = mc.field_1687.method_18456().iterator();
        while (var9.hasNext()) {
            class_1657 player = ((class_1657) var9.next());
            if (player == mc.field_1724) {
                break label_31;
            }
            if (player.method_5628() == -6969) {
                break label_31;
            }
            if (this.filter.get() == Tracers.Filter.MOBS) {
                break label_31;
            }
            double distSq = mc.field_1724.method_5858(player);
            if (distSq > maxDistSq) {
                break label_31;
            }
            col = RainbowUtil.distanceColor(distSq, 16384.0, -4160260, -65536);
            col = RainbowUtil.withAlpha(col, 200);
            RenderUtil.drawTracer(player, col, tickDelta);
            drawn++;
            if (drawn >= drawCap) {
                break label_43;
            }
        }
        label_43:
    }


    public enum Filter {
        ALL,
        PLAYERS,
        MOBS;


        private Filter() {
        }

    }
}

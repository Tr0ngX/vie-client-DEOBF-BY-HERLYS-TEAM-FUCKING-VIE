package me.drake.viecheats.modules.world;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_239;
import net.minecraft.class_3965;

public class AutoPlace extends Module {
    private final IntSetting delay;
    private final BoolSetting onlyBlocks;
    private int delayTicks;

    public AutoPlace() {
        super("AutoPlace", "Auto-places blocks on timer", Category.WORLD);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between placements", 2, 0, 20, 1)));
        this.onlyBlocks = ((BoolSetting) addSetting(new BoolSetting("OnlyBlocks", "Only place when holding blocks", true)));
        this.delayTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        if (((Boolean) this.onlyBlocks.get()).booleanValue() && !(mc.field_1724.method_6047().method_7909() instanceof class_1747)) return;
        class_239 hit = mc.field_1765;
        if ((hit == null) || (hit.method_17783() != class_239.class_240.field_1332)) {
            return;
        }
        class_3965 blockHit = ((class_3965) hit);
        mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, blockHit);
        mc.field_1724.method_6104(class_1268.field_5808);
        this.delayTicks = ((Integer) this.delay.get()).intValue();
    }

    public String getInfo() {
        return (String.valueOf(this.delay.get()) + "t");
    }

}

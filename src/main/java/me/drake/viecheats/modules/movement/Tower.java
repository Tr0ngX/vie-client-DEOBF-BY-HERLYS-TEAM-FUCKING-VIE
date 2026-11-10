package me.drake.viecheats.modules.movement;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

public class Tower extends Module {
    private final DoubleSetting speedMultiplier;
    private final IntSetting placeDelay;
    private int placeTicks;

    public Tower() {
        super("Tower", "Rapidly builds upward with Scaffold", Category.MOVEMENT);
        this.speedMultiplier = ((DoubleSetting) addSetting(new DoubleSetting("Speed", "Tower speed multiplier", 1.0, 0.5, 1.5, 0.1)));
        this.placeDelay = ((IntSetting) addSetting(new IntSetting("PlaceDelay", "Minimum ticks between placements", 1, 0, 5)));
        this.placeTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        Scaffold scaffold = ((Scaffold) VieCheatsMod.getInstance().getModuleManager().getModule(Scaffold.class));
        if ((scaffold == null) || (!(scaffold.isEnabled()))) {
            return;
        }
        if (!(mc.field_1690.field_1903.method_1434())) return;
        if (!((mc.field_1724.method_6047().method_7909() instanceof class_1747))) return;
        this.placeTicks = (this.placeTicks + 1);
        int delay = Math.max(1, ((int) (2.0 / ((Double) this.speedMultiplier.get()).doubleValue())));
        delay = Math.max(delay, ((Integer) this.placeDelay.get()).intValue());
        if (this.placeTicks < delay) return;
        this.placeTicks = 0;
        class_2338 below = mc.field_1724.method_24515().method_10074();
        class_2680 belowState = mc.field_1687.method_8320(below);
        if (mc.field_1724.method_24828()) {
            mc.field_1724.method_6043();
        }
        if (belowState.method_26215()) {
            placeBlockBelow(below);
        }
    }

    private void placeBlockBelow(class_2338 pos) {
        Object var2 = class_2350.values();
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            class_2350 dir = var2[var4];
            class_2338 neighbor = pos.method_10093(dir);
            class_2680 neighborState = mc.field_1687.method_8320(neighbor);
            if (!(neighborState.method_26215())) {
                class_243 hitVec = class_243.method_24953(neighbor).method_1031((((double) dir.method_10153().method_10148()) * 0.5), (((double) dir.method_10153().method_10164()) * 0.5), (((double) dir.method_10153().method_10165()) * 0.5));
                class_3965 hit = new class_3965(hitVec, dir.method_10153(), neighbor, false);
                mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
                return;
            }
            var4++;
        }
    }

    public String getInfo() {
        return "JUMP";
    }

}

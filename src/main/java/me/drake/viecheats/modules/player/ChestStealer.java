package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_476;
import net.minecraft.class_495;

public class ChestStealer extends Module {
    private final IntSetting delay;
    private final BoolSetting closeWhenDone;
    private int delayTicks;
    private int currentSlot;

    public ChestStealer() {
        super("ChestStealer", "Steals items from open containers", Category.PLAYER);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between steals (min 1 for bypass)", 2, 1, 10)));
        this.closeWhenDone = ((BoolSetting) addSetting(new BoolSetting("CloseWhenDone", "Close container when finished", true)));
        this.delayTicks = 0;
        this.currentSlot = 0;
    }

    public void onEnable() {
        super.onEnable();
        this.currentSlot = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_476 gs = mc.field_1755;
        if ((gs instanceof class_476)) {
            class_476 screen = ((class_476) gs);
        } else {
            if (!((mc.field_1755 instanceof class_495))) return;
        }
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        class_1799 stack = mc.field_1755;
        if ((stack instanceof class_476)) {
            gs = ((class_476) stack);
        } else {
        }
        class_1703 handler = ((class_495) mc.field_1755).method_17577();
        int containerSlots = (handler.field_7761.size() - 36);
        while (this.currentSlot < containerSlots) {
            stack = ((class_1735) handler.field_7761.get(this.currentSlot)).method_7677();
            if (!(stack.method_7960())) {
                mc.field_1761.method_2906(handler.field_7763, this.currentSlot, 0, class_1713.field_7794, mc.field_1724);
                this.currentSlot = (this.currentSlot + 1);
                this.delayTicks = (((Integer) this.delay.get()).intValue() + ((int) (Math.random() * 2.0)));
                return;
            }
            this.currentSlot = (this.currentSlot + 1);
        }
        if (((Boolean) this.closeWhenDone.get()).booleanValue()) {
            mc.field_1724.method_7346();
        }
        this.currentSlot = 0;
        setEnabled(false);
    }

}

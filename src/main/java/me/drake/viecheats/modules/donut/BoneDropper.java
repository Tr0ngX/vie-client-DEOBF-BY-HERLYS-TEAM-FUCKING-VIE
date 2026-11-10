package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class BoneDropper extends Module {
    private final EnumSetting mode;
    private static final int DROP_INTERVAL = 20;
    private int delayTicks;

    public BoneDropper() {
        super("BoneDropper", "Auto-drops bones for wolf taming", Category.DONUT_SMP);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Drop mode", BoneDropper.DropMode.INVENTORY)));
        this.delayTicks = 0;
    }

    public void onEnable() {
        super.onEnable();
        this.delayTicks = 20;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        switch (((BoneDropper.DropMode) this.mode.get()).ordinal()) {
            case 0 -> { break label_6; }
            case 1 -> { break label_7; }
            default -> { break label_8; }
        }
        label_6:
        handleInventoryMode();
        break label_8;
        label_7:
        handleSpawnerMode();
        label_8:
    }

    private void handleInventoryMode() {
        int i = 0;
        while (i < 36) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == class_1802.field_8606 && !(stack.method_7960())) {
                if (i < 9) {
                } else {
                }
                int screenSlot = i;
                mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, screenSlot, 0, class_1713.field_7795, mc.field_1724);
                this.delayTicks = 20;
                return;
            }
            i++;
        }
    }

    private void handleSpawnerMode() {
        int containerSize = mc.field_1724.field_7512;
        if ((containerSize instanceof class_1707)) {
            class_1707 container = ((class_1707) containerSize);
        } else {
            if (mc.field_1765 != null) {
                mc.field_1690.field_1904.method_23481(true);
                this.delayTicks = 20;
                mc.field_1690.field_1904.method_23481(false);
            }
            return;
        }
        containerSize = (container.method_17388() * 9);
        int i = 0;
        while (i < containerSize) {
            if (container.method_7611(i).method_7677().method_31574(class_1802.field_8606)) {
                mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, i, 1, class_1713.field_7795, mc.field_1724);
                this.delayTicks = 20;
                return;
            }
            i++;
        }
        mc.field_1724.method_7346();
        this.delayTicks = 100;
    }

    public String getInfo() {
        return ((BoneDropper.DropMode) this.mode.get()).name();
    }


    public enum DropMode {
        INVENTORY,
        SPAWNER;


        private DropMode() {
        }

    }
}

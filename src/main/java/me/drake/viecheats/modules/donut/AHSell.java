package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class AHSell extends Module {
    private final IntSetting delay;
    private final BoolSetting skipTools;
    private final BoolSetting skipTotems;
    private int delayTicks;
    private int currentSlot;
    private boolean running;

    public AHSell() {
        super("AHSell", "Auto /ah sell hotbar items", Category.DONUT_SMP);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between sells", 30, 10, 60, 1)));
        this.skipTools = ((BoolSetting) addSetting(new BoolSetting("SkipTools", "Skip tools", true)));
        this.skipTotems = ((BoolSetting) addSetting(new BoolSetting("SkipTotems", "Skip totems", true)));
        this.delayTicks = 0;
        this.currentSlot = 0;
        this.running = false;
    }

    public void onEnable() {
        super.onEnable();
        this.currentSlot = 0;
        this.running = true;
        this.delayTicks = 10;
    }

    public void onDisable() {
        super.onDisable();
        this.running = false;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if ((!(isInGame())) || (!this.running)) {
            return;
        }
        if (this.delayTicks <= 0) break label_4;
        this.delayTicks = (this.delayTicks - 1);
        return;
        while (this.currentSlot <= 8) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(this.currentSlot);
            if (!(stack.method_7960()) && !(shouldSkip(stack))) {
                mc.field_1724.method_31548().method_61496(this.currentSlot);
                mc.field_1724.field_3944.method_45730("ah sell");
                this.currentSlot = (this.currentSlot + 1);
                this.delayTicks = ((((Integer) this.delay.get()).intValue() + ((int) (Math.random() * 6.0))) - 3);
                this.delayTicks = Math.max(5, this.delayTicks);
                return;
            }
            this.currentSlot = (this.currentSlot + 1);
        }
        this.running = false;
        setEnabled(false);
    }

    private boolean shouldSkip(class_1799 stack) {
        if (((Boolean) this.skipTotems.get()).booleanValue() && stack.method_7909() == class_1802.field_8288) return true;
        if (((Boolean) this.skipTools.get()).booleanValue() && stack.method_7963()) return true;
        return false;
    }

    public String getInfo() {
        if (!(!this.running)) {
        } else {
        }
        return "Done";
    }

}

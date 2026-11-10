package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1536;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_746;

public class AutoFish extends Module {
    private final BoolSetting stopFull;
    private final IntSetting recastDelay;
    private int waitTicks;
    private boolean waitingToRecast;

    public AutoFish() {
        super("AutoFish", "Automatically catches fish", Category.PLAYER);
        this.stopFull = ((BoolSetting) addSetting(new BoolSetting("StopWhenFull", "Stop when inventory is full", true)));
        this.recastDelay = ((IntSetting) addSetting(new IntSetting("RecastDelay", "Ticks to wait before recasting", 5, 0, 20)));
        this.waitTicks = 0;
        this.waitingToRecast = false;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        class_1799 held = p.method_6047();
        if (!(held.method_7909() instanceof class_1787) && !(p.method_6079().method_7909() instanceof class_1787)) return;
        if (((Boolean) this.stopFull.get()).booleanValue() && isInventoryFull(p)) {
            setEnabled(false);
            return;
        }
        class_1536 bobber = p.field_7513;
        if (!(!this.waitingToRecast)) {
            this.waitTicks = (this.waitTicks - 1);
            if (this.waitTicks <= 0) {
                mc.field_1761.method_2919(p, class_1268.field_5808);
                this.waitingToRecast = false;
            }
            return;
        }
        if (bobber != null && isBiting(bobber)) {
            mc.field_1761.method_2919(p, class_1268.field_5808);
            this.waitingToRecast = true;
            this.waitTicks = ((Integer) this.recastDelay.get()).intValue();
        }
        if (bobber == null) {
            mc.field_1761.method_2919(p, class_1268.field_5808);
        }
    }

    private boolean isBiting(class_1536 bobber) {
        if (!(bobber.method_5799())) break label_1;
        return bobber.method_18798().field_1351 < -0.2;
    }

    private boolean isInventoryFull(class_746 p) {
        int i = 0;
        while (i < 36) {
            if (p.method_31548().method_5438(i).method_7960()) return false;
            i++;
        }
        return true;
    }

}

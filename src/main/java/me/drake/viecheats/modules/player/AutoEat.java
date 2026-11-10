package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_746;

public class AutoEat extends Module {
    private final IntSetting minHunger;
    private final DoubleSetting minHP;
    private final BoolSetting eatWhileMoving;
    private boolean eating;
    private int prevSlot;
    private int eatTicks;

    public AutoEat() {
        super("AutoEat", "Automatically eats food when hungry", Category.PLAYER);
        this.minHunger = ((IntSetting) addSetting(new IntSetting("MinHunger", "Eat when hunger below this", 15, 1, 20)));
        this.minHP = ((DoubleSetting) addSetting(new DoubleSetting("MinHP", "Eat when HP below this regardless of hunger", 8.0, 1.0, 20.0)));
        this.eatWhileMoving = ((BoolSetting) addSetting(new BoolSetting("EatWhileMoving", "Allow eating while moving", true)));
        this.eating = false;
        this.prevSlot = -1;
        this.eatTicks = 0;
    }

    public void onDisable() {
        super.onDisable();
        stopEating();
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        if (!(((Boolean) this.eatWhileMoving.get()).booleanValue()) && p.method_5624()) return;
        float hp = p.method_6032();
        int hunger = p.method_7344().method_7586();
        if (hunger >= ((Integer) this.minHunger.get()).intValue()) {
            if (hp < ((Double) this.minHP.get()).floatValue()) {
            }
        } else {
        }
        boolean shouldEat = false;
        if (!shouldEat) {
            if (!(!this.eating)) {
                stopEating();
            }
            return;
        }
        if (!this.eating) {
            int slot = InventoryUtil.findBestFood(p);
            if (slot == -1) return;
            this.prevSlot = p.method_31548().method_67532();
            p.method_31548().method_61496(slot);
            this.eating = true;
            this.eatTicks = 0;
        }
        mc.field_1690.field_1904.method_23481(true);
        this.eatTicks = (this.eatTicks + 1);
        if (this.eatTicks > 40) {
            stopEating();
        }
    }

    private void stopEating() {
        if (mc != null && mc.field_1690 != null) {
            mc.field_1690.field_1904.method_23481(false);
        }
        if (!(!this.eating) && this.prevSlot != -1 && mc != null && mc.field_1724 != null) {
            mc.field_1724.method_31548().method_61496(this.prevSlot);
        }
        this.eating = false;
        this.prevSlot = -1;
        this.eatTicks = 0;
    }

    public String getInfo() {
        return (!(!this.eating) ? "Eating" : null);
    }

}

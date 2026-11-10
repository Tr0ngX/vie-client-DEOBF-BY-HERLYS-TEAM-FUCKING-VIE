package me.drake.viecheats.modules.combat;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1713;
import net.minecraft.class_1802;

public class AutoTotem extends Module {
    private final EnumSetting mode;
    private final DoubleSetting minHP;
    private final IntSetting swapDelay;
    private final BoolSetting softSwap;
    private int ticksSinceSwap;

    public AutoTotem() {
        super("AutoTotem", "Auto-equip Totem of Undying (legit)", Category.COMBAT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "When to swap totem", AutoTotem.Mode.ALWAYS, AutoTotem.Mode.values())));
        this.minHP = ((DoubleSetting) addSetting(new DoubleSetting("MinHP", "Equip totem below this HP (LOW_HP mode)", 12.0, 1.0, 20.0)));
        this.swapDelay = ((IntSetting) addSetting(new IntSetting("SwapDelay", "Min ticks between swaps (higher = more legit)", 3, 1, 20)));
        this.softSwap = ((BoolSetting) addSetting(new BoolSetting("SoftSwap", "Don't replace items in offhand (only equip when empty)", false)));
        this.ticksSinceSwap = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        this.ticksSinceSwap = (this.ticksSinceSwap + 1);
        if (mc.field_1724.method_6079().method_7909() == class_1802.field_8288) return;
        if (((Boolean) this.softSwap.get()).booleanValue() && !(mc.field_1724.method_6079().method_7960())) return;
        if (this.mode.get() == AutoTotem.Mode.LOW_HP) {
            float hp = (mc.field_1724.method_6032() + mc.field_1724.method_6067());
            if (((double) hp) > ((Double) this.minHP.get()).doubleValue()) return;
        }
        int jitter = ((int) (Math.random() * 3.0));
        if (this.ticksSinceSwap < (((Integer) this.swapDelay.get()).intValue() + jitter)) return;
        if (mc.field_1755 != null) return;
        int totemSlot = InventoryUtil.findInInventory(class_1802.field_8288);
        if (totemSlot == -1) return;
        if (totemSlot < 9) {
        } else {
        }
        int screenSlot = totemSlot;
        int syncId = mc.field_1724.field_7512.field_7763;
        mc.field_1761.method_2906(syncId, screenSlot, 0, class_1713.field_7790, mc.field_1724);
        mc.field_1761.method_2906(syncId, 45, 0, class_1713.field_7790, mc.field_1724);
        if (!(mc.field_1724.field_7512.method_34255().method_7960())) {
            mc.field_1761.method_2906(syncId, screenSlot, 0, class_1713.field_7790, mc.field_1724);
        }
        this.ticksSinceSwap = 0;
    }

    public String getInfo() {
        int count = InventoryUtil.countItem(class_1802.field_8288);
        return String.valueOf(count);
    }


    public enum Mode {
        ALWAYS,
        LOW_HP;


        private Mode() {
        }

    }
}

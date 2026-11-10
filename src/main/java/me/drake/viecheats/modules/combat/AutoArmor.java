package me.drake.viecheats.modules.combat;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1713;

public class AutoArmor extends Module {
    private final BoolSetting onlyBetter;
    private final IntSetting delay;
    private int delayTicks;

    public AutoArmor() {
        super("AutoArmor", "Auto equips best armor", Category.COMBAT);
        this.onlyBetter = ((BoolSetting) addSetting(new BoolSetting("OnlyUpgrade", "Only equip if better than current", true)));
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Ticks between armor swaps", 5, 0, 20)));
        this.delayTicks = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        if (mc.field_1755 != null) return;
        var tmp0 = new int[4];
        tmp0[0] = 39;
        tmp0[1] = 38;
        tmp0[2] = 37;
        tmp0[3] = 36;
        int[] armorSlots = tmp0;
        boolean swapped = false;
        Object var3 = armorSlots;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            int armorSlot = var3[var5];
            int best = InventoryUtil.findBestArmorPiece(mc.field_1724, armorSlot);
            if (best == -1) {
            } else {
                mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, best, 0, class_1713.field_7794, mc.field_1724);
                swapped = true;
                break label_13;
            }
            var5++;
        }
        label_13:
        if (!(!swapped)) {
            this.delayTicks = ((Integer) this.delay.get()).intValue();
        }
    }

}

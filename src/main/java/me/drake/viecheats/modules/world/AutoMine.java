package me.drake.viecheats.modules.world;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

public class AutoMine extends Module {
    public AutoMine() {
        super("AutoMine", "Auto mines the targeted block", Category.WORLD);
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_2338 pos = mc.field_1765;
        if ((pos instanceof class_3965)) {
            class_3965 hit = ((class_3965) pos);
        } else {
            return;
        }
        if (hit.method_17783() != class_239.class_240.field_1332) return;
        pos = hit.method_17777();
        class_2680 state = mc.field_1687.method_8320(pos);
        if (state.method_26215()) return;
        int best = InventoryUtil.findBestTool(state);
        if (best != -1) {
            mc.field_1724.method_31548().method_61496(best);
        }
        mc.field_1761.method_2902(pos, hit.method_17780());
        mc.field_1724.method_6104(class_1268.field_5808);
    }

}

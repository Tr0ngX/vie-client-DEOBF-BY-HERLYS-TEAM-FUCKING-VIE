package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1799;

public class InvManager extends Module {
    private final BoolSetting dropJunk;
    private final BoolSetting autoSort;
    private int tickCounter;

    public InvManager() {
        super("InvManager", "Manages your inventory automatically", Category.PLAYER);
        this.dropJunk = ((BoolSetting) addSetting(new BoolSetting("DropJunk", "Auto drop useless items", false)));
        this.autoSort = ((BoolSetting) addSetting(new BoolSetting("AutoSort", "Sort inventory periodically", false)));
        this.tickCounter = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (mc.field_1755 != null) return;
        this.tickCounter = (this.tickCounter + 1);
        if (this.tickCounter < 40) return;
        this.tickCounter = 0;
        if (((Boolean) this.dropJunk.get()).booleanValue()) {
            dropJunkItems();
        }
    }

    private void dropJunkItems() {
        class_1661 inv = mc.field_1724.method_31548();
        int i = 9;
        while (i < 36) {
            class_1799 stack = inv.method_5438(i);
            if (stack.method_7960()) {
                break label_9;
            }
            String id = stack.method_7909().toString();
            if (!(id.contains("rotten_flesh")) && !(id.contains("poisonous_potato")) && !(id.contains("spider_eye"))) {
                if (!(id.contains("bone_meal"))) break label_9;
            }
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, i, 1, class_1713.field_7795, mc.field_1724);
            label_9:
            i++;
        }
    }

}

package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3965;
import net.minecraft.class_7923;

public class AutoTool extends Module {
    private final BoolSetting pickaxe;
    private final BoolSetting shovel;
    private final BoolSetting onlyWhileMining;

    public AutoTool() {
        super("AutoTool", "Auto switch between pickaxe and shovel while mining", Category.PLAYER);
        this.pickaxe = ((BoolSetting) addSetting(new BoolSetting("Pickaxe", "Auto switch to pickaxe on pickaxe-mineable blocks", true)));
        this.shovel = ((BoolSetting) addSetting(new BoolSetting("Shovel", "Auto switch to shovel on shovel-mineable blocks", true)));
        this.onlyWhileMining = ((BoolSetting) addSetting(new BoolSetting("OnlyMining", "Only switch while left-click mining key is held", true)));
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (!(isEnabled())) return;
        if (((Boolean) this.onlyWhileMining.get()).booleanValue() && !(mc.field_1690.field_1886.method_1434())) return;
        class_2338 targetPos = null;
        AutoTool.ToolKind kind = mc.field_1765;
        if (!((kind instanceof class_3965))) break label_7;
        class_3965 hit = ((class_3965) kind);
        if (hit.method_17783() != class_239.class_240.field_1332) break label_7;
        targetPos = hit.method_17777();
        label_7:
        if (targetPos == null) return;
        class_2680 state = mc.field_1687.method_8320(targetPos);
        if (state.method_26215()) return;
        kind = requiredTool(state);
        if (kind == AutoTool.ToolKind.NONE) return;
        int targetSlot = findBestHotbarToolSlot(kind, state);
        if (targetSlot < 0) return;
        if (mc.field_1724.method_31548().method_67532() != targetSlot) {
            mc.field_1724.method_31548().method_61496(targetSlot);
        }
    }

    public String getInfo() {
        if (((Boolean) this.pickaxe.get()).booleanValue() && ((Boolean) this.shovel.get()).booleanValue()) return "P+S";
        if (((Boolean) this.pickaxe.get()).booleanValue()) return "P";
        if (((Boolean) this.shovel.get()).booleanValue()) return "S";
        return "OFF";
    }

    private AutoTool.ToolKind requiredTool(class_2680 state) {
        if (((Boolean) this.pickaxe.get()).booleanValue() && state.method_26164(class_3481.field_33715)) return AutoTool.ToolKind.PICKAXE;
        if (((Boolean) this.shovel.get()).booleanValue() && state.method_26164(class_3481.field_33716)) return AutoTool.ToolKind.SHOVEL;
        return AutoTool.ToolKind.NONE;
    }

    private int findBestHotbarToolSlot(AutoTool.ToolKind kind, class_2680 state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        int i = 0;
        while (i < 9) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960()) {
                break label_12;
            }
            String itemPath = class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
            if (!(isExpectedToolType(kind, itemPath))) {
                break label_12;
            }
            float speed = stack.method_7924(state);
            if (speed <= bestSpeed) break label_12;
            bestSpeed = speed;
            bestSlot = i;
            label_12:
            i++;
        }
        return bestSlot;
    }

    private boolean isExpectedToolType(AutoTool.ToolKind kind, String itemPath) {
        if ((itemPath == null) || (itemPath.isEmpty())) {
            return false;
        }
        switch (kind.ordinal()) {
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            default -> { break label_5; }
        }
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return false;
    }


    enum ToolKind {
        NONE,
        PICKAXE,
        SHOVEL;


        private ToolKind() {
        }

    }
}

package me.drake.viecheats.util;

import java.util.function.Predicate;
import net.minecraft.class_10192;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4174;
import net.minecraft.class_746;
import net.minecraft.class_9285;
import net.minecraft.class_9334;

public class InventoryUtil {
    private static final class_310 mc;

    public static void switchTo(int slot) {
        if (((mc.field_1724 == null) || (slot < 0)) || (slot > 8)) {
            return;
        }
        mc.field_1724.method_31548().method_61496(slot);
    }

    public static int getSelectedSlot() {
        if (mc.field_1724 == null) return 0;
        return mc.field_1724.method_31548().method_67532();
    }

    public static void clickSlot(int syncId, int slotIndex, int button, class_1713 actionType) {
        if (mc.field_1761 == null) return;
        mc.field_1761.method_2906(syncId, slotIndex, button, actionType, mc.field_1724);
    }

    public static int findInHotbar(class_1792 item) {
        if (mc.field_1724 == null) return -1;
        int i = 0;
        while (i < 9) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) return i;
            i++;
        }
        return -1;
    }

    public static int findInHotbar(Predicate predicate) {
        if (mc.field_1724 == null) return -1;
        int i = 0;
        while (i < 9) {
            if (predicate.test(mc.field_1724.method_31548().method_5438(i))) return i;
            i++;
        }
        return -1;
    }

    public static int findInInventory(class_1792 item) {
        if (mc.field_1724 == null) return -1;
        int i = 0;
        while (i < 36) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) return i;
            i++;
        }
        return -1;
    }

    public static int findBestFood(class_746 player) {
        if (player == null) return -1;
        int bestSlot = -1;
        float bestScore = -1.0f;
        int i = 0;
        while (i < 36) {
            class_1799 stack = player.method_31548().method_5438(i);
            if (stack.method_7960()) {
                break label_16;
            }
            class_4174 food = ((class_4174) stack.method_58694(class_9334.field_50075));
            if (food == null) {
                break label_16;
            }
            float score = (((float) food.comp_2491()) + (food.comp_2492() * 2.0f));
            if (score <= bestScore) break label_16;
            bestScore = score;
            bestSlot = i;
            label_16:
            i++;
        }
        return bestSlot;
    }

    public static int findBestTool(class_2680 state) {
        if (mc.field_1724 == null) return -1;
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        int i = 0;
        while (i < 9) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960()) {
                break label_13;
            }
            float speed = stack.method_7924(state);
            if (speed <= bestSpeed) break label_13;
            bestSpeed = speed;
            bestSlot = i;
            label_13:
            i++;
        }
        return bestSlot;
    }

    public static int findBestArmor(class_1304 slot) {
        if (mc.field_1724 == null) return -1;
        int bestSlot = -1;
        int bestValue = -1;
        int i = 0;
        while (i < 36) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960()) {
                break label_21;
            }
            class_10192 equippable = ((class_10192) stack.method_58694(class_9334.field_54196));
            if (equippable == null) {
                break label_21;
            }
            if (equippable.comp_3174() != slot) {
                break label_21;
            }
            class_9285 protection = ((class_9285) stack.method_58694(class_9334.field_49636));
            if (protection != null) {
            } else {
            }
            int value = 0;
            if (value <= bestValue) break label_21;
            bestValue = value;
            bestSlot = i;
            label_21:
            i++;
        }
        return bestSlot;
    }

    public static int getArmorValue(class_1799 stack) {
        if (stack.method_7960()) return -1;
        class_10192 equippable = ((class_10192) stack.method_58694(class_9334.field_54196));
        if (equippable == null) return -1;
        return 1;
    }

    public static int countItem(class_1792 item) {
        if (mc.field_1724 == null) return 0;
        int count = 0;
        int i = 0;
        while (i < 36) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == item) {
                count = (count + stack.method_7947());
            }
            i++;
        }
        class_1799 offhand = mc.field_1724.method_6079();
        if (offhand.method_7909() == item) {
            count = (count + offhand.method_7947());
        }
        return count;
    }

    public static int findBlockInHotbar() {
        if (mc.field_1724 == null) return -1;
        int i = 0;
        while (i < 9) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!(stack.method_7960()) && (stack.method_7909() instanceof class_1747)) return i;
            i++;
        }
        return -1;
    }

    public static int findBestArmorPiece(class_746 player, int slotIndex) {
        if (player == null) return -1;
        if (slotIndex >= 36) {
            switch (slotIndex) {
                case 36 -> { break label_7; }
                case 37 -> { break label_6; }
                case 38 -> { break label_5; }
                case 39 -> { break label_4; }
                default -> { break label_8; }
            }
            label_4:
            break label_9;
            label_5:
            break label_9;
            label_6:
            break label_9;
            label_7:
            break label_9;
            label_8:
            label_9:
            class_1304 targetSlot = null;
        } else {
            switch (slotIndex) {
                case 0 -> { break label_15; }
                case 1 -> { break label_14; }
                case 2 -> { break label_13; }
                case 3 -> { break label_12; }
                default -> { break label_16; }
            }
            label_12:
            break label_17;
            label_13:
            break label_17;
            label_14:
            break label_17;
            label_15:
            break label_17;
            label_16:
            label_17:
            targetSlot = null;
        }
        if (targetSlot == null) return -1;
        return findBestArmor(targetSlot);
    }

    static {
        mc = class_310.method_1551();
    }

}

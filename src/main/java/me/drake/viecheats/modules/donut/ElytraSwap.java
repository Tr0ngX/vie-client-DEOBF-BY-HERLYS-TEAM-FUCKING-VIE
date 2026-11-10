package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class ElytraSwap extends Module {
    private final EnumSetting trigger;
    private int swapCooldown;
    private static final int SWAP_INTERVAL = 5;

    public ElytraSwap() {
        super("ElytraSwap", "Auto swaps Elytra/Chestplate (DonutSMP)", Category.DONUT_SMP);
        this.trigger = ((EnumSetting) addSetting(new EnumSetting("Trigger", "When to swap", ElytraSwap.Trigger.FALL, ElytraSwap.Trigger.values())));
        this.swapCooldown = 0;
    }

    public boolean usesEntityCache() {
        return true;
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (this.swapCooldown <= 0) break label_2;
        this.swapCooldown = (this.swapCooldown - 1);
        return;
        label_2:
        if (mc.field_1724.method_24828()) break label_3;
        boolean isAirborne = !(mc.field_1724.method_5799());
        boolean nearEnemy = isNearPlayer();
        boolean wantsElytra = false;
        boolean wantsChest = false;
        switch (((ElytraSwap.Trigger) this.trigger.get()).ordinal()) {
            case 0 -> { break label_9; }
            case 1 -> { break label_12; }
            case 2 -> { break label_15; }
            default -> { break label_21; }
        }
        label_9:
        wantsElytra = isAirborne;
        wantsChest = !isAirborne;
        break label_21;
        label_12:
        wantsElytra = !nearEnemy;
        wantsChest = nearEnemy;
        break label_21;
        label_15:
        if (!isAirborne) break label_16;
        wantsElytra = !nearEnemy;
        if (!nearEnemy) {
            if (!isAirborne) {
            }
        } else {
        }
        wantsChest = false;
        label_21:
        class_1799 chest = mc.field_1724.method_6118(class_1304.field_6174);
        boolean hasElytra = chest.method_7909() == class_1802.field_8833;
        if (!(!wantsElytra) && !hasElytra) {
            int slot = InventoryUtil.findInInventory(class_1802.field_8833);
            if (slot != -1) {
                swapToArmorSlot(slot);
                this.swapCooldown = 5;
            }
            break label_32;
        }
        if ((!wantsChest) || (!hasElytra)) break label_32;
        slot = InventoryUtil.findBestArmorPiece(mc.field_1724, 2);
        if (slot == -1) break label_32;
        swapToArmorSlot(slot);
        this.swapCooldown = 5;
        label_32:
    }

    private void swapToArmorSlot(int inventorySlot) {
        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, inventorySlot, 0, class_1713.field_7794, mc.field_1724);
    }

    private boolean isNearPlayer() {
        double rangeSq = 400.0;
        Object var3 = EntityCache.getEntities().iterator();
        while (var3.hasNext()) {
            EntityCache.CachedEntity cached = ((EntityCache.CachedEntity) var3.next());
            if (cached.entity() == mc.field_1724) {
                break label_2;
            }
            if ((cached.entity() instanceof class_1657) && cached.distanceSq() < rangeSq) return true;
            if (cached.distanceSq() > rangeSq) {
                break label_7;
            }
        }
        label_7:
        return false;
    }

    public String getInfo() {
        return ((ElytraSwap.Trigger) this.trigger.get()).name();
    }


    public enum Trigger {
        FALL,
        COMBAT,
        BOTH;


        private Trigger() {
        }

    }
}

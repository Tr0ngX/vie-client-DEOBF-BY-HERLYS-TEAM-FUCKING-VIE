package me.drake.viecheats.modules.donut;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2397;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3965;

public class AutoTreeFarmer extends Module {
    private final IntSetting radiusXZ;
    private final IntSetting breakDelay;
    private final BoolSetting replant;
    private final BoolSetting autoSwitch;
    private final Queue breakQueue;
    private final Set replantPositions;
    private int delayTicks;
    private int scanCooldown;

    public AutoTreeFarmer() {
        super("AutoTreeFarmer", "Auto-chops trees + replants", Category.DONUT_SMP);
        this.radiusXZ = ((IntSetting) addSetting(new IntSetting("RadiusXZ", "Horizontal scan radius", 8, 1, 16, 1)));
        this.breakDelay = ((IntSetting) addSetting(new IntSetting("BreakDelay", "Ticks between breaks", 2, 0, 10, 1)));
        this.replant = ((BoolSetting) addSetting(new BoolSetting("Replant", "Auto-replant saplings", true)));
        this.autoSwitch = ((BoolSetting) addSetting(new BoolSetting("AutoSwitch", "Auto switch to axe", true)));
        this.breakQueue = new ArrayDeque();
        this.replantPositions = new HashSet();
        this.delayTicks = 0;
        this.scanCooldown = 0;
    }

    public void onDisable() {
        super.onDisable();
        this.breakQueue.clear();
        this.replantPositions.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        if (!(this.breakQueue.isEmpty())) {
            if (this.delayTicks > 0) {
                this.delayTicks = (this.delayTicks - 1);
                return;
            }
            class_2338 target = ((class_2338) this.breakQueue.poll());
            if (target == null) return;
            class_2680 state = mc.field_1687.method_8320(target);
            if (!(isLog(state))) {
                if (!(isLeaves(state))) break label_21;
            }
            if (!(((Boolean) this.autoSwitch.get()).booleanValue())) break label_14;
            int axeSlot = InventoryUtil.findInHotbar((stack) -> {
    if (stack.method_7909() != class_1802.field_8556 && stack.method_7909() != class_1802.field_22025 && stack.method_7909() != class_1802.field_8475 && stack.method_7909() != class_1802.field_8062) {
        if (stack.method_7909() == class_1802.field_8406) {
        }
    } else {
    }
    return false;
});
            if (axeSlot < 0) break label_14;
            mc.field_1724.method_31548().method_61496(axeSlot);
            label_14:
            mc.field_1761.method_2902(target, class_2350.field_11036);
            this.delayTicks = ((Integer) this.breakDelay.get()).intValue();
            if (!(isLog(state))) break label_21;
            class_2338 ground = target.method_10074();
            class_2680 groundState = mc.field_1687.method_8320(ground);
            if (groundState.method_26204() != class_2246.field_10566) {
                if (groundState.method_26204() != class_2246.field_10219) break label_21;
            }
            this.replantPositions.add(target);
            label_21:
            return;
        }
        if (((Boolean) this.replant.get()).booleanValue() && !(this.replantPositions.isEmpty())) {
            class_2338 pos = ((class_2338) this.replantPositions.iterator().next());
            this.replantPositions.remove(pos);
            if (!(mc.field_1687.method_8320(pos).method_26215())) break label_32;
            int saplingSlot = InventoryUtil.findInHotbar((stack) -> {
    if (stack.method_7909() != class_1802.field_17535 && stack.method_7909() != class_1802.field_17536 && stack.method_7909() != class_1802.field_17537 && stack.method_7909() != class_1802.field_17538 && stack.method_7909() != class_1802.field_17539) {
        if (stack.method_7909() == class_1802.field_17540) {
        }
    } else {
    }
    return false;
});
            if (saplingSlot < 0) break label_32;
            mc.field_1724.method_31548().method_61496(saplingSlot);
            class_3965 hit = new class_3965(class_243.method_24953(pos.method_10074()), class_2350.field_11036, pos.method_10074(), false);
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
            label_32:
            return;
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown = (this.scanCooldown - 1);
            return;
        }
        this.scanCooldown = 40;
        scanForTrees();
    }

    private void scanForTrees() {
        class_2338 center = mc.field_1724.method_24515();
        int radius = ((Integer) this.radiusXZ.get()).intValue();
        int x = -(radius);
        while (x <= radius) {
            int z = -(radius);
            while (z <= radius) {
                int y = 0;
                while (y <= (radius * 2)) {
                    class_2338 pos = center.method_10069(x, y, z);
                    class_2680 state = mc.field_1687.method_8320(pos);
                    if (isLog(state)) {
                        floodFillTree(pos);
                        return;
                    }
                    y++;
                }
                z++;
            }
            x++;
        }
    }

    private void floodFillTree(class_2338 start) {
        Set visited = new HashSet();
        Queue queue = new ArrayDeque();
        queue.add(start);
        visited.add(start);
        int maxBlocks = 256;
        while (!(queue.isEmpty()) && visited.size() < maxBlocks) {
            class_2338 pos = ((class_2338) queue.poll());
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!(isLog(state))) {
                if (!(isLeaves(state))) break label_19;
            }
            this.breakQueue.add(pos);
            Object var7 = class_2350.values();
            int var8 = var7.length;
            int var9 = 0;
            while (var9 < var8) {
                class_2350 dir = var7[var9];
                class_2338 neighbor = pos.method_10093(dir);
                if (visited.contains(neighbor)) break label_18;
                class_2680 nState = mc.field_1687.method_8320(neighbor);
                if (!(isLog(nState))) {
                    if (!(isLeaves(nState))) break label_18;
                }
                visited.add(neighbor);
                queue.add(neighbor);
                label_18:
                var9++;
            }
            label_19:
        }
    }

    private boolean isLog(class_2680 state) {
        return state.method_26164(class_3481.field_15475);
    }

    private boolean isLeaves(class_2680 state) {
        return (state.method_26204() instanceof class_2397);
    }

    public String getInfo() {
        if (!(this.breakQueue.isEmpty())) return (this.breakQueue.size() + " blocks");
        return null;
    }

}

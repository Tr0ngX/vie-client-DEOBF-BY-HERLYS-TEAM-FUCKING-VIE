package me.drake.viecheats.modules.combat;

import java.util.List;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.EntityUtil;
import me.drake.viecheats.util.InventoryUtil;
import me.drake.viecheats.util.RotationUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1802;

public class SpearAura extends Module {
    private final BoolSetting autoCharge;
    private final IntSetting chargeTime;
    private final BoolSetting autoThrust;
    private final DoubleSetting range;
    private final EnumSetting targetMode;
    private final BoolSetting silentAim;
    private final DoubleSetting rotationSpeed;
    private final BoolSetting autoSwitch;
    private class_1309 currentTarget;
    private int chargeTicks;
    private boolean isCharging;
    private int previousSlot;

    public SpearAura() {
        super("SpearAura", "Auto-attack with Spear (1.21.11)", Category.COMBAT);
        this.autoCharge = ((BoolSetting) addSetting(new BoolSetting("AutoCharge", "Auto hold right-click to charge", true)));
        this.chargeTime = ((IntSetting) addSetting(new IntSetting("ChargeTime", "Charge duration in ticks", 20, 10, 40, 1)));
        this.autoThrust = ((BoolSetting) addSetting(new BoolSetting("AutoThrust", "Auto release at full charge", true)));
        this.range = ((DoubleSetting) addSetting(new DoubleSetting("Range", "Attack range", 4.5, 2.0, 6.0, 0.1)));
        this.targetMode = ((EnumSetting) addSetting(new EnumSetting("TargetMode", "Target selection", SpearAura.TargetMode.CLOSEST)));
        this.silentAim = ((BoolSetting) addSetting(new BoolSetting("SilentAim", "Server-side rotation only", true)));
        this.rotationSpeed = ((DoubleSetting) addSetting(new DoubleSetting("RotationSpeed", "Rotation smoothing (lower = more human)", 0.5, 0.1, 1.0)));
        this.autoSwitch = ((BoolSetting) addSetting(new BoolSetting("AutoSwitch", "Auto switch to Spear", true)));
        this.currentTarget = null;
        this.chargeTicks = 0;
        this.isCharging = false;
        this.previousSlot = -1;
    }

    public boolean usesEntityCache() {
        return true;
    }

    public void onDisable() {
        super.onDisable();
        this.currentTarget = null;
        this.chargeTicks = 0;
        this.isCharging = false;
        if (this.previousSlot >= 0 && mc.field_1724 != null) {
            mc.field_1724.method_31548().method_61496(this.previousSlot);
            this.previousSlot = -1;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        int spearSlot = InventoryUtil.findInHotbar((stack) -> stack.method_7909() == class_1802.field_8547);
        if (spearSlot == -1) return;
        List targets = EntityUtil.getTargets(((Double) this.range.get()).doubleValue(), true, true);
        targets.removeIf((e) -> {
    if (!((e instanceof class_1657))) break label_2;
    class_1657 pe = ((class_1657) e);
    return VieCheatsMod.getInstance().getFriendManager().isFriend(pe.method_7334().name());
});
        if (targets.isEmpty()) {
            this.currentTarget = null;
            resetCharge();
            return;
        }
        switch (((SpearAura.TargetMode) this.targetMode.get()).ordinal()) {
            case 0 -> { break label_12; }
            case 1 -> { break label_13; }
            default -> { break label_11; }
        }
        label_11:
        throw new MatchException(null, null);
        label_12:
        break label_16;
        label_13:
        label_16:
        ((class_1309) targets.getFirst()).currentTarget = ((class_1309) targets.stream().min((a, b) -> Float.compare(a.method_6032(), b.method_6032())).orElse(null));
        if (this.currentTarget == null) return;
        if (((double) this.currentTarget.method_5739(mc.field_1724)) > ((Double) this.range.get()).doubleValue()) return;
        if (((Boolean) this.autoSwitch.get()).booleanValue() && mc.field_1724.method_31548().method_67532() != spearSlot) {
            this.previousSlot = mc.field_1724.method_31548().method_67532();
            mc.field_1724.method_31548().method_61496(spearSlot);
        }
        float[] angles = RotationUtil.calcAngles(this.currentTarget);
        float sensitivity = ((Double) mc.field_1690.method_42495().method_41753()).floatValue();
        float targetYaw = (mc.field_1724.method_36454() + RotationUtil.applyGCD((angles[0] - mc.field_1724.method_36454()), sensitivity));
        float targetPitch = (mc.field_1724.method_36455() + RotationUtil.applyGCD((angles[1] - mc.field_1724.method_36455()), sensitivity));
        float smooth = ((Double) this.rotationSpeed.get()).floatValue();
        targetYaw = RotationUtil.smoothAngle(mc.field_1724.method_36454(), targetYaw, smooth);
        targetPitch = RotationUtil.smoothAngle(mc.field_1724.method_36455(), targetPitch, smooth);
        if (((Boolean) this.silentAim.get()).booleanValue()) {
            KillAura.serverYaw = targetYaw;
            KillAura.serverPitch = targetPitch;
            KillAura.isSilentAiming = true;
        } else {
            mc.field_1724.method_36456(targetYaw);
            mc.field_1724.method_36457(targetPitch);
        }
        if (((Boolean) this.autoCharge.get()).booleanValue()) {
            if (!this.isCharging) {
                this.isCharging = true;
                this.chargeTicks = 0;
            }
            this.chargeTicks = (this.chargeTicks + 1);
            if ((!(((Boolean) this.autoThrust.get()).booleanValue())) || (this.chargeTicks < ((Integer) this.chargeTime.get()).intValue())) break label_53;
            mc.field_1761.method_2918(mc.field_1724, this.currentTarget);
            mc.field_1724.method_6104(class_1268.field_5808);
            resetCharge();
            if (this.previousSlot < 0) break label_53;
            mc.field_1724.method_31548().method_61496(this.previousSlot);
            this.previousSlot = -1;
            break label_53;
        }
        mc.field_1761.method_2918(mc.field_1724, this.currentTarget);
        mc.field_1724.method_6104(class_1268.field_5808);
        label_53:
    }

    private void resetCharge() {
        this.chargeTicks = 0;
        this.isCharging = false;
    }

    public String getInfo() {
        var tmp0 = new Object[1];
        tmp0[0] = this.range.get();
        return String.format("%.1fr", tmp0);
    }


    public enum TargetMode {
        CLOSEST,
        LOWEST_HP;


        private TargetMode() {
        }

    }
}

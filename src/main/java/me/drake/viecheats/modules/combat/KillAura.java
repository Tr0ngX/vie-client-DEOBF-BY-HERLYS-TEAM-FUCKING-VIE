package me.drake.viecheats.modules.combat;

import java.util.List;
import java.util.Random;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.EntityUtil;
import me.drake.viecheats.util.RotationUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;

public class KillAura extends Module {
    private final DoubleSetting range;
    private final DoubleSetting tps;
    private final EnumSetting rotationMode;
    private final EnumSetting targetMode;
    private final EnumSetting filter;
    private final BoolSetting swing;
    private final BoolSetting friendCheck;
    private final BoolSetting antiAfk;
    private final DoubleSetting rotationSmoothing;
    private final DoubleSetting rotationJitter;
    private final BoolSetting sprintReset;
    private final BoolSetting critOnly;
    private final BoolSetting cooldownCheck;
    private final Random random;
    private class_1309 currentTarget;
    private int ticksSinceLastAttack;
    private boolean pendingAttack;
    public static float serverYaw;
    public static float serverPitch;
    public static boolean isSilentAiming;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT);
        this.range = ((DoubleSetting) addSetting(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0, 0.1)));
        this.tps = ((DoubleSetting) addSetting(new DoubleSetting("TPS", "Attacks per second", 10.0, 1.0, 20.0, 0.5)));
        this.rotationMode = ((EnumSetting) addSetting(new EnumSetting("RotationMode", "How to rotate to target", KillAura.RotationMode.SILENT_AIM)));
        this.targetMode = ((EnumSetting) addSetting(new EnumSetting("TargetMode", "Target selection priority", KillAura.TargetMode.CLOSEST)));
        this.filter = ((EnumSetting) addSetting(new EnumSetting("Filter", "Entity filter", KillAura.Filter.PLAYERS)));
        this.swing = ((BoolSetting) addSetting(new BoolSetting("Swing", "Swing arm visually", true)));
        this.friendCheck = ((BoolSetting) addSetting(new BoolSetting("FriendCheck", "Don't attack friends", true)));
        this.antiAfk = ((BoolSetting) addSetting(new BoolSetting("AntiAFK", "Randomize timing slightly", true)));
        this.rotationSmoothing = ((DoubleSetting) addSetting(new DoubleSetting("RotationSmoothing", "Rotation lerp speed (lower = more human)", 0.4, 0.1, 1.0, 0.05)));
        this.rotationJitter = ((DoubleSetting) addSetting(new DoubleSetting("RotationJitter", "Random rotation noise (degrees)", 0.3, 0.0, 2.0, 0.1)));
        this.sprintReset = ((BoolSetting) addSetting(new BoolSetting("SprintReset", "Stop sprint before hit (NCP bypass)", true)));
        this.critOnly = ((BoolSetting) addSetting(new BoolSetting("CritOnly", "Only attack when falling (crits)", false)));
        this.cooldownCheck = ((BoolSetting) addSetting(new BoolSetting("CooldownCheck", "Respect MC attack cooldown to avoid packet spam", true)));
        this.random = new Random();
        this.currentTarget = null;
        this.ticksSinceLastAttack = 0;
        this.pendingAttack = false;
    }

    public boolean usesEntityCache() {
        return true;
    }

    public void onDisable() {
        super.onDisable();
        this.currentTarget = null;
        isSilentAiming = false;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        this.ticksSinceLastAttack = (this.ticksSinceLastAttack + 1);
        if (!(((Boolean) this.critOnly.get()).booleanValue())) break label_8;
        if (mc.field_1724.method_24828()) break label_5;
        if (mc.field_1724.field_6017 >= 0.009999999776482582) break label_6;
        label_5:
        return;
        label_6:
        if ((mc.field_1724.method_6101()) || (mc.field_1724.method_5799())) break label_7;
        if (!(mc.field_1724.method_5765())) break label_8;
        label_7:
        return;
        label_8:
        if (this.filter.get() != KillAura.Filter.PLAYERS) {
            if (this.filter.get() == KillAura.Filter.ALL) {
            }
        } else {
        }
        boolean wantPlayers = false;
        if (this.filter.get() != KillAura.Filter.MOBS) {
            if (this.filter.get() == KillAura.Filter.ALL) {
            }
        } else {
        }
        boolean wantMobs = false;
        List targets = EntityUtil.getTargets(((Double) this.range.get()).doubleValue(), wantPlayers, wantMobs);
        if (((Boolean) this.friendCheck.get()).booleanValue()) {
            targets.removeIf((e) -> {
    if (!((e instanceof class_1657))) break label_2;
    class_1657 pe = ((class_1657) e);
    return VieCheatsMod.getInstance().getFriendManager().isFriend(pe.method_7334().name());
});
        }
        if (targets.isEmpty()) {
            this.currentTarget = null;
            isSilentAiming = false;
            return;
        }
        switch (((KillAura.TargetMode) this.targetMode.get()).ordinal()) {
            case 0 -> { break label_25; }
            case 1 -> { break label_26; }
            case 2 -> { break label_27; }
            case 3 -> { break label_28; }
            default -> { break label_24; }
        }
        label_24:
        throw new MatchException(null, null);
        label_25:
        break label_30;
        label_26:
        break label_30;
        label_27:
        break label_30;
        label_28:
        if (this.currentTarget != null && targets.contains(this.currentTarget)) {
            break label_30;
        }
        label_30:
        this.currentTarget.currentTarget = ((class_1309) targets.getFirst());
        if (this.currentTarget == null) return;
        double effectiveRange = (((Double) this.range.get()).doubleValue() + (this.random.nextGaussian() * 0.07));
        if (((double) this.currentTarget.method_5739(mc.field_1724)) > effectiveRange) return;
        float[] angles = RotationUtil.calcAngles(this.currentTarget);
        float targetYaw = angles[0];
        float targetPitch = angles[1];
        if (((Double) this.rotationJitter.get()).doubleValue() > 0.0) {
            targetYaw = (targetYaw + ((float) (this.random.nextGaussian() * ((Double) this.rotationJitter.get()).doubleValue())));
            targetPitch = (targetPitch + ((float) ((this.random.nextGaussian() * ((Double) this.rotationJitter.get()).doubleValue()) * 0.5)));
        }
        float sensitivity = ((Double) mc.field_1690.method_42495().method_41753()).floatValue();
        targetYaw = (mc.field_1724.method_36454() + RotationUtil.applyGCD((targetYaw - mc.field_1724.method_36454()), sensitivity));
        targetPitch = (mc.field_1724.method_36455() + RotationUtil.applyGCD((targetPitch - mc.field_1724.method_36455()), sensitivity));
        float smoothSpeed = ((Double) this.rotationSmoothing.get()).floatValue();
        targetYaw = RotationUtil.smoothAngle(mc.field_1724.method_36454(), targetYaw, smoothSpeed);
        targetPitch = RotationUtil.smoothAngle(mc.field_1724.method_36455(), targetPitch, smoothSpeed);
        switch (((KillAura.RotationMode) this.rotationMode.get()).ordinal()) {
            case 0 -> { break label_47; }
            case 1 -> { break label_51; }
            case 2 -> { break label_51; }
            case 3 -> { break label_55; }
            default -> { break label_56; }
        }
        label_47:
        serverYaw = targetYaw;
        serverPitch = targetPitch;
        isSilentAiming = true;
        break label_56;
        label_51:
        mc.field_1724.method_36456(targetYaw);
        mc.field_1724.method_36457(targetPitch);
        isSilentAiming = false;
        break label_56;
        label_55:
        isSilentAiming = false;
        label_56:
        if (!(!this.pendingAttack)) {
            this.pendingAttack = false;
            mc.field_1761.method_2918(mc.field_1724, this.currentTarget);
            if (((Boolean) this.swing.get()).booleanValue()) {
                mc.field_1724.method_6104(class_1268.field_5808);
            }
            return;
        }
        int attackDelayTicks = ((int) (20.0 / ((Double) this.tps.get()).doubleValue()));
        if (((Boolean) this.antiAfk.get()).booleanValue()) {
            attackDelayTicks = (attackDelayTicks + ((int) (this.random.nextGaussian() * 1.5)));
            attackDelayTicks = Math.max(1, attackDelayTicks);
        }
        if (((Boolean) this.cooldownCheck.get()).booleanValue()) {
            float cooldown = mc.field_1724.method_7261(0.5f);
            if (cooldown < 0.9f) return;
        }
        if (this.ticksSinceLastAttack < attackDelayTicks) break label_78;
        this.ticksSinceLastAttack = 0;
        if (((Boolean) this.sprintReset.get()).booleanValue() && mc.field_1724.method_5624()) {
            mc.field_1724.method_5728(false);
            this.pendingAttack = true;
            return;
        }
        mc.field_1761.method_2918(mc.field_1724, this.currentTarget);
        if (!(((Boolean) this.swing.get()).booleanValue())) break label_78;
        mc.field_1724.method_6104(class_1268.field_5808);
        label_78:
    }

    public String getInfo() {
        var tmp0 = new Object[1];
        tmp0[0] = this.range.get();
        return String.format("%.1fr", tmp0);
    }

    static {
        serverYaw = 0.0f;
        serverPitch = 0.0f;
        isSilentAiming = false;
    }


    public enum Filter {
        PLAYERS,
        MOBS,
        ALL;


        private Filter() {
        }

    }

    public enum RotationMode {
        SILENT_AIM,
        BODY,
        HEAD,
        NONE;


        private RotationMode() {
        }

    }

    public enum TargetMode {
        CLOSEST,
        LOWEST_HP,
        HIGHEST_HP,
        SINGLE;


        private TargetMode() {
        }

    }
}

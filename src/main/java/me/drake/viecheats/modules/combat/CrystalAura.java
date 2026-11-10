package me.drake.viecheats.modules.combat;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.RotationUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;

public class CrystalAura extends Module {
    private final DoubleSetting placeRange;
    private final DoubleSetting breakRange;
    private final EnumSetting mode;
    private final DoubleSetting minDamage;
    private final DoubleSetting maxSelfDamage;
    private final IntSetting placeDelay;
    private final IntSetting breakDelay;
    private final BoolSetting rotate;
    private final DoubleSetting rotationSpeed;
    private final BoolSetting swingHand;
    private final BoolSetting antiSuicide;
    private int breakTicks;
    private int placeTicks;

    public CrystalAura() {
        super("CrystalAura", "Auto place and break end crystals", Category.COMBAT);
        this.placeRange = ((DoubleSetting) addSetting(new DoubleSetting("PlaceRange", "Crystal placement range", 4.0, 2.0, 6.0)));
        this.breakRange = ((DoubleSetting) addSetting(new DoubleSetting("BreakRange", "Crystal break range", 4.5, 2.0, 6.0)));
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Timing mode", CrystalAura.Mode.SAFE)));
        this.minDamage = ((DoubleSetting) addSetting(new DoubleSetting("MinDamage", "Min damage to target", 6.0, 0.0, 36.0)));
        this.maxSelfDamage = ((DoubleSetting) addSetting(new DoubleSetting("MaxSelfDamage", "Max self damage", 8.0, 0.0, 36.0)));
        this.placeDelay = ((IntSetting) addSetting(new IntSetting("PlaceDelay", "Ticks between placements", 2, 0, 10)));
        this.breakDelay = ((IntSetting) addSetting(new IntSetting("BreakDelay", "Ticks between breaks", 2, 0, 5)));
        this.rotate = ((BoolSetting) addSetting(new BoolSetting("Rotate", "Rotate to crystal", true)));
        this.rotationSpeed = ((DoubleSetting) addSetting(new DoubleSetting("RotationSpeed", "Rotation smoothing (lower = more human)", 0.5, 0.1, 1.0)));
        this.swingHand = ((BoolSetting) addSetting(new BoolSetting("SwingHand", "Swing hand on break", true)));
        this.antiSuicide = ((BoolSetting) addSetting(new BoolSetting("AntiSuicide", "Don't place if would kill self", true)));
        this.breakTicks = 0;
        this.placeTicks = 0;
    }

    public boolean usesEntityCache() {
        return true;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        this.breakTicks = (this.breakTicks + 1);
        this.placeTicks = (this.placeTicks + 1);
        if (this.breakTicks < ((Integer) this.breakDelay.get()).intValue()) break label_27;
        Object var2 = EntityCache.getEntities().iterator();
        label_6:
        if (!(var2.hasNext())) break label_27;
        EntityCache.CachedEntity cached = ((EntityCache.CachedEntity) var2.next());
        class_1297 entity = cached.entity();
        if (!((entity instanceof class_1511))) break label_6;
        class_1511 crystal = ((class_1511) entity);
        if (((double) crystal.method_5739(mc.field_1724)) > ((Double) this.breakRange.get()).doubleValue()) {
            break label_27;
        }
        if (((Boolean) this.rotate.get()).booleanValue()) {
            float[] angles = RotationUtil.calcAngles(crystal);
            float sensitivity = ((Double) mc.field_1690.method_42495().method_41753()).floatValue();
            float targetYaw = (mc.field_1724.method_36454() + RotationUtil.applyGCD((angles[0] - mc.field_1724.method_36454()), sensitivity));
            float targetPitch = (mc.field_1724.method_36455() + RotationUtil.applyGCD((angles[1] - mc.field_1724.method_36455()), sensitivity));
            float smooth = ((Double) this.rotationSpeed.get()).floatValue();
            mc.field_1724.method_36456(RotationUtil.smoothAngle(mc.field_1724.method_36454(), targetYaw, smooth));
            mc.field_1724.method_36457(RotationUtil.smoothAngle(mc.field_1724.method_36455(), targetPitch, smooth));
        }
        mc.field_1761.method_2918(mc.field_1724, crystal);
        if (((Boolean) this.swingHand.get()).booleanValue()) {
            mc.field_1724.method_6104(class_1268.field_5808);
        }
        this.breakTicks = 0;
        break label_27;
        label_27:
    }

    public String getInfo() {
        return ((CrystalAura.Mode) this.mode.get()).name();
    }


    public enum Mode {
        VANILLA,
        FAST,
        SAFE;


        private Mode() {
        }

    }
}

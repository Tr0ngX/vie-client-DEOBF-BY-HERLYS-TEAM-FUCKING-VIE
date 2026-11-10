package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.ServerSafety;
import net.minecraft.class_2561;
import net.minecraft.class_746;

public class Fly extends Module {
    private final EnumSetting mode;
    private final DoubleSetting speed;
    private final BoolSetting autoDisable;
    private final BoolSetting antiKick;
    private int tickCounter;
    private boolean donutWarned;

    public Fly() {
        super("Fly", "Allows the player to fly", Category.MOVEMENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "How to fly", Fly.Mode.VANILLA, Fly.Mode.values())));
        this.speed = ((DoubleSetting) addSetting(new DoubleSetting("Speed", "Flight speed", 1.0, 0.1, 3.0)));
        this.autoDisable = ((BoolSetting) addSetting(new BoolSetting("AutoDisable", "Disable when touching ground", false)));
        this.antiKick = ((BoolSetting) addSetting(new BoolSetting("AntiKick", "Descend slightly every 40 ticks to avoid fly-kick", true)));
        this.tickCounter = 0;
        this.donutWarned = false;
    }

    public void onDisable() {
        super.onDisable();
        if (!(isInGame())) return;
        this.tickCounter = 0;
        this.donutWarned = false;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        if (ServerSafety.isDonutServer() && this.mode.get() == Fly.Mode.VANILLA) {
            if (!this.donutWarned) {
                this.donutWarned = true;
                p.method_7353(class_2561.method_43470("\u00a7d[VC]\u00a7c Fly VANILLA disabled on Donut safety mode."), false);
            }
            setEnabled(false);
            return;
        }
        if (((Boolean) this.autoDisable.get()).booleanValue() && p.method_24828()) {
            setEnabled(false);
            return;
        }
        this.tickCounter = (this.tickCounter + 1);
        switch (((Fly.Mode) this.mode.get()).ordinal()) {
            case 0 -> { break label_13; }
            case 1 -> { break label_38; }
            default -> { break label_41; }
        }
        label_13:
        double s = (((Double) this.speed.get()).doubleValue() * 0.25);
        double motionY = 0.0;
        if (mc.field_1690.field_1903.method_1434()) {
            motionY = s;
            break label_20;
        }
        if (mc.field_1690.field_1832.method_1434()) {
            motionY = -(s);
            break label_20;
        }
        motionY = 0.0;
        label_20:
        if (((Boolean) this.antiKick.get()).booleanValue() && (this.tickCounter % 40) == 0) {
            motionY = -0.04;
        }
        double forward = 0.0;
        double strafe = 0.0;
        float yaw = ((float) Math.toRadians(((double) p.method_36454())));
        if (mc.field_1690.field_1894.method_1434()) {
            forward = (forward + s);
        }
        if (mc.field_1690.field_1881.method_1434()) {
            forward = (forward - s);
        }
        if (mc.field_1690.field_1913.method_1434()) {
            strafe = (strafe + s);
        }
        if (mc.field_1690.field_1849.method_1434()) {
            strafe = (strafe - s);
        }
        double mx = ((-(Math.sin(((double) yaw))) * forward) - (Math.cos(((double) yaw)) * strafe));
        double mz = ((Math.cos(((double) yaw)) * forward) - (Math.sin(((double) yaw)) * strafe));
        double hLen = Math.sqrt(((mx * mx) + (mz * mz)));
        if (hLen > s) {
            mx = ((mx / hLen) * s);
            mz = ((mz / hLen) * s);
        }
        p.method_18800(mx, motionY, mz);
        p.field_6017 = 0.0;
        break label_41;
        label_38:
        if (!(p.method_24828()) && p.method_18798().field_1351 < -0.05) {
            p.method_18800(p.method_18798().field_1352, -0.04, p.method_18798().field_1350);
        }
        p.field_6017 = 0.0;
        label_41:
    }

    public String getInfo() {
        return ((Fly.Mode) this.mode.get()).name();
    }


    public enum Mode {
        VANILLA,
        GLIDE;


        private Mode() {
        }

    }
}

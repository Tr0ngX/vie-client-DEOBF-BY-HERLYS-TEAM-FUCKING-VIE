package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.ServerSafety;
import net.minecraft.class_243;
import net.minecraft.class_746;

public class Speed extends Module {
    private final EnumSetting mode;
    private final DoubleSetting multiplier;
    private final BoolSetting onlyGround;
    private static final double VANILLA_SPRINT_SPEED = 0.2873;
    private static final double MAX_SAFE_SPEED = 0.42;
    private static final double DONUT_MAX_SAFE_SPEED = 0.34;
    private static final double DONUT_MULTIPLIER_CAP = 1.12;
    private boolean wasOnGround;

    public Speed() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Speed mode", Speed.Mode.STRAFE, Speed.Mode.values())));
        this.multiplier = ((DoubleSetting) addSetting(new DoubleSetting("Multiplier", "Speed multiplier (capped for bypass)", 1.3, 1.0, 1.6)));
        this.onlyGround = ((BoolSetting) addSetting(new BoolSetting("OnlyOnGround", "Only boost on ground", false)));
        this.wasOnGround = false;
    }

    public void onTick() {
        if (!(isInGame())) return;
        class_746 p = mc.field_1724;
        boolean onDonut = ServerSafety.isDonutServer();
        if (((Boolean) this.onlyGround.get()).booleanValue() && !(p.method_24828())) return;
        class_243 vel = p.method_18798();
        double hSpeed = Math.sqrt(((vel.field_1352 * vel.field_1352) + (vel.field_1350 * vel.field_1350)));
        if (!(p.field_3913.field_54155.comp_3159()) && !(p.field_3913.field_54155.comp_3160()) && !(p.field_3913.field_54155.comp_3161())) {
            if (p.field_3913.field_54155.comp_3162()) {
            }
        } else {
        }
        boolean isMoving = false;
        Speed.Mode activeMode = ((Speed.Mode) this.mode.get());
        double activeMultiplier = ((Double) this.multiplier.get()).doubleValue();
        double maxSafeSpeed = 0.42;
        if (!onDonut) break label_22;
        if (activeMode == Speed.Mode.BHOP) {
            activeMode = Speed.Mode.STRAFE;
        }
        activeMultiplier = Math.min(activeMultiplier, 1.12);
        maxSafeSpeed = 0.34;
        if (p.method_24828()) break label_22;
        this.wasOnGround = false;
        return;
        label_22:
        if (!isMoving) {
            this.wasOnGround = p.method_24828();
            return;
        }
        switch (activeMode.ordinal()) {
            case 0 -> { break label_26; }
            case 1 -> { break label_48; }
            case 2 -> { break label_60; }
            default -> { break label_71; }
        }
        label_26:
        double targetSpeed = (0.2873 * activeMultiplier);
        float yaw = ((float) Math.toRadians(((double) p.method_36454())));
        double dx = 0.0;
        double dz = 0.0;
        if (p.field_3913.field_54155.comp_3159()) {
            dx = (dx - Math.sin(((double) yaw)));
            dz = (dz + Math.cos(((double) yaw)));
        }
        if (p.field_3913.field_54155.comp_3160()) {
            dx = (dx + Math.sin(((double) yaw)));
            dz = (dz - Math.cos(((double) yaw)));
        }
        if (p.field_3913.field_54155.comp_3161()) {
            dx = (dx - Math.cos(((double) yaw)));
            dz = (dz - Math.sin(((double) yaw)));
        }
        if (p.field_3913.field_54155.comp_3162()) {
            dx = (dx + Math.cos(((double) yaw)));
            dz = (dz + Math.sin(((double) yaw)));
        }
        double len = Math.sqrt(((dx * dx) + (dz * dz)));
        if (len > 0.0) {
            dx = (dx / len);
            dz = (dz / len);
        }
        double blendFactor = (!(!onDonut) ? 0.22 : 0.3);
        double newVx = (vel.field_1352 + (((dx * targetSpeed) - vel.field_1352) * blendFactor));
        double newVz = (vel.field_1350 + (((dz * targetSpeed) - vel.field_1350) * blendFactor));
        double newHSpeed = Math.sqrt(((newVx * newVx) + (newVz * newVz)));
        if (newHSpeed > maxSafeSpeed) {
            double scale = (maxSafeSpeed / newHSpeed);
            newVx = (newVx * scale);
            newVz = (newVz * scale);
        }
        p.method_18800(newVx, vel.field_1351, newVz);
        break label_71;
        label_48:
        if ((!(p.method_24828())) || (this.wasOnGround)) break label_71;
        p.method_6043();
        double boost = ((activeMultiplier - 1.0) * 0.5);
        newVx = (vel.field_1352 * (1.0 + boost));
        newVz = (vel.field_1350 * (1.0 + boost));
        newHSpeed = Math.sqrt(((newVx * newVx) + (newVz * newVz)));
        if (newHSpeed > maxSafeSpeed) {
            scale = (maxSafeSpeed / newHSpeed);
            newVx = (newVx * scale);
            newVz = (newVz * scale);
        }
        p.method_18800(newVx, p.method_18798().field_1351, newVz);
        break label_71;
        label_60:
        targetSpeed = (0.2873 * activeMultiplier);
        if ((hSpeed >= targetSpeed) || (hSpeed <= 0.01)) break label_71;
        boost = (1.0 + ((activeMultiplier - 1.0) * 0.1));
        newVx = (vel.field_1352 * boost);
        newVz = (vel.field_1350 * boost);
        newHSpeed = Math.sqrt(((newVx * newVx) + (newVz * newVz)));
        if (newHSpeed > maxSafeSpeed) {
            scale = (maxSafeSpeed / newHSpeed);
            newVx = (newVx * scale);
            newVz = (newVz * scale);
        }
        p.method_18800(newVx, vel.field_1351, newVz);
        label_71:
        this.wasOnGround = p.method_24828();
    }

    public String getInfo() {
        var tmp0 = new Object[1];
        tmp0[0] = this.multiplier.get();
        return String.format("%.1fx", tmp0);
    }


    public enum Mode {
        STRAFE,
        BHOP,
        BOOST;


        private Mode() {
        }

    }
}

package me.drake.viecheats.util;

import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public class RotationUtil {
    private static final class_310 mc;

    public static float[] getRotations(class_1297 target) {
        if (mc.field_1724 != null) {
            if (target == null) {
            }
            var tmp0 = new float[2];
            tmp0[0] = 0.0f;
            tmp0[1] = 0.0f;
            return tmp0;
        }
        class_243 eyePos = mc.field_1724.method_33571();
        class_243 targetPos = target.method_33571();
        double dx = (targetPos.field_1352 - eyePos.field_1352);
        double dy = (targetPos.field_1351 - eyePos.field_1351);
        double dz = (targetPos.field_1350 - eyePos.field_1350);
        double dist = Math.sqrt(((dx * dx) + (dz * dz)));
        float yaw = (((float) Math.toDegrees(Math.atan2(dz, dx))) - 90.0f);
        float pitch = ((float) -(Math.toDegrees(Math.atan2(dy, dist))));
        var tmp1 = new float[2];
        tmp1[0] = class_3532.method_15393(yaw);
        tmp1[1] = class_3532.method_15363(pitch, -90.0f, 90.0f);
        return tmp1;
    }

    public static float[] getRotations(class_2338 target, class_746 player) {
        if (player != null) {
            if (target == null) {
            }
            var tmp0 = new float[2];
            tmp0[0] = 0.0f;
            tmp0[1] = 0.0f;
            return tmp0;
        }
        class_243 eyePos = player.method_33571();
        class_243 targetPos = class_243.method_24953(target);
        double dx = (targetPos.field_1352 - eyePos.field_1352);
        double dy = (targetPos.field_1351 - eyePos.field_1351);
        double dz = (targetPos.field_1350 - eyePos.field_1350);
        double dist = Math.sqrt(((dx * dx) + (dz * dz)));
        float yaw = (((float) Math.toDegrees(Math.atan2(dz, dx))) - 90.0f);
        float pitch = ((float) -(Math.toDegrees(Math.atan2(dy, dist))));
        var tmp1 = new float[2];
        tmp1[0] = class_3532.method_15393(yaw);
        tmp1[1] = class_3532.method_15363(pitch, -90.0f, 90.0f);
        return tmp1;
    }

    public static float[] calcAngles(class_1297 target) {
        return getRotations(target);
    }

    public static float applyGCD(float delta, float sensitivity) {
        float f = ((sensitivity * 0.6f) + 0.2f);
        float gcd = (((f * f) * f) * 1.2f);
        return (delta - (delta % gcd));
    }

    public static float smoothAngle(float current, float target, float speed) {
        float diff = class_3532.method_15393((target - current));
        return (current + (diff * Math.min(1.0f, speed)));
    }

    public static float[] smoothRotations(float[] current, float[] target, float speed) {
        float yawDiff = class_3532.method_15393((target[0] - current[0]));
        float pitchDiff = (target[1] - current[1]);
        float yaw = (current[0] + (yawDiff * speed));
        float pitch = (current[1] + (pitchDiff * speed));
        var tmp0 = new float[2];
        tmp0[0] = class_3532.method_15393(yaw);
        tmp0[1] = class_3532.method_15363(pitch, -90.0f, 90.0f);
        return tmp0;
    }

    static {
        mc = class_310.method_1551();
    }

}

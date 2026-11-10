package me.drake.viecheats.util;

import java.util.ArrayList;
import java.util.List;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;

public final class EntityUtil {
    private static final class_310 mc;

    public static boolean passesFilter(class_1309 entity, EntityUtil.Filter filter, boolean friendCheck) {
        if (entity == mc.field_1724) return false;
        if (!(entity.method_5805())) return false;
        if (!(!friendCheck) && (entity instanceof class_1657)) {
            class_1657 p = ((class_1657) entity);
            if (VieCheatsMod.getInstance().getFriendManager().isFriend(p.method_5477().getString())) return false;
        }
        switch (filter.ordinal()) {
            case 0 -> { break label_7; }
            case 1 -> { break label_8; }
            case 2 -> { break label_11; }
            default -> { break label_6; }
        }
        label_6:
        throw new MatchException(null, null);
        label_7:
        break label_12;
        label_8:
        if (!((entity instanceof class_1588))) {
            if ((entity instanceof class_1296)) {
            }
            break label_12;
        }
        break label_12;
        label_11:
        label_12:
        return true;
    }

    public static List getTargets(double range, boolean players, boolean mobs) {
        if (mc.field_1724 == null) break label_1;
        if (mc.field_1687 == null) {
            label_1:
            return List.of();
        }
        List targets = new ArrayList();
        double rangeSq = (range * range);
        Object var7 = EntityCache.getEntities().iterator();
        while (var7.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var7.next());
            if (ce.distanceSq() > rangeSq) {
                break label_12;
            }
            Object var10 = ce.entity();
            if (!((var10 instanceof class_1309))) break label_5;
            class_1309 living = ((class_1309) var10);
            if ((living instanceof class_1657) && !players) {
                break label_5;
            }
            if (!(living instanceof class_1657) && !mobs) {
                break label_5;
            }
            targets.add(living);
        }
        label_12:
        return targets;
    }

    public static class_1657 getClosestPlayer(double range) {
        if (mc.field_1724 == null) break label_1;
        if (mc.field_1687 == null) {
            label_1:
            return null;
        }
        double rangeSq = (range * range);
        Object var4 = EntityCache.getEntities().iterator();
        while (var4.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var4.next());
            if (ce.distanceSq() > rangeSq) {
                break label_10;
            }
            Object var7 = ce.entity();
            if ((var7 instanceof class_1657)) {
                class_1657 p = ((class_1657) var7);
                if (p != mc.field_1724 && p.method_5805()) return p;
            }
        }
        label_10:
        return null;
    }

    public static boolean canSeeEntity(class_1297 target) {
        if ((mc.field_1724 == null) || (mc.field_1687 == null)) {
            return false;
        }
        class_243 eye = mc.field_1724.method_33571();
        class_243 tEye = target.method_33571();
        class_3965 result = mc.field_1687.method_17742(new class_3959(eye, tEye, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, mc.field_1724));
        return result.method_17784().method_1025(tEye) < 1.0;
    }

    public static class_1297 getCrosshairEntity() {
        Object var1 = mc.field_1765;
        if ((var1 instanceof class_3966)) {
            class_3966 ehr = ((class_3966) var1);
            return ehr.method_17782();
        }
        return null;
    }

    static {
        mc = class_310.method_1551();
    }


    public enum Filter {
        PLAYERS,
        MOBS,
        ALL;


        private Filter() {
        }

    }
}

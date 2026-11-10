package me.drake.viecheats.util;

import java.util.Locale;
import net.minecraft.class_310;

public final class ServerSafety {
    public static boolean isDonutServer() {
        String address = currentServerAddressLower();
        if (address.isEmpty()) return false;
        if (!(address.contains("donut")) && !(address.contains("donutsmp")) && !(address.contains("donutsm"))) {
            if (address.contains("donsmp")) {
            }
        } else {
        }
        return false;
    }

    public static String currentServerAddressLower() {
        class_310 mc = class_310.method_1551();
        if (((mc == null) || (mc.method_1558() == null)) || (mc.method_1558().field_3761 == null)) {
            return "";
        }
        return mc.method_1558().field_3761.toLowerCase(Locale.ROOT);
    }

}

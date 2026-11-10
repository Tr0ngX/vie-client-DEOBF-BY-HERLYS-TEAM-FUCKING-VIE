package me.drake.viecheats.core;

/**
 * Purified CoreBridge: Standard Java String Builder Helper (Native JNI Wrapper Removed).
 */
public class CoreBridge {
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
    }

    public static String getTitle() {
        return "VIE_CHEATS";
    }

    public static String getPrefix() {
        return ".";
    }

    public static String buildStr(int... chars) {
        StringBuilder sb = new StringBuilder(chars.length);
        for (int c : chars) {
            sb.append((char) c);
        }
        return sb.toString();
    }
}

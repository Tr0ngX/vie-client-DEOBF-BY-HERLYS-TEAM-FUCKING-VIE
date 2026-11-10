package me.drake.viecheats.util;

import net.minecraft.class_1923;
import net.minecraft.class_2596;
import net.minecraft.class_2797;
import net.minecraft.class_2799;
import net.minecraft.class_2828;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_2886;
import net.minecraft.class_310;

public final class PlayerFootprintUtil {
    private static final PlayerFootprintUtil.FootprintSignal NONE;
    private static final ThreadLocal TLS;
    private static final String[] TARGETING_CMD_PREFIXES;

    public static PlayerFootprintUtil.FootprintSignal inspect(class_2596 packet) {
        if (packet == null) return NONE;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return NONE;
        long playerChunk = class_1923.method_8331((((int) mc.field_1724.method_23317()) >> 4), (((int) mc.field_1724.method_23321()) >> 4));
        if ((packet instanceof class_2846)) {
            class_2846 action = ((class_2846) packet);
            return setSignal(class_1923.method_8331((action.method_12362().method_10263() >> 4), (action.method_12362().method_10260() >> 4)), 8);
        }
        if ((packet instanceof class_2885)) {
            class_2885 interact = ((class_2885) packet);
            return setSignal(class_1923.method_8331((interact.method_12543().method_17777().method_10263() >> 4), (interact.method_12543().method_17777().method_10260() >> 4)), 6);
        }
        if ((packet instanceof class_2797)) {
            class_2797 chat = ((class_2797) packet);
            String msg = chat.comp_945();
            if (msg.length() > 0 && msg.charAt(0) == 47) {
                if (isTargetingCommand(msg)) return setSignal(playerChunk, 7);
                return setSignal(playerChunk, 3);
            }
            return setSignal(playerChunk, 2);
        }
        if ((packet instanceof class_2799)) return setSignal(playerChunk, 2);
        if ((packet instanceof class_2886)) return setSignal(playerChunk, 4);
        if ((packet instanceof class_2828)) {
            class_2828 move = ((class_2828) packet);
            if (!(move.method_36171())) return NONE;
            return setSignal(class_1923.method_8331((((int) move.method_12269(0.0)) >> 4), (((int) move.method_12274(0.0)) >> 4)), 1);
        }
        return NONE;
    }

    private static PlayerFootprintUtil.FootprintSignal setSignal(long chunkKey, int weight) {
        PlayerFootprintUtil.FootprintSignal s = ((PlayerFootprintUtil.FootprintSignal) TLS.get());
        s.hasSignal = true;
        s.chunkKey = chunkKey;
        s.weight = weight;
        return s;
    }

    private static boolean isTargetingCommand(String cmd) {
        int len = cmd.length();
        Object var2 = TARGETING_CMD_PREFIXES;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            String prefix = var2[var4];
            int pl = prefix.length();
            if (len >= pl && cmd.regionMatches(true, 0, prefix, 0, pl)) return true;
            var4++;
        }
        return false;
    }

    static {
        NONE = new PlayerFootprintUtil.FootprintSignal(false, -1L, 0);
        TLS = ThreadLocal.withInitial(PlayerFootprintUtil.FootprintSignal::new);
        var tmp0 = new String[20];
        tmp0[0] = "/home";
        tmp0[1] = "/spawn";
        tmp0[2] = "/tpa";
        tmp0[3] = "/tpahere";
        tmp0[4] = "/sethome";
        tmp0[5] = "/setworldspawn";
        tmp0[6] = "/warp";
        tmp0[7] = "/back";
        tmp0[8] = "/tpr";
        tmp0[9] = "/rtp";
        tmp0[10] = "/accept";
        tmp0[11] = "/deny";
        tmp0[12] = "/sit";
        tmp0[13] = "/pet";
        tmp0[14] = "/jobs";
        tmp0[15] = "/bal";
        tmp0[16] = "/money";
        tmp0[17] = "/w ";
        tmp0[18] = "/msg ";
        tmp0[19] = "/r ";
        TARGETING_CMD_PREFIXES = tmp0;
    }


    public final class FootprintSignal {
        public boolean hasSignal;
        public long chunkKey;
        public int weight;

        public FootprintSignal() {
            this(false, -1L, 0);
        }

        public FootprintSignal(boolean hasSignal, long chunkKey, int weight) {
            this.hasSignal = hasSignal;
            this.chunkKey = chunkKey;
            this.weight = weight;
        }

        public boolean hasSignal() {
            return this.hasSignal;
        }

        public long chunkKey() {
            return this.chunkKey;
        }

        public int weight() {
            return this.weight;
        }

    }
}

package me.drake.viecheats.internal;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public final class MsgpackEncoder {
    public static byte[] encodeMap(Map map) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(256);
        writeMap(out, map);
        return out.toByteArray();
    }

    private static void writeValue(ByteArrayOutputStream out, Object val) {
        if (val != null) break label_2;
        out.write(192);
        break label_29;
        label_2:
        if ((val instanceof Boolean)) {
            out.write((((Boolean) val).booleanValue() ? 195 : 194));
            break label_29;
        }
        if ((val instanceof Byte)) {
            writeInt(out, ((Byte) val).longValue());
            break label_29;
        }
        if ((val instanceof Short)) {
            writeInt(out, ((Short) val).longValue());
            break label_29;
        }
        if ((val instanceof Integer)) {
            writeInt(out, ((Integer) val).longValue());
            break label_29;
        }
        if ((val instanceof Long)) {
            writeInt(out, ((Long) val).longValue());
            break label_29;
        }
        if ((val instanceof Number)) {
            writeFloat64(out, ((Number) val).doubleValue());
            break label_29;
        }
        if ((val instanceof String)) {
            writeString(out, ((String) val));
            break label_29;
        }
        if ((val instanceof byte[])) {
            writeBin(out, ((byte[]) val));
            break label_29;
        }
        if ((val instanceof Map)) {
            Map m = ((Map) val);
            writeMap(out, m);
            break label_29;
        }
        if ((val instanceof Collection)) {
            writeArray(out, ((Collection) val));
            break label_29;
        }
        if ((val instanceof Object[])) {
            writeArray(out, Arrays.asList(((Object[]) val)));
            break label_29;
        }
        writeString(out, val.toString());
        label_29:
    }

    private static void writeMap(ByteArrayOutputStream out, Map map) {
        int size = map.size();
        if (size <= 15) {
            out.write((128 | size));
            break label_12;
        }
        if (size <= 65535) {
            out.write(222);
            out.write(((size >> 8) & 255));
            out.write((size & 255));
            break label_12;
        }
        out.write(223);
        out.write(((size >> 24) & 255));
        out.write(((size >> 16) & 255));
        out.write(((size >> 8) & 255));
        out.write((size & 255));
        label_12:
        Object var3 = map.entrySet().iterator();
        while (var3.hasNext()) {
            Map.Entry entry = ((Map.Entry) var3.next());
            writeString(out, ((String) entry.getKey()));
            writeValue(out, entry.getValue());
        }
    }

    private static void writeArray(ByteArrayOutputStream out, Collection list) {
        int size = list.size();
        if (size <= 15) {
            out.write((144 | size));
            break label_12;
        }
        if (size <= 65535) {
            out.write(220);
            out.write(((size >> 8) & 255));
            out.write((size & 255));
            break label_12;
        }
        out.write(221);
        out.write(((size >> 24) & 255));
        out.write(((size >> 16) & 255));
        out.write(((size >> 8) & 255));
        out.write((size & 255));
        label_12:
        Object var3 = list.iterator();
        while (var3.hasNext()) {
            Object item = var3.next();
            writeValue(out, item);
        }
    }

    private static void writeString(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        if (len <= 31) {
            out.write((160 | len));
            break label_16;
        }
        if (len <= 255) {
            out.write(217);
            out.write(len);
            break label_16;
        }
        if (len <= 65535) {
            out.write(218);
            out.write(((len >> 8) & 255));
            out.write((len & 255));
            break label_16;
        }
        out.write(219);
        out.write(((len >> 24) & 255));
        out.write(((len >> 16) & 255));
        out.write(((len >> 8) & 255));
        out.write((len & 255));
        label_16:
        out.write(bytes, 0, len);
    }

    private static void writeBin(ByteArrayOutputStream out, byte[] bytes) {
        int len = bytes.length;
        if (len <= 255) {
            out.write(196);
            out.write(len);
            break label_13;
        }
        if (len <= 65535) {
            out.write(197);
            out.write(((len >> 8) & 255));
            out.write((len & 255));
            break label_13;
        }
        out.write(198);
        out.write(((len >> 24) & 255));
        out.write(((len >> 16) & 255));
        out.write(((len >> 8) & 255));
        out.write((len & 255));
        label_13:
        out.write(bytes, 0, len);
    }

    private static void writeInt(ByteArrayOutputStream out, long val) {
        if (val >= 0L) {
            if (val > 127L) break label_3;
            out.write(((int) val));
            break label_42;
            label_3:
            if (val <= 255L) {
                out.write(204);
                out.write(((int) val));
                break label_42;
            }
            if (val <= 65535L) {
                out.write(205);
                out.write(((int) ((val >> 8) & 255L)));
                out.write(((int) (val & 255L)));
                break label_42;
            }
            if (val <= 4294967295L) {
                out.write(206);
                out.write(((int) ((val >> 24) & 255L)));
                out.write(((int) ((val >> 16) & 255L)));
                out.write(((int) ((val >> 8) & 255L)));
                out.write(((int) (val & 255L)));
                break label_42;
            }
            out.write(207);
            int i = 56;
            while (i >= 0) {
                out.write(((int) ((val >> i) & 255L)));
                i -= 8;
            }
            break label_42;
        }
        if (val >= -32L) {
            out.write(((int) (val & 255L)));
            break label_42;
        }
        if (val >= -128L) {
            out.write(208);
            out.write(((int) (val & 255L)));
            break label_42;
        }
        if (val >= -32768L) {
            out.write(209);
            out.write(((int) ((val >> 8) & 255L)));
            out.write(((int) (val & 255L)));
            break label_42;
        }
        if (val >= -2147483648L) {
            out.write(210);
            out.write(((int) ((val >> 24) & 255L)));
            out.write(((int) ((val >> 16) & 255L)));
            out.write(((int) ((val >> 8) & 255L)));
            out.write(((int) (val & 255L)));
            break label_42;
        }
        out.write(211);
        i = 56;
        while (i >= 0) {
            out.write(((int) ((val >> i) & 255L)));
            i -= 8;
        }
        label_42:
    }

    private static void writeFloat64(ByteArrayOutputStream out, double val) {
        long bits = Double.doubleToRawLongBits(val);
        out.write(203);
        int i = 56;
        while (i >= 0) {
            out.write(((int) ((bits >> i) & 255L)));
            i -= 8;
        }
    }

}

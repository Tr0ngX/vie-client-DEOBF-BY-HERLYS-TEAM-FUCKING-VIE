package me.drake.viecheats.internal;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MsgpackDecoder {
    public static Map decodeMap(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        Object result = readValue(buf);
        if ((result instanceof Map)) {
            Map map = ((Map) result);
            return map;
        }
        throw new IllegalArgumentException("Top-level msgpack value is not a map");
    }

    private static Object readValue(ByteBuffer buf) {
        int b = (buf.get() & 255);
        if (b <= 127) return b;
        if (b >= 128 && b <= 143) return readMap(buf, (b & 15));
        if (b >= 144 && b <= 159) return readArray(buf, (b & 15));
        if (b >= 160 && b <= 191) return readString(buf, (b & 31));
        if (b == 192) return null;
        if (b == 194) return false;
        if (b == 195) return true;
        if (b == 196) {
            int len = (buf.get() & 255);
            byte[] bytes = new byte[len];
            buf.get(bytes);
            return bytes;
        }
        if (b == 197) {
            len = (buf.getShort() & 65535);
            bytes = new byte[len];
            buf.get(bytes);
            return bytes;
        }
        if (b == 198) {
            len = buf.getInt();
            bytes = new byte[len];
            buf.get(bytes);
            return bytes;
        }
        if (b == 202) {
            buf.position((buf.position() + 4));
            return null;
        }
        if (b == 203) {
            buf.position((buf.position() + 8));
            return null;
        }
        if (b == 204) return (buf.get() & 255);
        if (b == 205) return (buf.getShort() & 65535);
        if (b == 206) return (((long) buf.getInt()) & 4294967295L);
        if (b == 207) return buf.getLong();
        if (b == 208) return buf.get();
        if (b == 209) return buf.getShort();
        if (b == 210) return buf.getInt();
        if (b == 211) return buf.getLong();
        if (b == 217) return readString(buf, (buf.get() & 255));
        if (b == 218) return readString(buf, (buf.getShort() & 65535));
        if (b == 219) return readString(buf, buf.getInt());
        if (b == 220) return readArray(buf, (buf.getShort() & 65535));
        if (b == 221) return readArray(buf, buf.getInt());
        if (b == 222) return readMap(buf, (buf.getShort() & 65535));
        if (b == 223) return readMap(buf, buf.getInt());
        if (b >= 224) return ((byte) b);
        throw new IllegalArgumentException(("Unsupported msgpack type: 0x" + Integer.toHexString(b)));
    }

    private static Map readMap(ByteBuffer buf, int size) {
        Map map = new LinkedHashMap(size);
        int i = 0;
        while (i < size) {
            Object key = readValue(buf);
            Object val = readValue(buf);
            map.put(String.valueOf(key), val);
            i++;
        }
        return map;
    }

    private static List readArray(ByteBuffer buf, int size) {
        List list = new ArrayList(size);
        int i = 0;
        while (i < size) {
            list.add(readValue(buf));
            i++;
        }
        return list;
    }

    private static String readString(ByteBuffer buf, int len) {
        byte[] bytes = new byte[len];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static Map decodeToMap(byte[] data) {
        return decodeMap(data);
    }

}

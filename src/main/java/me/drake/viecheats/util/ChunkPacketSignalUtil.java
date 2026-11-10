package me.drake.viecheats.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1299;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2680;

public final class ChunkPacketSignalUtil {
    private static final Map EXTRACTOR_CACHE;
    private static final Map ITEM_SPAWN_ACCESSOR_CACHE;
    private static final Map ENTITY_REMOVE_ACCESSOR_CACHE;
    private static final Map BLOCK_STATE_ACCESSOR_CACHE;
    private static final Map ITEM_ENTITY_TRACKER;
    private static final long ITEM_TRACKER_TTL_MS = 90000L;
    private static final long ITEM_TRACKER_CLEANUP_INTERVAL_MS = 5000L;
    private static volatile long lastItemTrackerCleanupMs;
    private static final Map PLAYER_DROP_TRACKER;
    private static final long DROP_TRACKER_TTL_MS = 30000L;
    private static final long DROP_TRACKER_CLEANUP_INTERVAL_MS = 5000L;
    private static volatile long lastDropTrackerCleanupMs;
    private static final Map RAPID_SPAWN_SIGNALS;
    private static final long RAPID_SPAWN_TTL_MS = 120000L;
    private static final long RAPID_SPAWN_CLEANUP_INTERVAL_MS = 5000L;
    private static volatile long lastRapidSpawnCleanupMs;
    private static final ChunkPacketSignalUtil.RedstonePacketSignal NO_REDSTONE;
    private static final ChunkPacketSignalUtil.BlockStateSignal NO_BLOCK_STATE;

    public static ChunkPacketSignalUtil.PacketSignal inspect(class_2596 packet) {
        if (packet == null) return ChunkPacketSignalUtil.PacketSignal.NONE;
        cleanupTrackedItemsIfNeeded(System.currentTimeMillis());
        ChunkPacketSignalUtil.PacketSignal itemSignal = inspectItemFlow(packet);
        if (itemSignal.environmentSignal()) return itemSignal;
        Class packetClass = packet.getClass();
        String simpleName = packetClass.getSimpleName();
        String lowerName = simpleName.toLowerCase(Locale.ROOT);
        ChunkPacketSignalUtil.ChunkExtractor extractor = ((ChunkPacketSignalUtil.ChunkExtractor) EXTRACTOR_CACHE.computeIfAbsent(packetClass, this::buildExtractor));
        Long chunkKey = extractor.extract(packet);
        if (chunkKey == null) return ChunkPacketSignalUtil.PacketSignal.NONE;
        int weight = classifyWeight(lowerName);
        if (weight <= 0) {
            weight = classifyFallbackWeight(lowerName, packet.toString());
        }
        if (weight <= 0) return ChunkPacketSignalUtil.PacketSignal.NONE;
        return new ChunkPacketSignalUtil.PacketSignal(true, chunkKey, weight);
    }

    public static ChunkPacketSignalUtil.BlockStateSignal inspectBlockState(class_2596 packet) {
        if (packet == null) return NO_BLOCK_STATE;
        Class packetClass = packet.getClass();
        ChunkPacketSignalUtil.BlockStateAccessor accessor = ((ChunkPacketSignalUtil.BlockStateAccessor) BLOCK_STATE_ACCESSOR_CACHE.computeIfAbsent(packetClass, this::buildBlockStateAccessor));
        class_2680 state = accessor.extract(packet);
        if (state == null) return NO_BLOCK_STATE;
        ChunkPacketSignalUtil.ChunkExtractor extractor = ((ChunkPacketSignalUtil.ChunkExtractor) EXTRACTOR_CACHE.computeIfAbsent(packetClass, this::buildExtractor));
        Long chunkKey = extractor.extract(packet);
        if (chunkKey == null) return NO_BLOCK_STATE;
        return new ChunkPacketSignalUtil.BlockStateSignal(true, chunkKey, state);
    }

    public static ChunkPacketSignalUtil.RedstonePacketSignal inspectRedstoneMetadata(class_2596 packet) {
        if (packet == null) return NO_REDSTONE;
        Class packetClass = packet.getClass();
        String lower = packetClass.getSimpleName().toLowerCase(Locale.ROOT);
        if (!(isRedstoneMetadataPacketName(lower))) return NO_REDSTONE;
        ChunkPacketSignalUtil.ChunkExtractor extractor = ((ChunkPacketSignalUtil.ChunkExtractor) EXTRACTOR_CACHE.computeIfAbsent(packetClass, this::buildExtractor));
        Long chunkKey = extractor.extract(packet);
        if (chunkKey == null) return NO_REDSTONE;
        int weight = 0;
        if (lower.contains("blockevent")) {
            weight = 4;
            break label_20;
        }
        if (lower.contains("blockentity")) {
            weight = 3;
            break label_20;
        }
        if (lower.contains("sectionblocksupdate")) {
            weight = 2;
            break label_20;
        }
        if (!(lower.contains("blockupdate"))) break label_20;
        ChunkPacketSignalUtil.BlockStateAccessor accessor = ((ChunkPacketSignalUtil.BlockStateAccessor) BLOCK_STATE_ACCESSOR_CACHE.computeIfAbsent(packetClass, this::buildBlockStateAccessor));
        class_2680 state = accessor.extract(packet);
        if (state == null) return NO_REDSTONE;
        if (!(isRedstoneState(state))) return NO_REDSTONE;
        weight = 3;
        label_20:
        if (weight <= 0) return NO_REDSTONE;
        return new ChunkPacketSignalUtil.RedstonePacketSignal(true, chunkKey, weight);
    }

    private static ChunkPacketSignalUtil.PacketSignal inspectItemFlow(class_2596 packet) {
        Class packetClass = packet.getClass();
        ChunkPacketSignalUtil.ItemSpawnAccessor spawnAccessor = ((ChunkPacketSignalUtil.ItemSpawnAccessor) ITEM_SPAWN_ACCESSOR_CACHE.computeIfAbsent(packetClass, this::buildItemSpawnAccessor));
        ChunkPacketSignalUtil.ItemSpawnSnapshot snapshot = spawnAccessor.extract(packet);
        if (snapshot != null && !(!snapshot.itemEntity) && snapshot.chunkKey != null) {
            if (snapshot.entityId != null) {
                ITEM_ENTITY_TRACKER.put(snapshot.entityId, new ChunkPacketSignalUtil.ItemEntityTrace(snapshot.chunkKey.longValue(), System.currentTimeMillis()));
            }
            trackRapidSpawn(snapshot.chunkKey.longValue(), System.currentTimeMillis());
            return new ChunkPacketSignalUtil.PacketSignal(true, snapshot.chunkKey, 4);
        }
        if (ITEM_ENTITY_TRACKER.isEmpty()) break label_36;
        ChunkPacketSignalUtil.EntityRemoveAccessor removeAccessor = ((ChunkPacketSignalUtil.EntityRemoveAccessor) ENTITY_REMOVE_ACCESSOR_CACHE.computeIfAbsent(packetClass, this::buildEntityRemoveAccessor));
        int[] ids = removeAccessor.extractIds(packet);
        if (ids.length <= 0) break label_36;
        long now = System.currentTimeMillis();
        Map byChunk = new HashMap();
        Long bestChunk = ids;
        ChunkPacketSignalUtil.RemovalAccumulator best = bestChunk.length;
        int weight = 0;
        while (weight < best) {
            int id = bestChunk[weight];
            ChunkPacketSignalUtil.ItemEntityTrace trace = ((ChunkPacketSignalUtil.ItemEntityTrace) ITEM_ENTITY_TRACKER.remove(id));
            if (trace == null) {
                break label_23;
            }
            long ageMs = (now - trace.createdAtMs);
            ChunkPacketSignalUtil.RemovalAccumulator acc = ((ChunkPacketSignalUtil.RemovalAccumulator) byChunk.computeIfAbsent(trace.chunkKey, (ignored) -> new ChunkPacketSignalUtil.RemovalAccumulator()));
            acc.removedCount = (acc.removedCount + 1);
            if (ageMs > 3500L) break label_23;
            acc.fastRemovedCount = (acc.fastRemovedCount + 1);
            label_23:
            weight++;
        }
        if (byChunk.isEmpty()) break label_36;
        bestChunk = null;
        best = null;
        weight = byChunk.entrySet().iterator();
        while (weight.hasNext()) {
            Map.Entry entry = ((Map.Entry) weight.next());
            if (best != null) {
                if (((ChunkPacketSignalUtil.RemovalAccumulator) entry.getValue()).score() > best.score()) {
                }
                bestChunk = ((Long) entry.getKey());
                best = ((ChunkPacketSignalUtil.RemovalAccumulator) entry.getValue());
            }
        }
        if ((bestChunk == null) || (best == null)) break label_36;
        weight = clamp(((best.removedCount * 2) + (best.fastRemovedCount * 3)), 3, 12);
        return new ChunkPacketSignalUtil.PacketSignal(true, bestChunk, weight);
        label_36:
        return ChunkPacketSignalUtil.PacketSignal.NONE;
    }

    private static void trackRapidSpawn(long chunkKey, long now) {
        cleanupRapidSpawnSignalsIfNeeded(now);
        ChunkPacketSignalUtil.RapidSpawnSignal signal = ((ChunkPacketSignalUtil.RapidSpawnSignal) RAPID_SPAWN_SIGNALS.computeIfAbsent(chunkKey, (ignored) -> new ChunkPacketSignalUtil.RapidSpawnSignal(now)));
        int ageMs = ((int) Math.min((now - signal.firstSpawnMs), 2147483647L));
        int burstMs = ((int) Math.min((now - signal.lastSpawnMs), 2147483647L));
        signal.lastSpawnMs = now;
        signal.spawnCount = (signal.spawnCount + 1);
        if (burstMs <= 800) {
            signal.rapidBurstCount = (signal.rapidBurstCount + 1);
        }
        if (ageMs >= 28000) {
            signal.avgSpawnRate = (((double) signal.spawnCount) / Math.max(1.0, (((double) ageMs) / 1000.0)));
        }
    }

    public static int getRapidSpawnWeight(long chunkKey, long now) {
        ChunkPacketSignalUtil.RapidSpawnSignal signal = ((ChunkPacketSignalUtil.RapidSpawnSignal) RAPID_SPAWN_SIGNALS.get(chunkKey));
        if (signal == null) return 0;
        if ((now - signal.lastSpawnMs) > 120000L) return 0;
        int weight = 0;
        if (signal.spawnCount >= 10) {
            weight += 2;
        }
        if (signal.spawnCount >= 20) {
            weight += 3;
        }
        if (signal.rapidBurstCount >= 3) {
            weight += 4;
        }
        if (signal.rapidBurstCount >= 6) {
            weight += 3;
        }
        if (signal.avgSpawnRate >= 1.5) {
            weight += 2;
        }
        if (signal.avgSpawnRate >= 3.0) {
            weight += 2;
        }
        return clamp(weight, 0, 16);
    }

    public static void recordPlayerDrop(int playerEntityId, long chunkKey, long now) {
        PLAYER_DROP_TRACKER.put(playerEntityId, new ChunkPacketSignalUtil.ItemDropRecord(chunkKey, now));
    }

    public static boolean isLikelyPlayerDrop(int itemEntityId, int dropperEntityId) {
        cleanupDropTrackerIfNeeded(System.currentTimeMillis());
        ChunkPacketSignalUtil.ItemDropRecord drop = ((ChunkPacketSignalUtil.ItemDropRecord) PLAYER_DROP_TRACKER.get(dropperEntityId));
        if (drop == null) return false;
        return (System.currentTimeMillis() - drop.dropMs) <= 2000L;
    }

    private static int classifyWeight(String lowerSimpleName) {
        if (lowerSimpleName.contains("chunkdata")) return 4;
        if ((lowerSimpleName.contains("chunkdelta")) || (lowerSimpleName.contains("sectionblocksupdate"))) {
            return 3;
        }
        if (lowerSimpleName.contains("blockupdate")) return 2;
        if (lowerSimpleName.contains("blockentity")) return 2;
        if (lowerSimpleName.contains("unloadchunk")) return 2;
        if (lowerSimpleName.contains("chunksent")) return 1;
        if (lowerSimpleName.contains("chunkrenderdistancecenter")) return 1;
        if (lowerSimpleName.contains("blockevent")) return 1;
        if (lowerSimpleName.contains("chunk")) return 1;
        return 0;
    }

    private static int classifyFallbackWeight(String lowerSimpleName, String packetText) {
        if (packetText == null) {
        } else {
        }
        String lower = packetText.toLowerCase(Locale.ROOT);
        if (((lower.contains("chunkdata")) || (lower.contains("chunk data"))) || (lower.contains("chunkdelta"))) {
            return 3;
        }
        if (((lower.contains("sectionblocksupdate")) || (lower.contains("blockupdate"))) || (lower.contains("block update"))) {
            return 2;
        }
        if (lower.contains("blockentity")) return 2;
        if (lower.contains("chunk")) return 1;
        if (lowerSimpleName.startsWith("class_")) return 1;
        return 0;
    }

    private static ChunkPacketSignalUtil.ChunkExtractor buildExtractor(Class packetClass) {
        Method chunkPosMethod = findChunkPosMethod(packetClass);
        Method blockPosMethod = findBlockPosMethod(packetClass);
        Method chunkXMethod = findIntMethod(packetClass, true);
        Method chunkZMethod = findIntMethod(packetClass, false);
        Field chunkPosField = findChunkPosField(packetClass);
        Field blockPosField = findBlockPosField(packetClass);
        Field chunkXField = findIntField(packetClass, true);
        Field chunkZField = findIntField(packetClass, false);
        return (packet) -> {
    Long byMethod = extractByMethods(packet, chunkPosMethod, blockPosMethod, chunkXMethod, chunkZMethod);
    if (byMethod != null) return byMethod;
    Long byField = extractByFields(packet, chunkPosField, blockPosField, chunkXField, chunkZField);
    if (byField != null) return byField;
    return extractChunkFromWorldCoords(packet, packetClass);
};
    }

    private static Long extractByMethods(Object packet, Method chunkPosMethod, Method blockPosMethod, Method chunkXMethod, Method chunkZMethod) {
        if (chunkPosMethod == null) break label_4;
        Object value = invoke(packet, chunkPosMethod);
        if (!((value instanceof class_1923))) break label_4;
        class_1923 cp = ((class_1923) value);
        return class_1923.method_8331(cp.field_9181, cp.field_9180);
        label_4:
        if ((chunkXMethod == null) || (chunkZMethod == null)) break label_10;
        Object xValue = invoke(packet, chunkXMethod);
        Object zValue = invoke(packet, chunkZMethod);
        if (!((xValue instanceof Number))) break label_10;
        Number xNum = ((Number) xValue);
        if (!((zValue instanceof Number))) break label_10;
        Number zNum = ((Number) zValue);
        return class_1923.method_8331(xNum.intValue(), zNum.intValue());
        label_10:
        if (blockPosMethod == null) break label_14;
        value = invoke(packet, blockPosMethod);
        if (!((value instanceof class_2338))) break label_14;
        class_2338 bp = ((class_2338) value);
        return class_1923.method_8331((bp.method_10263() >> 4), (bp.method_10260() >> 4));
        label_14:
        return null;
    }

    private static Long extractByFields(Object packet, Field chunkPosField, Field blockPosField, Field chunkXField, Field chunkZField) {
        if (chunkPosField == null) break label_4;
        Object value = read(packet, chunkPosField);
        if (!((value instanceof class_1923))) break label_4;
        class_1923 cp = ((class_1923) value);
        return class_1923.method_8331(cp.field_9181, cp.field_9180);
        label_4:
        if ((chunkXField == null) || (chunkZField == null)) break label_10;
        Object xValue = read(packet, chunkXField);
        Object zValue = read(packet, chunkZField);
        if (!((xValue instanceof Number))) break label_10;
        Number xNum = ((Number) xValue);
        if (!((zValue instanceof Number))) break label_10;
        Number zNum = ((Number) zValue);
        return class_1923.method_8331(xNum.intValue(), zNum.intValue());
        label_10:
        if (blockPosField == null) break label_14;
        value = read(packet, blockPosField);
        if (!((value instanceof class_2338))) break label_14;
        class_2338 bp = ((class_2338) value);
        return class_1923.method_8331((bp.method_10263() >> 4), (bp.method_10260() >> 4));
        label_14:
        return null;
    }

    private static Long extractChunkFromWorldCoords(Object packet, Class packetClass) {
        Method xMethod = findCoordMethod(packetClass, true);
        Method zMethod = findCoordMethod(packetClass, false);
        if (xMethod != null && zMethod != null) {
            Object xObj = invoke(packet, xMethod);
            Object zObj = invoke(packet, zMethod);
            Long chunk = chunkFromCoords(xObj, zObj);
            if (chunk != null) return chunk;
        }
        Field xField = findCoordField(packetClass, true);
        Field zField = findCoordField(packetClass, false);
        if (xField != null && zField != null) {
            xObj = read(packet, xField);
            zObj = read(packet, zField);
            chunk = chunkFromCoords(xObj, zObj);
            if (chunk != null) return chunk;
        }
        return null;
    }

    private static Long chunkFromCoords(Object xObj, Object zObj) {
        if (!((xObj instanceof Number))) break label_3;
        Number xNum = ((Number) xObj);
        if (!((zObj instanceof Number))) break label_3;
        Number zNum = ((Number) zObj);
        break label_4;
        label_3:
        return null;
        label_4:
        int bx = ((int) Math.floor(xNum.doubleValue()));
        int bz = ((int) Math.floor(zNum.doubleValue()));
        return class_1923.method_8331((bx >> 4), (bz >> 4));
    }

    private static Method findCoordMethod(Class type, boolean xAxis) {
        String axis = (!(!xAxis) ? "x" : "z");
        Object var3 = type.getMethods();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Method method = var3[var5];
            if (method.getParameterCount()) {
                break label_15;
            }
            Class returnType = method.getReturnType();
            if (returnType != Double.TYPE && returnType != Double.class && returnType != Float.TYPE) {
                if (returnType == Float.class) {
                }
            } else {
            }
            boolean numeric = false;
            if (!numeric) {
                break label_15;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (((name.endsWith(axis)) || (name.equals(("get" + axis)))) || (name.contains(("position" + axis)))) {
                return method;
            }
            label_15:
            var5++;
        }
        return null;
    }

    private static Field findCoordField(Class type, boolean xAxis) {
        String axis = (!(!xAxis) ? "x" : "z");
        Object var3 = type.getDeclaredFields();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Field field = var3[var5];
            Class fieldType = field.getType();
            if (fieldType != Double.TYPE && fieldType != Double.class && fieldType != Float.TYPE) {
                if (fieldType == Float.class) {
                }
            } else {
            }
            boolean numeric = false;
            if (!numeric) {
                break label_17;
            }
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (!(name.endsWith(axis)) && !(name.equals(axis))) {
                break label_17;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex16) {
                Object var10 = ex16;
                label_17:
                var5++;
            }
            return null;
        }
    }

    private static Method findChunkPosMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_7;
            }
            if (!(class_1923.class.isAssignableFrom(method.getReturnType()))) {
                break label_7;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("chunk") && name.contains("pos")) return method;
            label_7:
            var3++;
        }
        return null;
    }

    private static Method findBlockPosMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_7;
            }
            if (!(class_2338.class.isAssignableFrom(method.getReturnType()))) {
                break label_7;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("pos")) return method;
            label_7:
            var3++;
        }
        return null;
    }

    private static Method findIntMethod(Class type, boolean xAxis) {
        String axis = (!(!xAxis) ? "x" : "z");
        Method axisPriority = null;
        Method genericPriority = null;
        Object var5 = type.getMethods();
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Method method = var5[var7];
            if (method.getParameterCount()) {
                break label_15;
            }
            Class returnType = method.getReturnType();
            if (returnType != Integer.TYPE && returnType != Integer.class) {
                break label_15;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("chunk") && name.endsWith(axis)) return method;
            if (name.endsWith(axis) && axisPriority == null) {
                axisPriority = method;
            }
            if (!(name.equals(("get" + axis)))) {
                if (!(name.equals(axis))) break label_15;
            }
            if (genericPriority != null) break label_15;
            genericPriority = method;
            label_15:
            var7++;
        }
        if (axisPriority != null) return axisPriority;
        return genericPriority;
    }

    private static Field findChunkPosField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            if (!(class_1923.class.isAssignableFrom(field.getType()))) {
                break label_9;
            }
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("chunk"))) {
                if (!(name.contains("pos"))) break label_9;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex8) {
                Object var6 = ex8;
                label_9:
                var3++;
            }
            return null;
        }
    }

    private static Field findBlockPosField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            if (!(class_2338.class.isAssignableFrom(field.getType()))) {
                break label_9;
            }
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("pos"))) break label_9;
            try {
                field.setAccessible(true);
                return field;
            } catch (Exception ex8) {
                Object var6 = ex8;
                label_9:
                var3++;
            }
            return null;
        }
    }

    private static Field findIntField(Class type, boolean xAxis) {
        String axis = (!(!xAxis) ? "x" : "z");
        Object var3 = type.getDeclaredFields();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Field field = var3[var5];
            Class fieldType = field.getType();
            if (fieldType != Integer.TYPE && fieldType != Integer.class) {
                break label_17;
            }
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (name.contains("chunk")) {
                if (name.endsWith(axis)) break label_13;
            }
            if ((name.equals(axis)) || (name.endsWith(("chunk" + axis)))) break label_13;
            break label_17;
            try {
                label_13:
                field.setAccessible(true);
                return field;
            } catch (Exception ex16) {
                Object var9 = ex16;
                label_17:
                var5++;
            }
            return null;
        }
    }

    private static ChunkPacketSignalUtil.ItemSpawnAccessor buildItemSpawnAccessor(Class packetClass) {
        ChunkPacketSignalUtil.ChunkExtractor chunkExtractor = ((ChunkPacketSignalUtil.ChunkExtractor) EXTRACTOR_CACHE.computeIfAbsent(packetClass, this::buildExtractor));
        Method entityIdMethod = findEntityIdMethod(packetClass);
        Field entityIdField = findEntityIdField(packetClass);
        Method entityTypeMethod = findEntityTypeMethod(packetClass);
        Field entityTypeField = findEntityTypeField(packetClass);
        return (packet) -> {
    class_1299 entityType = extractEntityType(packet, entityTypeMethod, entityTypeField);
    boolean isItem = entityType == class_1299.field_6052;
    if (!isItem) return null;
    Long chunk = chunkExtractor.extract(packet);
    if (chunk == null) return null;
    Integer entityId = extractEntityId(packet, entityIdMethod, entityIdField);
    return new ChunkPacketSignalUtil.ItemSpawnSnapshot(entityId, chunk, true);
};
    }

    private static ChunkPacketSignalUtil.EntityRemoveAccessor buildEntityRemoveAccessor(Class packetClass) {
        Method idsMethod = findEntityIdsMethod(packetClass);
        Field idsField = findEntityIdsField(packetClass);
        return (packet) -> {
    Object value = null;
    if (idsMethod != null) {
        value = invoke(packet, idsMethod);
    }
    if (value == null && idsField != null) {
        value = read(packet, idsField);
    }
    if (value == null) return new int[0];
    return toIntArray(value);
};
    }

    private static ChunkPacketSignalUtil.BlockStateAccessor buildBlockStateAccessor(Class packetClass) {
        Method stateMethod = findBlockStateMethod(packetClass);
        Field stateField = findBlockStateField(packetClass);
        return (packet) -> {
    if (stateMethod == null) break label_1;
    break label_2;
    label_1:
    label_2:
    Object byMethod = null;
    if ((byMethod instanceof class_2680)) {
        class_2680 state = ((class_2680) byMethod);
        return state;
    }
    if (stateField != null) {
    } else {
    }
    Object byField = null;
    if ((byField instanceof class_2680)) {
        state = ((class_2680) byField);
        return state;
    }
    return null;
};
    }

    private static Method findEntityIdMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_7;
            }
            Class rt = method.getReturnType();
            if (rt != Integer.TYPE && rt != Integer.class) {
                break label_7;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("entity") && name.contains("id")) return method;
            label_7:
            var3++;
        }
        return null;
    }

    private static Field findEntityIdField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            Class ft = field.getType();
            if (ft != Integer.TYPE && ft != Integer.class) {
                break label_10;
            }
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("id"))) {
                break label_10;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex9) {
                Object var7 = ex9;
                label_10:
                var3++;
            }
            return null;
        }
    }

    private static Method findEntityTypeMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_6;
            }
            if (!(class_1299.class.isAssignableFrom(method.getReturnType()))) {
                break label_6;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("type")) return method;
            label_6:
            var3++;
        }
        return null;
    }

    private static Field findEntityTypeField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            if (!(class_1299.class.isAssignableFrom(field.getType()))) {
                break label_7;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex6) {
                Object var5 = ex6;
                label_7:
                var3++;
            }
            return null;
        }
    }

    private static class_1299 extractEntityType(Object packet, Method typeMethod, Field typeField) {
        if (typeMethod == null) break label_1;
        break label_2;
        label_1:
        label_2:
        Object byMethod = null;
        if ((byMethod instanceof class_1299)) {
            class_1299 entityType = ((class_1299) byMethod);
            return entityType;
        }
        if (typeField != null) {
        } else {
        }
        Object byField = null;
        if ((byField instanceof class_1299)) {
            entityType = ((class_1299) byField);
            return entityType;
        }
        return null;
    }

    private static Integer extractEntityId(Object packet, Method idMethod, Field idField) {
        if (idMethod == null) break label_1;
        break label_2;
        label_1:
        label_2:
        Object byMethod = null;
        if ((byMethod instanceof Number)) {
            Number num = ((Number) byMethod);
            return num.intValue();
        }
        if (idField != null) {
        } else {
        }
        Object byField = null;
        if ((byField instanceof Number)) {
            num = ((Number) byField);
            return num.intValue();
        }
        return null;
    }

    private static Method findEntityIdsMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_10;
            }
            Class rt = method.getReturnType();
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (rt == int[].class) {
                if ((name.contains("id")) || (name.contains("entities"))) {
                    return method;
                }
            }
            if (!(name.contains("id"))) {
                if (!(name.contains("entities"))) break label_10;
            }
            if (rt.getName().toLowerCase(Locale.ROOT).contains("intlist")) return method;
            label_10:
            var3++;
        }
        return null;
    }

    private static Field findEntityIdsField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            Class ft = field.getType();
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("id")) && !(name.contains("entities"))) {
                break label_10;
            }
            if (ft != int[].class) {
                if (!(ft.getName().toLowerCase(Locale.ROOT).contains("intlist"))) break label_10;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex9) {
                Object var7 = ex9;
                label_10:
                var3++;
            }
            return null;
        }
    }

    private static int[] toIntArray(Object value) {
        if (value == null) return new int[0];
        if (!((value instanceof int[]))) break label_3;
        int[] arr = ((int[]) value);
        return arr;
        label_3:
        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            int[] out = new int[len];
            int i = 0;
            while (i < len) {
                Object element = Array.get(value, i);
                if ((element instanceof Number)) {
                    Number num = ((Number) element);
                } else {
                }
                i[num.intValue()] = 0;
                i++;
            }
            return out;
        }
        if ((value instanceof Iterable)) {
            Iterable iterable = ((Iterable) value);
            int[] tmp = new int[64];
            int count = 0;
            element = iterable.iterator();
            while (element.hasNext()) {
                element = element.next();
                if (!((element instanceof Number))) break label_19;
                num = ((Number) element);
                if (count >= tmp.length) {
                    int[] grown = new int[(tmp.length * 2)];
                    System.arraycopy(tmp, 0, grown, 0, tmp.length);
                    tmp = grown;
                }
                count++;
                tmp[count] = num.intValue();
            }
            out = new int[count];
            System.arraycopy(tmp, 0, out, 0, count);
            return out;
        }
        return new int[0];
    }

    private static Method findBlockStateMethod(Class type) {
        Object var1 = type.getMethods();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Method method = var1[var3];
            if (method.getParameterCount()) {
                break label_6;
            }
            if (!(class_2680.class.isAssignableFrom(method.getReturnType()))) {
                break label_6;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("state")) return method;
            label_6:
            var3++;
        }
        return null;
    }

    private static Field findBlockStateField(Class type) {
        Object var1 = type.getDeclaredFields();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Field field = var1[var3];
            if (!(class_2680.class.isAssignableFrom(field.getType()))) {
                break label_7;
                try {
                }
                field.setAccessible(true);
                return field;
            } catch (Exception ex6) {
                Object var5 = ex6;
                label_7:
                var3++;
            }
            return null;
        }
    }

    private static boolean isRedstoneMetadataPacketName(String lowerSimpleName) {
        if (!(lowerSimpleName.contains("blockupdate")) && !(lowerSimpleName.contains("sectionblocksupdate")) && !(lowerSimpleName.contains("blockevent"))) {
            if (lowerSimpleName.contains("blockentityupdate")) {
            }
        } else {
        }
        return false;
    }

    private static boolean isRedstoneState(class_2680 state) {
        class_2248 block = state.method_26204();
        if (block != class_2246.field_10091 && block != class_2246.field_10450 && block != class_2246.field_10377 && block != class_2246.field_10282 && block != class_2246.field_10560 && block != class_2246.field_10615 && block != class_2246.field_10523 && block != class_2246.field_10301 && block != class_2246.field_10363 && block != class_2246.field_10429 && block != class_2246.field_22422 && block != class_2246.field_10312 && block != class_2246.field_10200 && block != class_2246.field_10228 && block != class_2246.field_10179 && block != class_2246.field_28108) {
            if (block == class_2246.field_43231) {
            }
        } else {
        }
        return false;
    }

    private static void cleanupTrackedItemsIfNeeded(long now) {
        if ((now - lastItemTrackerCleanupMs) < 5000L) return;
        lastItemTrackerCleanupMs = now;
        ITEM_ENTITY_TRACKER.entrySet().removeIf((entry) -> (now - ((ChunkPacketSignalUtil.ItemEntityTrace) entry.getValue()).createdAtMs) > 90000L);
    }

    private static void cleanupDropTrackerIfNeeded(long now) {
        if ((now - lastDropTrackerCleanupMs) < 5000L) return;
        lastDropTrackerCleanupMs = now;
        PLAYER_DROP_TRACKER.entrySet().removeIf((entry) -> (now - ((ChunkPacketSignalUtil.ItemDropRecord) entry.getValue()).dropMs) > 30000L);
    }

    private static void cleanupRapidSpawnSignalsIfNeeded(long now) {
        if ((now - lastRapidSpawnCleanupMs) < 5000L) return;
        lastRapidSpawnCleanupMs = now;
        RAPID_SPAWN_SIGNALS.entrySet().removeIf((entry) -> (now - ((ChunkPacketSignalUtil.RapidSpawnSignal) entry.getValue()).lastSpawnMs) > 120000L);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Object invoke(Object target, Method method) {
        try {
            return method.invoke(target, new Object[0]);
        } catch (Exception ex2) {
            Exception ignored = ex2;
            return null;
        }
    }

    private static Object read(Object target, Field field) {
        try {
            return field.get(target);
        } catch (Exception ex2) {
            Exception ignored = ex2;
            return null;
        }
    }

    static {
        EXTRACTOR_CACHE = new ConcurrentHashMap();
        ITEM_SPAWN_ACCESSOR_CACHE = new ConcurrentHashMap();
        ENTITY_REMOVE_ACCESSOR_CACHE = new ConcurrentHashMap();
        BLOCK_STATE_ACCESSOR_CACHE = new ConcurrentHashMap();
        ITEM_ENTITY_TRACKER = new ConcurrentHashMap();
        lastItemTrackerCleanupMs = 0L;
        PLAYER_DROP_TRACKER = new ConcurrentHashMap();
        lastDropTrackerCleanupMs = 0L;
        RAPID_SPAWN_SIGNALS = new ConcurrentHashMap();
        lastRapidSpawnCleanupMs = 0L;
        NO_REDSTONE = new ChunkPacketSignalUtil.RedstonePacketSignal(false, null, 0);
        NO_BLOCK_STATE = new ChunkPacketSignalUtil.BlockStateSignal(false, null, null);
    }


    record ItemSpawnSnapshot(Integer entityId, Long chunkKey, boolean itemEntity) {

        private ItemSpawnSnapshot(Integer entityId, Long chunkKey, boolean itemEntity) {
            this.entityId = entityId;
            this.chunkKey = chunkKey;
            this.itemEntity = itemEntity;
        }

        public Integer entityId() {
            return this.entityId;
        }

        public Long chunkKey() {
            return this.chunkKey;
        }

        public boolean itemEntity() {
            return this.itemEntity;
        }

    }

    interface EntityRemoveAccessor {
        int[] extractIds(Object arg0);

    }

    interface ChunkExtractor {
        Long extract(Object arg0);

    }

    interface ItemSpawnAccessor {
        ChunkPacketSignalUtil.ItemSpawnSnapshot extract(Object arg0);

    }

    interface BlockStateAccessor {
        class_2680 extract(Object arg0);

    }

    final class RemovalAccumulator {
        private int removedCount;
        private int fastRemovedCount;

        private int score() {
            return ((this.removedCount * 2) + (this.fastRemovedCount * 3));
        }

    }

    public record BlockStateSignal(boolean hasState, Long chunkKey, class_2680 state) {

        public BlockStateSignal(boolean hasState, Long chunkKey, class_2680 state) {
            this.hasState = hasState;
            this.chunkKey = chunkKey;
            this.state = state;
        }

        public boolean hasState() {
            return this.hasState;
        }

        public Long chunkKey() {
            return this.chunkKey;
        }

        public class_2680 state() {
            return this.state;
        }

    }

    final class ItemDropRecord {
        final long chunkKey;
        final long dropMs;

        ItemDropRecord(long chunkKey, long dropMs) {
            this.chunkKey = chunkKey;
            this.dropMs = dropMs;
        }

    }

    final class ItemEntityTrace {
        private final long chunkKey;
        private final long createdAtMs;

        private ItemEntityTrace(long chunkKey, long createdAtMs) {
            this.chunkKey = chunkKey;
            this.createdAtMs = createdAtMs;
        }

    }

    public record PacketSignal(boolean environmentSignal, Long chunkKey, int weight) {
        private static final ChunkPacketSignalUtil.PacketSignal NONE;

        public PacketSignal(boolean environmentSignal, Long chunkKey, int weight) {
            this.environmentSignal = environmentSignal;
            this.chunkKey = chunkKey;
            this.weight = weight;
        }

        public boolean environmentSignal() {
            return this.environmentSignal;
        }

        public Long chunkKey() {
            return this.chunkKey;
        }

        public int weight() {
            return this.weight;
        }

        static {
            NONE = new ChunkPacketSignalUtil.PacketSignal(false, null, 0);
        }

    }

    final class RapidSpawnSignal {
        long firstSpawnMs;
        long lastSpawnMs;
        int spawnCount;
        int rapidBurstCount;
        double avgSpawnRate;

        RapidSpawnSignal(long now) {
            this.firstSpawnMs = now;
            this.lastSpawnMs = now;
            this.spawnCount = 0;
            this.rapidBurstCount = 0;
            this.avgSpawnRate = 0.0;
        }

    }

    public record RedstonePacketSignal(boolean hasSignal, Long chunkKey, int weight) {

        public RedstonePacketSignal(boolean hasSignal, Long chunkKey, int weight) {
            this.hasSignal = hasSignal;
            this.chunkKey = chunkKey;
            this.weight = weight;
        }

        public boolean hasSignal() {
            return this.hasSignal;
        }

        public Long chunkKey() {
            return this.chunkKey;
        }

        public int weight() {
            return this.weight;
        }

    }
}

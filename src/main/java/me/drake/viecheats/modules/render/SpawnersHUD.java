package me.drake.viecheats.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import me.drake.viecheats.core.event.events.GameJoinEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.mixin.MixinMobSpawnerLogicAccessor;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.render.VieMeshRenderer;
import me.drake.viecheats.util.SpawnerEntityCache;
import net.minecraft.class_1923;
import net.minecraft.class_1952;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class SpawnersHUD extends Module {
    private final BoolSetting showCount;
    private final BoolSetting showDist;
    private final ColorSetting textColor;
    private final ColorSetting backdropColor;
    private final Map spawnersByChunk;
    private final ArrayDeque scanQueue;
    private final Set queued;
    private int rescanTimer;

    public SpawnersHUD() {
        super("SpawnersHUD", "Shows spawner type labels above spawners", Category.RENDER);
        this.showCount = ((BoolSetting) addSetting(new BoolSetting("Count", "Show spawner count in label", true)));
        this.showDist = ((BoolSetting) addSetting(new BoolSetting("Distance", "Show distance", false)));
        this.textColor = ((ColorSetting) addSetting(new ColorSetting("TextColor", "Label text color", -1)));
        this.backdropColor = ((ColorSetting) addSetting(new ColorSetting("Backdrop", "Backdrop color", -870704614)));
        this.spawnersByChunk = new HashMap();
        this.scanQueue = new ArrayDeque();
        this.queued = new HashSet();
        this.rescanTimer = 0;
    }

    public String getInfo() {
        int total = this.spawnersByChunk.values().stream().mapToInt(List::size).sum();
        return (total + " spawners");
    }

    public void onEnable() {
        clearState();
        enqueueInitialChunks();
    }

    public void onDisable() {
        super.onDisable();
        clearState();
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent event) {
        clearState();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!(isInGame())) return;
        long key = pack(event.getChunkPos());
        if (event.isLoaded()) {
            enqueueChunk(key);
        } else {
            this.spawnersByChunk.remove(key);
            this.queued.remove(key);
            this.scanQueue.remove(key);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) break label_1;
        if (!(isInGame())) {
            label_1:
            return;
        }
        this.rescanTimer = (this.rescanTimer + 1);
        if ((this.rescanTimer + 1) >= 3) {
            this.rescanTimer = 0;
            enqueueNearbyChunks();
        }
        int left = 8;
        label_6:
        left--;
        if (left <= 0) break label_12;
        Long key = ((Long) this.scanQueue.pollFirst());
        if (key == null) {
            break label_12;
        }
        this.queued.remove(key);
        scanChunk(unpack(key.longValue()), key.longValue());
        break label_6;
        label_12:
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.HUD_2D) return;
        if (!(isInGame())) return;
        if (this.spawnersByChunk.isEmpty()) return;
        if (!(TessellatorRenderer.isInFrame())) return;
        class_332 ctx = event.getDrawContext();
        if (ctx == null) return;
        class_327 tr = mc.field_1772;
        class_4184 camera = mc.field_1773.method_19418();
        if (camera == null) return;
        class_243 camPos = camera.method_71156();
        Matrix4f mvp = buildMvp(camera);
        List all = new ArrayList();
        Object var8 = this.spawnersByChunk.values().iterator();
        while (var8.hasNext()) {
            List list = ((List) var8.next());
            all.addAll(list);
        }
        all.sort(Comparator.comparingDouble((s) -> s.distSq));
        var8 = all.iterator();
        while (var8.hasNext()) {
            SpawnersHUD.CachedSpawner spawner = ((SpawnersHUD.CachedSpawner) var8.next());
            if (!(spawner.isAlive())) {
                break label_18;
            }
            class_243 worldPos = new class_243((((double) spawner.pos.method_10263()) + 0.5), (((double) spawner.pos.method_10264()) + 1.6), (((double) spawner.pos.method_10260()) + 0.5));
            float[] screen = projectToScreen(worldPos.field_1352, worldPos.field_1351, worldPos.field_1350, mvp, camPos);
            if (screen == null) {
                break label_18;
            }
            int sx = ((int) screen[0]);
            int sy = ((int) screen[1]);
            StringBuilder label = new StringBuilder(formatType(spawner.type));
            if (((Boolean) this.showCount.get()).booleanValue() && spawner.count > 1) {
                label.append(" x").append(spawner.count);
            }
            if (((Boolean) this.showDist.get()).booleanValue()) {
                label.append(" ").append(((int) Math.sqrt(spawner.distSq))).append("m");
            }
            String text = label.toString();
            int textW = TessellatorRenderer.measureTextWidth(tr, text);
            int padX = 4;
            int padY = 3;
            int radius = 3;
            int backdrop = ((Integer) this.backdropColor.get()).intValue();
            int bx = ((sx - (textW / 2)) - padX);
            Objects.requireNonNull(tr);
            int by = ((sy - 9) - (padY * 2));
            int bw = (textW + (padX * 2));
            Objects.requireNonNull(tr);
            int bh = (9 + (padY * 2));
            int tx = (sx - (textW / 2));
            Objects.requireNonNull(tr);
            int ty = ((sy - 9) - padY);
            TessellatorRenderer.fillRoundedRect(ctx, bx, by, bw, bh, radius, backdrop);
            TessellatorRenderer.drawTextWithShadow(ctx, tr, text, tx, ty, ((Integer) this.textColor.get()).intValue());
        }
    }

    private void enqueueInitialChunks() {
        if (!(isInGame())) return;
        class_1923 playerChunk = mc.field_1724.method_31476();
        int x = (playerChunk.field_9181 - 3);
        while (x <= (playerChunk.field_9181 + 3)) {
            int z = (playerChunk.field_9180 - 3);
            while (z <= (playerChunk.field_9180 + 3)) {
                enqueueChunk(class_1923.method_8331(x, z));
                z++;
            }
            x++;
        }
    }

    private void enqueueNearbyChunks() {
        if (!(isInGame())) return;
        class_1923 center = mc.field_1724.method_31476();
        int dx = -2;
        while (dx <= 2) {
            int dz = -2;
            while (dz <= 2) {
                enqueueChunk(class_1923.method_8331((center.field_9181 + dx), (center.field_9180 + dz)));
                dz++;
            }
            dx++;
        }
    }

    private void enqueueChunk(long key) {
        if (!(isInGame())) return;
        class_1923 pos = unpack(key);
        if (!(mc.field_1687.method_8393(pos.field_9181, pos.field_9180))) return;
        if (this.queued.add(key)) {
            this.scanQueue.offerLast(key);
        }
    }

    private void scanChunk(class_1923 chunkPos, long key) {
        if (!(isInGame())) return;
        class_2818 chunk = mc.field_1687.method_2935().method_12126(chunkPos.field_9181, chunkPos.field_9180, false);
        if (chunk == null) return;
        List found = findSpawners(chunk);
        if (found.isEmpty()) {
            this.spawnersByChunk.remove(key);
            return;
        }
        Object var6 = found.iterator();
        while (var6.hasNext()) {
            SpawnersHUD.CachedSpawner s = ((SpawnersHUD.CachedSpawner) var6.next());
            s.distSq = mc.field_1724.method_5649((((double) s.pos.method_10263()) + 0.5), (((double) s.pos.method_10264()) + 0.5), (((double) s.pos.method_10260()) + 0.5));
        }
        this.spawnersByChunk.put(key, found);
    }

    private List findSpawners(class_2818 chunk) {
        List out = new ArrayList();
        class_1923 cpos = chunk.method_12004();
        class_2826[] sections = chunk.method_12006();
        int worldBottomY = mc.field_1687.method_31607();
        int i = 0;
        while (i < sections.length) {
            class_2826 section = sections[i];
            if (section == null) break label_26;
            if (section.method_38292()) {
                break label_26;
            }
            if (!(section.method_19523((state) -> state.method_27852(class_2246.field_10260)))) {
                break label_26;
            }
            int sectionY = (worldBottomY + (i * 16));
            int x = 0;
            while (x < 16) {
                int y = 0;
                while (y < 16) {
                    int z = 0;
                    while (z < 16) {
                        if (!(section.method_12254(x, y, z).method_27852(class_2246.field_10260))) {
                        } else {
                            class_2338.class_2339 mut = new class_2338.class_2339((cpos.method_8326() + x), (sectionY + y), (cpos.method_8328() + z));
                            String type = readSpawnerType(mut);
                            int count = countSpawnersInChunk(chunk, type);
                            double distSq = mc.field_1724.method_5649((((double) mut.method_10263()) + 0.5), (((double) mut.method_10264()) + 0.5), (((double) mut.method_10260()) + 0.5));
                            out.add(new SpawnersHUD.CachedSpawner(mut.method_10062(), type, count, distSq));
                        }
                        z++;
                    }
                    y++;
                }
                x++;
            }
            label_26:
            i++;
        }
        return out;
    }

    private String readSpawnerType(class_2338 pos) {
        String cached = SpawnerEntityCache.get(pos);
        if (cached != null) {
            return cached;
            try {
            }
            class_2586 be = mc.field_1687.method_8321(pos);
            if (!((be instanceof class_2636))) break label_14;
            class_2636 spawner = ((class_2636) be);
            class_1952 entry = ((MixinMobSpawnerLogicAccessor) spawner.method_11390()).getSpawnEntry();
            if (entry == null) break label_14;
            class_2487 nbt = entry.method_38093();
            if (nbt == null) break label_14;
            String id = nbt.method_68564("id", "");
            if (id.isEmpty()) break label_14;
            int dot = id.lastIndexOf(46);
            if (dot >= 0) {
            } else {
            }
            return id;
            label_14:
        } catch (Exception ex15) {
            be = ex15;
        }
        return "Spawner";
    }

    private int countSpawnersInChunk(class_2818 chunk, String type) {
        int count = 0;
        class_1923 cpos = chunk.method_12004();
        class_2826[] sections = chunk.method_12006();
        int worldBottomY = mc.field_1687.method_31607();
        int i = 0;
        while (i < sections.length) {
            class_2826 section = sections[i];
            if (section == null) break label_22;
            if (section.method_38292()) {
                break label_22;
            }
            if (!(section.method_19523((state) -> state.method_27852(class_2246.field_10260)))) {
                break label_22;
            }
            int sectionY = (worldBottomY + (i * 16));
            int x = 0;
            while (x < 16) {
                int y = 0;
                while (y < 16) {
                    int z = 0;
                    while (z < 16) {
                        if (!(section.method_12254(x, y, z).method_27852(class_2246.field_10260))) {
                            break label_19;
                        }
                        class_2338 pos = new class_2338((cpos.method_8326() + x), (sectionY + y), (cpos.method_8328() + z));
                        if (!(readSpawnerType(pos).equals(type))) break label_19;
                        count++;
                        label_19:
                        z++;
                    }
                    y++;
                }
                x++;
            }
            label_22:
            i++;
        }
        return count;
    }

    private static String formatType(String type) {
        if ((type == null) || (type.isEmpty())) {
            return "Spawner";
        }
        if (type.equals("Spawner")) return "Spawner";
        if (type.length() <= 3) return type.toUpperCase();
        return (type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase());
    }

    private Matrix4f buildMvp(class_4184 camera) {
        Quaternionf conjugate = camera.method_23767().conjugate(new Quaternionf());
        Matrix4f viewMatrix = new Matrix4f().rotation(conjugate);
        return new Matrix4f(VieMeshRenderer.projection).mul(viewMatrix);
    }

    private float[] projectToScreen(double wx, double wy, double wz, Matrix4f mvp, class_243 camPos) {
        float rx = ((float) (wx - camPos.field_1352));
        float ry = ((float) (wy - camPos.field_1351));
        float rz = ((float) (wz - camPos.field_1350));
        Vector4f pos = new Vector4f(rx, ry, rz, 1.0f);
        mvp.transform(pos);
        if (pos.w <= 0.0f) return null;
        float ndcX = (pos.x / pos.w);
        float ndcY = (pos.y / pos.w);
        int w = mc.method_22683().method_4486();
        int h = mc.method_22683().method_4502();
        var tmp0 = new float[2];
        tmp0[0] = (((ndcX + 1.0f) / 2.0f) * ((float) w));
        tmp0[1] = (((1.0f - ndcY) / 2.0f) * ((float) h));
        return tmp0;
    }

    private void clearState() {
        this.spawnersByChunk.clear();
        this.scanQueue.clear();
        this.queued.clear();
        this.rescanTimer = 0;
        SpawnerEntityCache.clear();
    }

    private static long pack(class_1923 pos) {
        return class_1923.method_8331(pos.field_9181, pos.field_9180);
    }

    private static class_1923 unpack(long key) {
        return new class_1923(class_1923.method_8325(key), class_1923.method_8332(key));
    }


    class CachedSpawner {
        final class_2338 pos;
        final String type;
        final int count;
        double distSq;

        CachedSpawner(class_2338 pos, String type, int count, double distSq) {
            this.pos = pos;
            this.type = type;
            this.count = count;
            this.distSq = distSq;
        }

        boolean isAlive() {
            if ((SpawnersHUD.access$000() == null) || (SpawnersHUD.access$100().field_1687 == null)) break label_1;
            return SpawnersHUD.access$200().field_1687.method_8320(this.pos).method_27852(class_2246.field_10260);
        }

    }
}

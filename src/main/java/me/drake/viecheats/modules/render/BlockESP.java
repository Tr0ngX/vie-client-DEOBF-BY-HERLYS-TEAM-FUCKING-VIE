package me.drake.viecheats.modules.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ChunkLoadEvent;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ButtonSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.gui.BlockFilterScreen;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.PerformanceHelper;
import me.drake.viecheats.util.RainbowUtil;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockESP extends Module {
    private final ButtonSetting openFilter;
    private final BoolSetting tracers;
    private final EnumSetting style;
    private final EnumSetting colorMode;
    private final DoubleSetting range;
    private final IntSetting maxPerChunk;
    private final IntSetting scanBudget;
    private final IntSetting rescanTicks;
    private final IntSetting maxRender;
    private final IntSetting outlineAlpha;
    private final IntSetting fillAlpha;
    private final IntSetting tracerAlpha;
    private final List blockOptions;
    private final List blockOptionsView;
    private final Map configById;
    private final Map configByBlock;
    private final Map chunkCache;
    private int cachedBlockCount;
    private int rescanTimer;
    private int scanRingIndex;
    private boolean dirtyFilter;
    private double lastRange;
    private int lastMaxPerChunk;

    public BlockESP() {
        super("BlockESP", "Highlights selected block types", Category.RENDER);
        this.openFilter = ((ButtonSetting) addSetting(new ButtonSetting("Open Filter", "Open block type filter", this::openFilterScreen)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracer lines", false)));
        this.style = ((EnumSetting) addSetting(new EnumSetting("Style", "Block render style", BlockESP.RenderStyle.SOLID)));
        this.colorMode = ((EnumSetting) addSetting(new EnumSetting("ColorMode", "Color source", BlockESP.ColorMode.FILTER_COLOR)));
        this.range = ((DoubleSetting) addSetting(((DoubleSetting) hidden(new DoubleSetting("Range", "Render range", 80.0, 16.0, 192.0)))));
        this.maxPerChunk = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("MaxPerChunk", "Max cached blocks per chunk", 64, 8, 2048)))));
        this.scanBudget = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("ScanBudget", "Chunks scanned per pass", 8, 1, 128)))));
        this.rescanTicks = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("RescanTicks", "Ticks between rescans", 8, 1, 40)))));
        this.maxRender = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("MaxRender", "Max blocks rendered per frame", 900, 50, 20000)))));
        this.outlineAlpha = ((IntSetting) addSetting(new IntSetting("OutlineAlpha", "Outline opacity", 200, 20, 255)));
        this.fillAlpha = ((IntSetting) addSetting(new IntSetting("FillAlpha", "Fill opacity", 30, 0, 255)));
        this.tracerAlpha = ((IntSetting) addSetting(new IntSetting("TracerAlpha", "Tracer opacity", 120, 0, 255)));
        this.blockOptions = new ArrayList();
        this.blockOptionsView = Collections.unmodifiableList(this.blockOptions);
        this.configById = new HashMap();
        this.configByBlock = new HashMap();
        this.chunkCache = new ConcurrentHashMap();
        this.cachedBlockCount = 0;
        this.rescanTimer = 0;
        this.scanRingIndex = 0;
        this.dirtyFilter = true;
        this.lastRange = -1.0;
        this.lastMaxPerChunk = -1;
        buildBlockFilters();
    }

    private static Setting hidden(Setting setting) {
        setting.setVisibleInGui(false);
        return setting;
    }

    public List getBlockOptions() {
        return this.blockOptionsView;
    }

    public int getTotalBlockTypeCount() {
        return this.blockOptions.size();
    }

    public int getEnabledBlockTypeCount() {
        int count = 0;
        Object var2 = this.configById.values().iterator();
        while (var2.hasNext()) {
            BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) var2.next());
            if (((Boolean) cfg.enabled.get()).booleanValue()) {
                count++;
            }
        }
        return count;
    }

    public boolean isBlockEnabled(class_2960 id) {
        BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configById.get(id));
        if (cfg == null) break label_2;
        return ((Boolean) cfg.enabled.get()).booleanValue();
    }

    public void setBlockEnabled(class_2960 id, boolean enabled) {
        BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configById.get(id));
        if ((cfg == null) || (((Boolean) cfg.enabled.get()).booleanValue() == enabled)) {
            return;
        }
        cfg.enabled.set(enabled);
        this.dirtyFilter = true;
    }

    public int getBlockColor(class_2960 id) {
        BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configById.get(id));
        if (cfg == null) {
        } else {
        }
        return ((Integer) cfg.color.get()).intValue();
    }

    public void setBlockColor(class_2960 id, int rgb) {
        BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configById.get(id));
        if (cfg == null) return;
        cfg.color.set((-16777216 | (rgb & 16777215)));
    }

    public void setBlocksEnabled(Collection ids, boolean enabled) {
        boolean changed = false;
        Object var4 = ids.iterator();
        while (var4.hasNext()) {
            class_2960 id = ((class_2960) var4.next());
            BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configById.get(id));
            if (cfg == null) break label_2;
            if (((Boolean) cfg.enabled.get()).booleanValue() == enabled) {
                break label_2;
            }
            cfg.enabled.set(enabled);
            changed = true;
        }
        if (!(!changed)) {
            this.dirtyFilter = true;
        }
    }

    public void openFilterScreen() {
        mc.method_1507(new BlockFilterScreen(this));
    }

    public void onEnable() {
        super.onEnable();
        this.dirtyFilter = true;
        fullRescan();
    }

    public void onDisable() {
        super.onDisable();
        this.chunkCache.clear();
        this.cachedBlockCount = 0;
        this.rescanTimer = 0;
    }

    public String getInfo() {
        return (this.cachedBlockCount + " | " + getEnabledBlockTypeCount() + " | " + ((int) ((Double) this.range.get()).doubleValue()) + "m");
    }

    public boolean usesEntityCache() {
        return true;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!(isInGame())) return;
        if (!(hasAnyEnabledBlocks())) {
            if (!(e.isLoaded())) {
                removeChunkCache(e.getChunkPos());
            }
            return;
        }
        if (e.isLoaded()) {
            scanChunk(e.getChunkPos());
        } else {
            removeChunkCache(e.getChunkPos());
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (e.getPhase() != TickEvent.Phase.PRE) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        if (Math.abs((this.lastRange - ((Double) this.range.get()).doubleValue())) > 1.0E-6) {
            this.lastRange = ((Double) this.range.get()).doubleValue();
            this.dirtyFilter = true;
        }
        if (this.lastMaxPerChunk != ((Integer) this.maxPerChunk.get()).intValue()) {
            this.lastMaxPerChunk = ((Integer) this.maxPerChunk.get()).intValue();
            this.dirtyFilter = true;
        }
        if (!(hasAnyEnabledBlocks())) {
            if (!(!this.cachedBlockCount)) {
                this.chunkCache.clear();
                this.cachedBlockCount = 0;
            }
            return;
        }
        if (!(!this.dirtyFilter)) {
            this.dirtyFilter = false;
            fullRescan();
            return;
        }
        int rescanInterval = PerformanceHelper.scaleInterval(((Integer) this.rescanTicks.get()).intValue());
        this.rescanTimer = (this.rescanTimer + 1);
        if ((this.rescanTimer + 1) >= rescanInterval) {
            this.rescanTimer = 0;
            rescanNearby();
        }
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if ((e.getStage() != RenderEvent.Stage.WORLD_3D) || (!(isInGame()))) {
            return;
        }
        if (!(hasAnyEnabledBlocks())) return;
        float tickDelta = e.getTickDelta();
        double effectiveRange = ((Double) this.range.get()).doubleValue();
        double maxDistSq = (effectiveRange * effectiveRange);
        double px = mc.field_1724.method_23317();
        double py = mc.field_1724.method_23318();
        double pz = mc.field_1724.method_23321();
        int outA = ((Integer) this.outlineAlpha.get()).intValue();
        int filA = ((Integer) this.fillAlpha.get()).intValue();
        int trA = ((Integer) this.tracerAlpha.get()).intValue();
        BlockESP.RenderStyle currentStyle = ((BlockESP.RenderStyle) this.style.get());
        boolean drawTracers = ((Boolean) this.tracers.get()).booleanValue();
        boolean drawOutline = currentStyle != BlockESP.RenderStyle.FILL_ONLY;
        boolean drawFill = currentStyle != BlockESP.RenderStyle.OUTLINE;
        if (!drawOutline && !drawFill && !drawTracers) return;
        int renderCap = PerformanceHelper.scaleRenderCap(((Integer) this.maxRender.get()).intValue());
        int rendered = 0;
        Object var22 = this.chunkCache.values().iterator();
        while (var22.hasNext()) {
            List entries = ((List) var22.next());
            Object var24 = entries.iterator();
            while (var24.hasNext()) {
                BlockESP.CachedBlock cb = ((BlockESP.CachedBlock) var24.next());
                if (rendered >= renderCap) {
                    break label_53;
                }
                double dx = (cb.centerX - px);
                double dy = (cb.centerY - py);
                double dz = (cb.centerZ - pz);
                double distSq = (((dx * dx) + (dy * dy)) + (dz * dz));
                if (distSq > maxDistSq) {
                    break label_26;
                }
                int col = getRenderColor(cb.config, distSq, cb.seed);
                if (!drawOutline) {
                    if (!(!drawFill)) {
                    }
                } else {
                }
                boolean needsVisibility = false;
                if (!(!needsVisibility)) {
                    if (EntityCache.isVisible(cb.box)) {
                    }
                } else {
                }
                boolean visible = false;
                if (!(!visible) && !(!drawOutline)) {
                    RenderUtil.drawBox(cb.box, RainbowUtil.withAlpha(col, outA));
                }
                if (!(!visible) && !(!drawFill)) {
                    RenderUtil.drawFilledBox(cb.box, RainbowUtil.withAlpha(col, filA));
                }
                if (!(!drawTracers)) {
                    RenderUtil.drawTracerToPos(cb.centerX, cb.centerY, cb.centerZ, RainbowUtil.withAlpha(col, trA), tickDelta);
                }
                rendered++;
            }
        }
        label_53:
    }

    private void buildBlockFilters() {
        List ids = new ArrayList();
        Set seenBlocks = new HashSet();
        Object var3 = class_7923.field_41175.method_10235().iterator();
        while (var3.hasNext()) {
            class_2960 id = ((class_2960) var3.next());
            if (!"minecraft".equals(id.method_12836())) {
                break label_3;
            }
            class_2248 block = ((class_2248) class_7923.field_41175.method_63535(id));
            if (((block == class_2246.field_10124) || (block == class_2246.field_10543)) || (block == class_2246.field_10243)) break label_3;
            if (block == class_2246.field_10499) {
                break label_3;
            }
            if (!(seenBlocks.add(block))) {
                break label_3;
            }
            ids.add(id);
        }
        ids.sort(Comparator.comparing(class_2960::toString));
        var3 = ids.iterator();
        while (var3.hasNext()) {
            id = ((class_2960) var3.next());
            block = ((class_2248) class_7923.field_41175.method_63535(id));
            if (block == null) {
                break label_12;
            }
            class_1799 icon = resolveBlockIcon(block, id);
            if (icon == null) break label_12;
            if (icon.method_7960()) {
                break label_12;
            }
            String settingKey = sanitizeIdentifier(id);
            BoolSetting enabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting(("BlockEnabled_" + settingKey), ("Enable " + String.valueOf(id)), false)))));
            ColorSetting color = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting(("BlockColor_" + settingKey), ("Color for " + String.valueOf(id)), defaultColorFor(id))))));
            String displayName = resolveDisplayName(icon, id);
            BlockESP.BlockOption option = new BlockESP.BlockOption(id, displayName, icon, (displayName + " " + String.valueOf(id)).toLowerCase(Locale.ROOT));
            BlockESP.BlockConfig cfg = new BlockESP.BlockConfig(block, id, enabled, color);
            this.blockOptions.add(option);
            this.configById.put(id, cfg);
            this.configByBlock.put(block, cfg);
        }
    }

    private static class_1799 resolveBlockIcon(class_2248 block, class_2960 id) {
        class_1792 item = block.method_8389();
        if (item != class_1802.field_8162) return new class_1799(item);
        String path = id.method_12832();
        if (path.contains("water")) return new class_1799(class_1802.field_8705);
        if (path.contains("lava")) return new class_1799(class_1802.field_8187);
        if (path.contains("portal")) return new class_1799(class_1802.field_8281);
        if (path.contains("fire")) return new class_1799(class_1802.field_8884);
        if (path.contains("piston")) return new class_1799(class_1802.field_8249);
        if ((path.contains("head")) || (path.contains("skull"))) {
            return new class_1799(class_1802.field_8398);
        }
        return class_1799.field_8037;
    }

    private int getRenderColor(BlockESP.BlockConfig config, double distSq, long offset) {
        int base = ((Integer) config.color.get()).intValue();
        switch (((BlockESP.ColorMode) this.colorMode.get()).ordinal()) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            default -> { break label_2; }
        }
        label_2:
        throw new MatchException(null, null);
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return RainbowUtil.rainbow(1.2, 1.0, offset, 255);
    }

    private boolean hasAnyEnabledBlocks() {
        Object var1 = this.configById.values().iterator();
        while (var1.hasNext()) {
            BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) var1.next());
            if (((Boolean) cfg.enabled.get()).booleanValue()) return true;
        }
        return false;
    }

    private void rescanNearby() {
        if (mc.field_1724 == null) break label_1;
        if (mc.field_1687 != null) break label_2;
        label_1:
        return;
        label_2:
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        int budget = PerformanceHelper.scaleBudget(((Integer) this.scanBudget.get()).intValue());
        if (budget <= 0) return;
        int dx = -1;
        while (dx <= 1) {
            int dz = -1;
            while (dz <= 1) {
                if (budget <= 0) {
                    break label_15;
                }
                scanChunk(new class_1923((pcx + dx), (pcz + dz)));
                budget--;
                dz++;
            }
            dx++;
        }
        label_15:
        int rd = getAdaptiveChunkRadius();
        if (rd <= 1) {
            cleanupOutOfRangeChunks(pcx, pcz, (rd + 1));
            return;
        }
        int ringSpan = Math.max(1, (rd - 1));
        int ring = Math.min(rd, ((this.scanRingIndex % ringSpan) + 2));
        this.scanRingIndex = (this.scanRingIndex + 1);
        dx = -(ring);
        while (dx <= ring) {
            if (Math.abs(dx) == ring) {
                dz = -(ring);
                while (dz <= ring) {
                    if (budget <= 0) {
                        break label_39;
                    }
                    scanChunk(new class_1923((pcx + dx), (pcz + dz)));
                    budget--;
                    dz++;
                }
            } else {
                if (budget <= 0) {
                    break label_39;
                }
                scanChunk(new class_1923((pcx + dx), (pcz - ring)));
                budget--;
                if (budget <= 0) {
                    break label_39;
                }
                scanChunk(new class_1923((pcx + dx), (pcz + ring)));
                budget--;
            }
            dx++;
        }
        label_39:
        cleanupOutOfRangeChunks(pcx, pcz, (rd + 1));
    }

    private void fullRescan() {
        this.chunkCache.clear();
        this.cachedBlockCount = 0;
        if ((mc.field_1687 == null) || (mc.field_1724 == null)) {
            return;
        }
        if (!(hasAnyEnabledBlocks())) return;
        int rd = getAdaptiveChunkRadius();
        int px = mc.field_1724.method_31476().field_9181;
        int pz = mc.field_1724.method_31476().field_9180;
        int cx = (px - rd);
        while (cx <= (px + rd)) {
            int cz = (pz - rd);
            while (cz <= (pz + rd)) {
                scanChunk(new class_1923(cx, cz));
                cz++;
            }
            cx++;
        }
    }

    private void scanChunk(class_1923 pos) {
        if (mc.field_1687 == null) break label_1;
        if (mc.field_1724 != null) break label_2;
        label_1:
        return;
        label_2:
        int rd = (getAdaptiveChunkRadius() + 1);
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        if (Math.abs((pos.field_9181 - pcx)) <= rd) {
            if (Math.abs((pos.field_9180 - pcz)) > rd) {
            }
            removeChunkCache(pos);
            return;
        }
        class_2818 chunk = mc.field_1687.method_2935().method_12126(pos.field_9181, pos.field_9180, false);
        if (chunk == null) {
            removeChunkCache(pos);
            return;
        }
        int cap = ((Integer) this.maxPerChunk.get()).intValue();
        if (cap <= 0) {
            removeChunkCache(pos);
            return;
        }
        List list = new ArrayList(Math.min(cap, 128));
        class_2826[] sections = chunk.method_12006();
        int startX = pos.method_8326();
        int startZ = pos.method_8328();
        int chunkBottomY = chunk.method_31607();
        int sectionIndex = 0;
        while (sectionIndex < sections.length) {
            class_2826 section = sections[sectionIndex];
            if (section == null) break label_46;
            if (section.method_38292()) {
                break label_46;
            }
            if (!(sectionMayContainEnabledBlock(section))) {
                break label_46;
            }
            int sectionBaseY = (chunkBottomY + (sectionIndex << 4));
            int ly = 0;
            while (ly < 16) {
                int wy = (sectionBaseY + ly);
                int lx = 0;
                while (lx < 16) {
                    int wx = (startX + lx);
                    int lz = 0;
                    while (lz < 16) {
                        int wz = (startZ + lz);
                        class_2680 state = section.method_12254(lx, ly, lz);
                        BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configByBlock.get(state.method_26204()));
                        if (cfg == null) break label_43;
                        if (!(((Boolean) cfg.enabled.get()).booleanValue())) {
                            break label_43;
                        }
                        list.add(new BlockESP.CachedBlock(new class_238(((double) wx), ((double) wy), ((double) wz), ((double) (wx + 1)), ((double) (wy + 1)), ((double) (wz + 1))), cfg, (((double) wx) + 0.5), (((double) wy) + 0.5), (((double) wz) + 0.5), class_2338.method_10064(wx, wy, wz)));
                        if (list.size() < cap) break label_43;
                        break label_47;
                        label_43:
                        lz++;
                    }
                    lx++;
                }
                ly++;
            }
            label_46:
            sectionIndex++;
        }
        label_47:
        List old = ((List) this.chunkCache.get(pos));
        if (old == null) {
        } else {
        }
        int oldSize = old.size();
        int newSize = list.size();
        this.cachedBlockCount = (this.cachedBlockCount + (newSize - oldSize));
        if (newSize == 0) {
            this.chunkCache.remove(pos);
        } else {
            this.chunkCache.put(pos, list);
        }
    }

    private boolean sectionMayContainEnabledBlock(class_2826 section) {
        return section.method_19523(() -> {
    BlockESP.BlockConfig cfg = ((BlockESP.BlockConfig) this.configByBlock.get(state.method_26204()));
    if (cfg == null) break label_2;
    return ((Boolean) cfg.enabled.get()).booleanValue();
});
    }

    private void cleanupOutOfRangeChunks(int pcx, int pcz, int radius) {
        List toRemove = new ArrayList();
        Object var5 = this.chunkCache.keySet().iterator();
        while (var5.hasNext()) {
            class_1923 pos = ((class_1923) var5.next());
            if (Math.abs((pos.field_9181 - pcx)) <= radius) {
                if (Math.abs((pos.field_9180 - pcz)) > radius) {
                }
                toRemove.add(pos);
            }
        }
        var5 = toRemove.iterator();
        while (var5.hasNext()) {
            pos = ((class_1923) var5.next());
            removeChunkCache(pos);
        }
    }

    private void removeChunkCache(class_1923 pos) {
        List old = ((List) this.chunkCache.remove(pos));
        if (old != null) {
            this.cachedBlockCount = (this.cachedBlockCount - old.size());
        }
    }

    private int getAdaptiveChunkRadius() {
        int viewDist = ((Integer) mc.field_1690.method_42503().method_41753()).intValue();
        int byRange = ((int) Math.ceil((((Double) this.range.get()).doubleValue() / 16.0)));
        return Math.max(1, Math.min(viewDist, byRange));
    }

    private static int defaultColorFor(class_2960 id) {
        int h = id.hashCode();
        float hue = (((float) (h & 65535)) / 65535.0f);
        float sat = (0.55f + ((((float) ((h >>> 16) & 255)) / 255.0f) * 0.35f));
        return (-16777216 | hsvToRgb(hue, sat, 1.0f));
    }

    private static String resolveDisplayName(class_1799 icon, class_2960 id) {
        if (icon != null && !(icon.method_7960())) {
            String name = icon.method_7964().getString();
            if (name != null && !(name.isBlank())) return name;
        }
        return prettifyPath(id.method_12832());
    }

    private static String prettifyPath(String path) {
        String p = path.replace(95, 32).replace(47, 32);
        if (p.isEmpty()) return path;
        return (Character.toUpperCase(p.charAt(0)) + p.substring(1));
    }

    private static String sanitizeIdentifier(class_2960 id) {
        String raw = id.toString();
        StringBuilder sb = new StringBuilder((raw.length() + 8));
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c >= 97) {
                if (c <= 122) break label_8;
            }
            if (c >= 65) {
                if (c <= 90) break label_8;
            }
            if (c >= 48 && c <= 57) {
                label_8:
                sb.append(c);
            } else {
                sb.append(95);
            }
            i++;
        }
        return sb.toString();
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        float h = ((hue - ((float) Math.floor(((double) hue)))) * 6.0f);
        float f = (h - ((float) Math.floor(((double) h))));
        float p = (value * (1.0f - saturation));
        float q = (value * (1.0f - (saturation * f)));
        float t = (value * (1.0f - (saturation * (1.0f - f))));
        switch (((int) h)) {
            case 0 -> { break label_6; }
            case 1 -> { break label_10; }
            case 2 -> { break label_14; }
            case 3 -> { break label_18; }
            case 4 -> { break label_22; }
            default -> { break label_26; }
        }
        label_6:
        int r = ((int) ((value * 255.0f) + 0.5f));
        int g = ((int) ((t * 255.0f) + 0.5f));
        int b = ((int) ((p * 255.0f) + 0.5f));
        break label_29;
        label_10:
        r = ((int) ((q * 255.0f) + 0.5f));
        g = ((int) ((value * 255.0f) + 0.5f));
        b = ((int) ((p * 255.0f) + 0.5f));
        break label_29;
        label_14:
        r = ((int) ((p * 255.0f) + 0.5f));
        g = ((int) ((value * 255.0f) + 0.5f));
        b = ((int) ((t * 255.0f) + 0.5f));
        break label_29;
        label_18:
        r = ((int) ((p * 255.0f) + 0.5f));
        g = ((int) ((q * 255.0f) + 0.5f));
        b = ((int) ((value * 255.0f) + 0.5f));
        break label_29;
        label_22:
        r = ((int) ((t * 255.0f) + 0.5f));
        g = ((int) ((p * 255.0f) + 0.5f));
        b = ((int) ((value * 255.0f) + 0.5f));
        break label_29;
        label_26:
        r = ((int) ((value * 255.0f) + 0.5f));
        g = ((int) ((p * 255.0f) + 0.5f));
        b = ((int) ((q * 255.0f) + 0.5f));
        label_29:
        return (((r << 16) | (g << 8)) | b);
    }


    public enum RenderStyle {
        SOLID,
        OUTLINE,
        FILL_ONLY;


        private RenderStyle() {
        }

    }

    record CachedBlock(class_238 box, BlockESP.BlockConfig config, double centerX, double centerY, double centerZ, long seed) {

        private CachedBlock(class_238 box, BlockESP.BlockConfig config, double centerX, double centerY, double centerZ, long seed) {
            this.box = box;
            this.config = config;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.seed = seed;
        }

        public class_238 box() {
            return this.box;
        }

        public BlockESP.BlockConfig config() {
            return this.config;
        }

        public double centerX() {
            return this.centerX;
        }

        public double centerY() {
            return this.centerY;
        }

        public double centerZ() {
            return this.centerZ;
        }

        public long seed() {
            return this.seed;
        }

    }

    public record BlockOption(class_2960 id, String name, class_1799 icon, String searchKey) {

        public BlockOption(class_2960 id, String name, class_1799 icon, String searchKey) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.searchKey = searchKey;
        }

        public class_2960 id() {
            return this.id;
        }

        public String name() {
            return this.name;
        }

        public class_1799 icon() {
            return this.icon;
        }

        public String searchKey() {
            return this.searchKey;
        }

    }

    final class BlockConfig {
        final class_2248 block;
        final class_2960 id;
        final BoolSetting enabled;
        final ColorSetting color;

        BlockConfig(class_2248 block, class_2960 id, BoolSetting enabled, ColorSetting color) {
            this.block = block;
            this.id = id;
            this.enabled = enabled;
            this.color = color;
        }

    }

    public enum ColorMode {
        FILTER_COLOR,
        DISTANCE,
        RAINBOW;


        private ColorMode() {
        }

    }
}

package me.drake.viecheats.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.gui.StorageFilterScreen;
import me.drake.viecheats.util.PerformanceHelper;
import me.drake.viecheats.util.RainbowUtil;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2586;
import net.minecraft.class_2589;
import net.minecraft.class_2595;
import net.minecraft.class_2601;
import net.minecraft.class_2608;
import net.minecraft.class_2611;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_3719;
import net.minecraft.class_3720;
import net.minecraft.class_3723;
import net.minecraft.class_3866;

public class StorageESP extends Module {
    private static final int MAX_RANGE = 32;
    private final ButtonSetting openFilter;
    private final EnumSetting colorTheme;
    private final BoolSetting tracers;
    private final IntSetting alpha;
    private final IntSetting maxRender;
    private final IntSetting rescanTicks;
    private final BoolSetting chestEnabled;
    private final BoolSetting barrelEnabled;
    private final BoolSetting shulkerEnabled;
    private final BoolSetting enderChestEnabled;
    private final BoolSetting spawnerEnabled;
    private final BoolSetting hopperEnabled;
    private final BoolSetting furnaceEnabled;
    private final BoolSetting blastFurnaceEnabled;
    private final BoolSetting smokerEnabled;
    private final BoolSetting dispenserEnabled;
    private final BoolSetting dropperEnabled;
    private final BoolSetting brewingEnabled;
    private final ColorSetting chestColor;
    private final ColorSetting barrelColor;
    private final ColorSetting shulkerColor;
    private final ColorSetting enderChestColor;
    private final ColorSetting spawnerColor;
    private final ColorSetting hopperColor;
    private final ColorSetting furnaceColor;
    private final ColorSetting blastFurnaceColor;
    private final ColorSetting smokerColor;
    private final ColorSetting dispenserColor;
    private final ColorSetting dropperColor;
    private final ColorSetting brewingColor;
    private final Map chunkCache;
    private int cachedStorageCount;
    private int rescanTimer;
    private int scanRingIndex;
    private volatile int cachedAlpha;
    private StorageESP.ColorTheme cachedTheme;

    public StorageESP() {
        super("StorageESP", "Highlights storage blocks", Category.RENDER);
        this.openFilter = ((ButtonSetting) addSetting(new ButtonSetting("Open Filter", "Open block type filter", this::openFilterScreen)));
        this.colorTheme = ((EnumSetting) addSetting(new EnumSetting("ColorTheme", "StorageESP color palette", StorageESP.ColorTheme.DEFAULT)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracer lines", false)));
        this.alpha = ((IntSetting) addSetting(new IntSetting("Alpha", "Fill opacity", 125, 5, 255)));
        this.maxRender = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("MaxRender", "Max rendered per frame", 8192, 50, 65536)))));
        this.rescanTicks = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("RescanTicks", "Ticks between rescans", 8, 1, 40)))));
        this.chestEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("ChestEnabled", "Enable chest", true)))));
        this.barrelEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("BarrelEnabled", "Enable barrel", true)))));
        this.shulkerEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("ShulkerEnabled", "Enable shulker", true)))));
        this.enderChestEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("EnderChestEnabled", "Enable ender chest", true)))));
        this.spawnerEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("SpawnerEnabled", "Enable spawner", false)))));
        this.hopperEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("HopperEnabled", "Enable hopper", true)))));
        this.furnaceEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("FurnaceEnabled", "Enable furnace", true)))));
        this.blastFurnaceEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("BlastFurnaceEnabled", "Enable blast furnace", true)))));
        this.smokerEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("SmokerEnabled", "Enable smoker", true)))));
        this.dispenserEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("DispenserEnabled", "Enable dispenser", true)))));
        this.dropperEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("DropperEnabled", "Enable dropper", true)))));
        this.brewingEnabled = ((BoolSetting) addSetting(((BoolSetting) hidden(new BoolSetting("BrewingEnabled", "Enable brewing stand", true)))));
        this.chestColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("ChestColor", "Chest color", -6530304)))));
        this.barrelColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("BarrelColor", "Barrel color", -29556)))));
        this.shulkerColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("ShulkerColor", "Shulker color", -7995234)))));
        this.enderChestColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("EnderChestColor", "Ender chest color", -9109249)))));
        this.spawnerColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("SpawnerColor", "Spawner color", -7700826)))));
        this.hopperColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("HopperColor", "Hopper color", -9803158)))));
        this.furnaceColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("FurnaceColor", "Furnace color", -8553091)))));
        this.blastFurnaceColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("BlastFurnaceColor", "Blast furnace color", -8553091)))));
        this.smokerColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("SmokerColor", "Smoker color", -8553091)))));
        this.dispenserColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("DispenserColor", "Dispenser color", -11579569)))));
        this.dropperColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("DropperColor", "Dropper color", -11579569)))));
        this.brewingColor = ((ColorSetting) addSetting(((ColorSetting) hidden(new ColorSetting("BrewingColor", "Brewing stand color", -5601216)))));
        this.chunkCache = new ConcurrentHashMap();
        this.cachedStorageCount = 0;
        this.rescanTimer = 0;
        this.scanRingIndex = 0;
        this.cachedAlpha = -1;
        this.cachedTheme = null;
    }

    private static Setting hidden(Setting setting) {
        setting.setVisibleInGui(false);
        return setting;
    }

    public boolean isTypeEnabled(StorageESP.StorageType type) {
        return ((Boolean) enabledSetting(type).get()).booleanValue();
    }

    public void setTypeEnabled(StorageESP.StorageType type, boolean enabled) {
        enabledSetting(type).set(enabled);
        fullRescan();
    }

    public int getTypeColor(StorageESP.StorageType type) {
        return ((Integer) colorSetting(type).get()).intValue();
    }

    public void setTypeColor(StorageESP.StorageType type, int rgb) {
        colorSetting(type).set((-16777216 | (rgb & 16777215)));
    }

    public void openFilterScreen() {
        mc.method_1507(new StorageFilterScreen(this));
    }

    public void onEnable() {
        super.onEnable();
        this.cachedAlpha = ((Integer) this.alpha.get()).intValue();
        applyColorTheme(((StorageESP.ColorTheme) this.colorTheme.get()));
        this.cachedTheme = ((StorageESP.ColorTheme) this.colorTheme.get());
        fullRescan();
    }

    public void onDisable() {
        super.onDisable();
        this.chunkCache.clear();
        this.cachedStorageCount = 0;
    }

    public String getInfo() {
        return (this.cachedStorageCount + " | 32ch");
    }

    public boolean usesEntityCache() {
        return false;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!(isInGame())) return;
        if (e.isLoaded()) {
            scanChunk(e.getChunkPos());
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (e.getPhase() != TickEvent.Phase.PRE) break label_1;
        if (!(isInGame())) {
            label_1:
            return;
        }
        int newAlpha = ((Integer) this.alpha.get()).intValue();
        if (this.cachedAlpha != newAlpha) {
            this.cachedAlpha = newAlpha;
        }
        StorageESP.ColorTheme selectedTheme = ((StorageESP.ColorTheme) this.colorTheme.get());
        if (selectedTheme != this.cachedTheme) {
            this.cachedTheme = selectedTheme;
            applyColorTheme(selectedTheme);
            fullRescan();
        }
        int interval = PerformanceHelper.scaleInterval(((Integer) this.rescanTicks.get()).intValue());
        this.rescanTimer = (this.rescanTimer + 1);
        if ((this.rescanTimer + 1) >= interval) {
            this.rescanTimer = 0;
            rescanNearby();
            cleanupOldChunks();
        }
    }

    private void cleanupOldChunks() {
        if (mc.field_1724 == null) return;
        int rd = 34;
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        this.chunkCache.keySet().removeIf((pos) -> {
    if (Math.abs((pos.field_9181 - pcx)) <= rd) {
        if (Math.abs((pos.field_9180 - pcz)) > rd) {
        }
    } else {
    }
    return false;
});
    }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (e.getStage() != RenderEvent.Stage.WORLD_3D) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        int fillA = this.cachedAlpha;
        if (fillA <= 0) return;
        boolean drawTracers = ((Boolean) this.tracers.get()).booleanValue();
        float tickDelta = e.getTickDelta();
        double px = mc.field_1724.method_23317();
        double py = mc.field_1724.method_23318();
        double pz = mc.field_1724.method_23321();
        int renderCap = PerformanceHelper.scaleRenderCap(((Integer) this.maxRender.get()).intValue());
        int rendered = 0;
        Object var13 = this.chunkCache.values().iterator();
        while (var13.hasNext()) {
            List entries = ((List) var13.next());
            Object var15 = entries.iterator();
            while (var15.hasNext()) {
                StorageESP.CachedStorage cs = ((StorageESP.CachedStorage) var15.next());
                if (rendered >= renderCap) return;
                double dx = (cs.centerX - px);
                double dz = (cs.centerZ - pz);
                double distSq = ((dx * dx) + (dz * dz));
                if (distSq > cs.maxDistSq) {
                    break label_14;
                }
                RenderUtil.drawFilledBox(cs.coreBox, RainbowUtil.withAlpha(cs.color, fillA));
                if (!(!drawTracers)) {
                    int tracerColor = RainbowUtil.withAlpha(cs.color, 255);
                    RenderUtil.drawTracerToPos(cs.centerX, cs.centerY, cs.centerZ, tracerColor, tickDelta);
                }
                rendered++;
            }
        }
    }

    private BoolSetting enabledSetting(StorageESP.StorageType type) {
        switch (type.ordinal()) {
            case 0 -> { break label_2; }
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            case 6 -> { break label_8; }
            case 7 -> { break label_9; }
            case 8 -> { break label_10; }
            case 9 -> { break label_11; }
            case 10 -> { break label_12; }
            case 11 -> { break label_13; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_14;
        label_3:
        break label_14;
        label_4:
        break label_14;
        label_5:
        break label_14;
        label_6:
        break label_14;
        label_7:
        break label_14;
        label_8:
        break label_14;
        label_9:
        break label_14;
        label_10:
        break label_14;
        label_11:
        break label_14;
        label_12:
        break label_14;
        label_13:
        label_14:
        return this.brewingEnabled;
    }

    private ColorSetting colorSetting(StorageESP.StorageType type) {
        switch (type.ordinal()) {
            case 0 -> { break label_2; }
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            case 6 -> { break label_8; }
            case 7 -> { break label_9; }
            case 8 -> { break label_10; }
            case 9 -> { break label_11; }
            case 10 -> { break label_12; }
            case 11 -> { break label_13; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_14;
        label_3:
        break label_14;
        label_4:
        break label_14;
        label_5:
        break label_14;
        label_6:
        break label_14;
        label_7:
        break label_14;
        label_8:
        break label_14;
        label_9:
        break label_14;
        label_10:
        break label_14;
        label_11:
        break label_14;
        label_12:
        break label_14;
        label_13:
        label_14:
        return this.brewingColor;
    }

    private void applyColorTheme(StorageESP.ColorTheme theme) {
        switch (theme.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_14; }
            case 2 -> { break label_27; }
            default -> { break label_39; }
        }
        label_1:
        this.chestColor.set(-6530304);
        this.barrelColor.set(-29556);
        this.shulkerColor.set(-7995234);
        this.enderChestColor.set(-9109249);
        this.spawnerColor.set(-7700826);
        this.hopperColor.set(-9803158);
        this.furnaceColor.set(-8553091);
        this.blastFurnaceColor.set(-8553091);
        this.smokerColor.set(-8553091);
        this.dispenserColor.set(-11579569);
        this.dropperColor.set(-11579569);
        this.brewingColor.set(-5601216);
        break label_39;
        label_14:
        this.chestColor.set(-3773925);
        this.barrelColor.set(-20338);
        this.shulkerColor.set(-5221242);
        this.enderChestColor.set(-5219329);
        this.spawnerColor.set(-4547974);
        this.hopperColor.set(-7571356);
        this.furnaceColor.set(-6584458);
        this.blastFurnaceColor.set(-6584458);
        this.smokerColor.set(-6584458);
        this.dispenserColor.set(-9083044);
        this.dropperColor.set(-9083044);
        this.brewingColor.set(-3695798);
        break label_39;
        label_27:
        this.chestColor.set(-11763528);
        this.barrelColor.set(-7812865);
        this.shulkerColor.set(-10585138);
        this.enderChestColor.set(-11899649);
        this.spawnerColor.set(-8479546);
        this.hopperColor.set(-4265330);
        this.furnaceColor.set(-4725887);
        this.blastFurnaceColor.set(-4725887);
        this.smokerColor.set(-4725887);
        this.dispenserColor.set(-3542372);
        this.dropperColor.set(-3542372);
        this.brewingColor.set(-4924550);
        label_39:
    }

    private void rescanNearby() {
        if (mc.field_1724 == null) break label_1;
        if (mc.field_1687 != null) break label_2;
        label_1:
        return;
        label_2:
        int pcx = mc.field_1724.method_31476().field_9181;
        int pcz = mc.field_1724.method_31476().field_9180;
        int budget = PerformanceHelper.scaleBudget(12);
        if (budget <= 0) return;
        int dx = -1;
        while (dx <= 1) {
            int dz = -1;
            while (dz <= 1) {
                if (budget <= 0) return;
                scanChunk(new class_1923((pcx + dx), (pcz + dz)));
                budget--;
                dz++;
            }
            dx++;
        }
        int rd = 32;
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
                    if (budget <= 0) return;
                    scanChunk(new class_1923((pcx + dx), (pcz + dz)));
                    budget--;
                    dz++;
                }
                break label_38;
            }
            if (budget > 0) {
                scanChunk(new class_1923((pcx + dx), (pcz - ring)));
                budget--;
            }
            if (budget <= 0) break label_38;
            scanChunk(new class_1923((pcx + dx), (pcz + ring)));
            budget--;
            label_38:
            dx++;
        }
        cleanupOutOfRangeChunks(pcx, pcz, (rd + 1));
    }

    private void fullRescan() {
        this.chunkCache.clear();
        this.cachedStorageCount = 0;
        this.scanRingIndex = 0;
        if ((mc.field_1687 == null) || (mc.field_1724 == null)) {
            return;
        }
        int rd = 32;
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
        int rd = 33;
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
        List list = new ArrayList();
        List old = chunk.method_12214().entrySet().iterator();
        while (old.hasNext()) {
            Map.Entry entry = ((Map.Entry) old.next());
            StorageESP.StorageType type = getType(((class_2586) entry.getValue()));
            if (type == null) break label_14;
            if (!(isTypeEnabled(type))) {
                break label_14;
            }
            class_2338 bp = ((class_2338) entry.getKey());
            class_238 coreBox = new class_238((((double) bp.method_10263()) + 0.1), (((double) bp.method_10264()) + 0.05), (((double) bp.method_10260()) + 0.1), (((double) bp.method_10263()) + 0.9), (((double) bp.method_10264()) + 0.85), (((double) bp.method_10260()) + 0.9));
            double centerY = (((double) bp.method_10264()) + 0.5);
            int color = ((Integer) colorSetting(type).get()).intValue();
            double maxDist = 528.0;
            double maxDistSq = (maxDist * maxDist);
            list.add(new StorageESP.CachedStorage(coreBox, color, (((double) bp.method_10263()) + 0.5), centerY, (((double) bp.method_10260()) + 0.5), maxDistSq, bp.method_10063()));
        }
        old = ((List) this.chunkCache.get(pos));
        if (old == null) {
        } else {
        }
        int oldSize = old.size();
        int newSize = list.size();
        this.cachedStorageCount = (this.cachedStorageCount + (newSize - oldSize));
        if (newSize == 0) {
            this.chunkCache.remove(pos);
        } else {
            this.chunkCache.put(pos, list);
        }
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
            this.cachedStorageCount = (this.cachedStorageCount - old.size());
        }
    }

    private StorageESP.StorageType getType(class_2586 be) {
        Objects.requireNonNull(be);
        Object var2 = be;
        int var3 = 0;
        switch (typeSwitch(var2, var3)) {
            case 0 -> { break label_2; }
            case 1 -> { break label_4; }
            case 2 -> { break label_6; }
            case 3 -> { break label_8; }
            case 4 -> { break label_10; }
            case 5 -> { break label_12; }
            case 6 -> { break label_14; }
            case 7 -> { break label_16; }
            case 8 -> { break label_18; }
            case 9 -> { break label_20; }
            case 10 -> { break label_22; }
            case 11 -> { break label_24; }
            default -> { break label_26; }
        }
        label_2:
        class_2595 i = ((class_2595) var2);
        break label_27;
        label_4:
        i = ((class_3719) var2);
        break label_27;
        label_6:
        i = ((class_2627) var2);
        break label_27;
        label_8:
        i = ((class_2611) var2);
        break label_27;
        label_10:
        i = ((class_2636) var2);
        break label_27;
        label_12:
        i = ((class_2614) var2);
        break label_27;
        label_14:
        i = ((class_2608) var2);
        break label_27;
        label_16:
        i = ((class_2601) var2);
        break label_27;
        label_18:
        i = ((class_3720) var2);
        break label_27;
        label_20:
        i = ((class_3723) var2);
        break label_27;
        label_22:
        i = ((class_3866) var2);
        break label_27;
        label_24:
        i = ((class_2589) var2);
        break label_27;
        label_26:
        label_27:
        return null;
    }


    public enum ColorTheme {
        DEFAULT,
        WARM,
        COOL;


        private ColorTheme() {
        }

    }

    public enum StorageType {
        CHEST,
        BARREL,
        SHULKER,
        ENDER_CHEST,
        SPAWNER,
        HOPPER,
        FURNACE,
        BLAST_FURNACE,
        SMOKER,
        DISPENSER,
        DROPPER,
        BREWING_STAND;


        private StorageType() {
        }

    }

    record CachedStorage(class_238 coreBox, int color, double centerX, double centerY, double centerZ, double maxDistSq, long seed) {

        private CachedStorage(class_238 coreBox, int color, double centerX, double centerY, double centerZ, double maxDistSq, long seed) {
            this.coreBox = coreBox;
            this.color = color;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.maxDistSq = maxDistSq;
            this.seed = seed;
        }

        public class_238 coreBox() {
            return this.coreBox;
        }

        public int color() {
            return this.color;
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

        public double maxDistSq() {
            return this.maxDistSq;
        }

        public long seed() {
            return this.seed;
        }

    }
}

package me.drake.viecheats.chunkfinder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class ChunkFinderClientInitializer implements ClientModInitializer {
    private static final Logger LOGGER;
    private static final AtomicBoolean INITIALIZED;
    private static final ChunkFinderScanner SCANNER;
    private static final ChunkFinderRenderer RENDERER;
    private static volatile boolean enabled;
    private static volatile int scanRadiusChunks;
    private static volatile long scanIntervalMs;

    public void onInitializeClient() {
        initialize();
    }

    public static void initialize() {
        if (!(INITIALIZED.compareAndSet(false, true))) return;
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        Objects.requireNonNull(RENDERER);
        WorldRenderEvents.END_MAIN.register(RENDERER::onWorldRender);
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> SCANNER.shutdown());
        LOGGER.info("[ChunkFinder] Initialized (ClickGUI controlled).");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        boolean wasEnabled = enabled;
        enabled = value;
        if (!wasEnabled && !(!value)) {
            SCANNER.resetCooldown();
            SCANNER.triggerImmediateScan(class_310.method_1551(), scanRadiusChunks);
        }
    }

    public static int getScanRadiusChunks() {
        return scanRadiusChunks;
    }

    public static void setScanRadiusChunks(int radius) {
        scanRadiusChunks = Math.max(1, Math.min(12, radius));
    }

    public static long getScanIntervalMs() {
        return scanIntervalMs;
    }

    public static void setScanIntervalMs(long intervalMs) {
        scanIntervalMs = Math.max(1000L, intervalMs);
    }

    public static void setSeedMismatchPercent(int percent) {
        int clamped = Math.max(35, Math.min(98, percent));
        SCANNER.setMaxSeedMismatchRatio((((double) clamped) / 100.0));
    }

    public static void setSeedSampleMin(int samples) {
        SCANNER.setMinSeedSamplesPerChunk(samples);
    }

    public static void setMinFlagsPerChunk(int flags) {
        SCANNER.setMinFlagsPerChunk(flags);
    }

    public static void setHostSeedSupport(int support) {
        SCANNER.setHostSeedSupportRequired(support);
    }

    public static boolean isScanInFlight() {
        return SCANNER.isScanning();
    }

    public static int getFlaggedCount() {
        return SCANNER.getFindingsSnapshot().size();
    }

    private static void onEndClientTick(class_310 client) {
        if (client == null) return;
        if (!enabled) return;
        SCANNER.tick(client, scanRadiusChunks, scanIntervalMs);
    }

    static {
        LOGGER = LoggerFactory.getLogger("VieCheats-ChunkFinder");
        INITIALIZED = new AtomicBoolean(false);
        SCANNER = new ChunkFinderScanner();
        RENDERER = new ChunkFinderRenderer(SCANNER, this::isEnabled);
        enabled = false;
        scanRadiusChunks = 4;
        scanIntervalMs = 5000L;
    }

}

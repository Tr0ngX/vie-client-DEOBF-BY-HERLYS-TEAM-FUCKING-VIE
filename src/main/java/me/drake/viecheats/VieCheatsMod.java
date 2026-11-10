package me.drake.viecheats;

import java.util.ArrayList;
import java.util.List;
import me.drake.viecheats.auth.AuthManager;
import me.drake.viecheats.core.command.CommandManager;
import me.drake.viecheats.core.config.ConfigManager;
import me.drake.viecheats.core.event.EventBus;
import me.drake.viecheats.core.module.ModuleManager;
import me.drake.viecheats.gui.ClickGUI;
import me.drake.viecheats.gui.HudManager;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.render.ViePipelines;
import me.drake.viecheats.security.RuntimeGuard;
import me.drake.viecheats.security.SecurityGuard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class VieCheatsMod implements ClientModInitializer {
    public static final String MOD_ID = "vie_cheats";
    public static final String MOD_NAME = "Vie Cheats";
    public static final String VERSION = "1.0.0";
    public static final String AUTHOR = "Drake";
    public static final Logger LOGGER;
    private static VieCheatsMod INSTANCE;
    private EventBus eventBus;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private HudManager hudManager;
    private VieCheatsMod.FriendManager friendManager;
    private AuthManager authManager;
    private RuntimeGuard runtimeGuard;
    private boolean runtimeCompromised;
    private String lastRuntimeAuthStatus;
    private static final boolean AUTH_ENABLED = true;
    private class_304 guiKeyBinding;
    private boolean guiHotkeyHeld;
    private long lastGuiOpenAtMs;
    private int tickCounter;
    private static final int SECURITY_GUARD_INTERVAL_TICKS = 600;
    private static final int RUNTIME_GUARD_INTERVAL_TICKS = 20;
    private static final int AUTH_TICK_INTERVAL_TICKS = 100;
    private static final int AUTH_GATE_INTERVAL_TICKS = 100;
    private static final long GUI_OPEN_COOLDOWN_MS = 140L;

    public VieCheatsMod() {
        this.runtimeCompromised = false;
        this.lastRuntimeAuthStatus = "";
        this.guiHotkeyHeld = false;
        this.lastGuiOpenAtMs = 0L;
        this.tickCounter = 0;
    }

    public void onInitializeClient() {
        INSTANCE = this;
        printBanner();
        LOGGER.info("[VieCheat] Initializing SecurityGuard...");
        try {
            SecurityGuard.initialize();
        } catch (Throwable ex5) {
            Throwable t = ex5;
            LOGGER.error("[VieCheat] SecurityGuard init failed", t);
        }
        if (SecurityGuard.isCompromised()) {
            LOGGER.error("[VieCheat] Security check failed. Aborting initialization.");
            return;
        }
        LOGGER.info("[VieCheat] Initializing EventBus...");
        this.eventBus = new EventBus();
        LOGGER.info("[VieCheat] Initializing ConfigManager...");
        this.configManager = new ConfigManager();
        LOGGER.info("[VieCheat] Initializing FriendManager...");
        this.friendManager = new VieCheatsMod.FriendManager();
        LOGGER.info("[VieCheat] Initializing ModuleManager...");
        this.moduleManager = new ModuleManager();
        LOGGER.info("[VieCheat] Initializing AuthManager...");
        this.authManager = new AuthManager();
        LOGGER.info("[VieCheat] Initializing RuntimeGuard...");
        this.runtimeGuard = new RuntimeGuard();
        LOGGER.info("[VieCheat] Initializing CommandManager...");
        this.commandManager = new CommandManager();
        LOGGER.info("[VieCheat] Initializing HudManager...");
        this.hudManager = new HudManager();
        if (this.authManager == null) break label_39;
        try {
            this.authManager.initialize();
            this.authManager.validateStoredKey();
            enforceStartupAuthGate();
        } catch (IllegalStateException ex31) {
            IllegalStateException e = ex31;
            LOGGER.error("[VieCheat] Startup auth check failed. Terminating client: {}", e.getMessage());
            Runtime.getRuntime().halt(1);
            return;
        } catch (Throwable ex35) {
            t = ex35;
            LOGGER.error("[VieCheat] Startup auth check crashed. Terminating client.", t);
            Runtime.getRuntime().halt(1);
            return;
        }
        try {
        }
        label_39:
        checkRuntimeSecurityOnce();
        } catch (IllegalStateException ex41) {
            e = ex41;
            LOGGER.warn("[VieCheat] Runtime security check failed: {}", e.getMessage());
        }
        this.configManager.load();
        this.configManager.save();
        ClientSettings cs = ClientSettings.get();
        if (cs != null) {
        } else {
        }
        int guiKey = 344;
        this.guiKeyBinding = KeyBindingHelper.registerKeyBinding(new class_304("key.vie_cheats.open_gui", guiKey, class_304.class_11900.field_62556));
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new VieCheatsMod.1(this));
        LOGGER.info("[VieCheat] Vie Cheats v{} by {} \u2014 Loaded successfully!", "1.0.0", "Drake");
    }

    private void onClientTick(class_310 client) {
        handleGuiHotkey(client);
        if (((client == null) || (client.field_1687 == null)) || (ViePipelines.isPrecompiled)) break label_6;
        try {
            ViePipelines.precompile();
        } catch (Throwable ex4) {
            Throwable t = ex4;
            LOGGER.error("[VieCheat] Pipeline precompile tick attempt failed", t);
        }
        label_6:
        this.tickCounter = (this.tickCounter + 1);
        if (client != null && client.field_1687 != null && (this.tickCounter % 600) == 0) {
            SecurityGuard.verifyIntegrity();
        }
        if (((this.runtimeGuard == null) || (this.runtimeCompromised)) || ((this.tickCounter % 20) != 0)) break label_13;
        String violation = this.runtimeGuard.checkPeriodic();
        if (violation == null) break label_13;
        applyRuntimeCompromise(violation);
        label_13:
        if (this.configManager != null) {
            this.configManager.tickAutosave();
        }
        if (this.authManager != null && (this.tickCounter % 100) == 0) {
            this.authManager.tick();
        }
        if ((this.authManager == null) || ((this.tickCounter % 100) != 0)) break label_24;
        try {
            enforceRuntimeAuthGate();
        } catch (IllegalStateException ex20) {
            IllegalStateException e = ex20;
            LOGGER.error("[VieCheat] Runtime auth check failed. Terminating client: {}", e.getMessage());
            Runtime.getRuntime().halt(1);
            return;
        }
        label_24:
        if ((this.runtimeCompromised) || (SecurityGuard.isCompromised())) {
            return;
        }
        if (this.moduleManager != null) {
            this.moduleManager.onTick();
        }
        if (((client == null) || (client.field_1724 == null)) || (client.field_1687 == null)) {
            return;
        }
    }

    private void onClientStarted(class_310 client) {
        if (client == null) return;
        if (!(!ViePipelines.isPrecompiled)) {
            return;
            try {
            }
            ViePipelines.precompile();
        } catch (Throwable ex4) {
            Object var2 = ex4;
        }
    }

    private void handleGuiHotkey(class_310 client) {
        if (client == null) break label_1;
        if (client.method_22683() == null) {
            label_1:
            return;
        }
        ClientSettings cs = ClientSettings.get();
        if (cs != null) {
        } else {
        }
        int guiKey = 344;
        long handle = client.method_22683().method_4490();
        if (GLFW.glfwGetKey(handle, guiKey) != 1) {
            if (guiKey != 260 && GLFW.glfwGetKey(handle, 260) == 1) {
            }
        } else {
        }
        boolean fallbackGuiHotkey = false;
        if (!fallbackGuiHotkey) break label_13;
        boolean hotkeyEdge = !this.guiHotkeyHeld;
        this.guiHotkeyHeld = fallbackGuiHotkey;
        if (this.guiKeyBinding != null) {
            if (!(this.guiKeyBinding.method_1436())) {
            }
            if (!(!hotkeyEdge)) {
            }
            openClickGui(client);
        }
    }

    public void openClickGuiFromKeyPress(class_310 client) {
        openClickGui(client);
    }

    private void openClickGui(class_310 client) {
        if (client == null) return;
        long now = System.currentTimeMillis();
        if ((now - this.lastGuiOpenAtMs) < 140L) return;
        this.lastGuiOpenAtMs = now;
        if ((client.field_1755 instanceof ClickGUI)) {
            client.method_1507(null);
            return;
        }
        client.method_1507(new ClickGUI());
    }

    private void onClientStopping(class_310 client) {
        if (this.configManager != null) {
            this.configManager.save();
        }
    }

    private void checkRuntimeSecurityOnce() {
        if ((this.runtimeGuard == null) || (!(!this.runtimeCompromised))) {
            return;
        }
        String violation = this.runtimeGuard.checkStartup();
        if (violation != null) {
            applyRuntimeCompromise(violation);
        }
    }

    private void applyRuntimeCompromise(String reason) {
        if (!(!this.runtimeCompromised)) return;
        this.runtimeCompromised = true;
        LOGGER.error("[VieCheat] RuntimeGuard triggered: {}", reason);
        if (this.authManager != null) {
            this.authManager.forceLockdown(("Runtime guard blocked client: " + reason));
        }
        if (this.moduleManager != null) {
            this.moduleManager.disableProtectedModules();
        }
        class_310 client = class_310.method_1551();
        if (client != null && client.field_1724 != null) {
            client.field_1724.method_7353(class_2561.method_43470("\u00a7d[VC]\u00a7c Security violation detected. Protected modules disabled."), false);
        }
    }

    private void enforceStartupAuthGate() {
        if (this.authManager == null) throw new IllegalStateException("[VC] Auth manager is not initialized.");
        AuthManager.StatusSnapshot snapshot = this.authManager.getStatusSnapshot();
        if (snapshot.authorized()) return;
        if (snapshot.status() == null) {
        } else {
        }
        String status = snapshot.status();
        String reason = ("[VC] Startup auth not ready (" + status + "): " + snapshot.message());
        LOGGER.warn("{}", reason);
        if (this.moduleManager != null) {
            this.moduleManager.disableProtectedModules();
        }
        throw new IllegalStateException(reason);
    }

    private void enforceRuntimeAuthGate() {
        if (this.authManager == null) return;
        AuthManager.StatusSnapshot snapshot = this.authManager.getStatusSnapshot();
        if (snapshot.status() == null) {
        } else {
        }
        String status = snapshot.status();
        if (snapshot.authorized()) {
            this.lastRuntimeAuthStatus = "";
            return;
        }
        if (!(status.equals(this.lastRuntimeAuthStatus))) {
            this.lastRuntimeAuthStatus = status;
            LOGGER.warn("[VC] Runtime auth status changed ({}): {}", status, snapshot.message());
            if (this.moduleManager != null) {
                this.moduleManager.disableProtectedModules();
            }
            throw new IllegalStateException(("[VC] Runtime auth failure (" + status + "): " + snapshot.message()));
        }
    }

    private void printBanner() {
        String banner = "\n\u2588\u2588\u2557   \u2588\u2588\u2557\u2588\u2588\u2557\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557     \u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2588\u2588\u2557  \u2588\u2588\u2557\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557 \u2588\u2588\u2588\u2588\u2588\u2557 \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557\n\u2588\u2588\u2551   \u2588\u2588\u2551\u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2550\u2550\u2550\u255d    \u2588\u2588\u2554\u2550\u2550\u2550\u2550\u255d\u2588\u2588\u2551  \u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2550\u2550\u2550\u255d\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u255a\u2550\u2550\u2588\u2588\u2554\u2550\u2550\u255d\u2588\u2588\u2554\u2550\u2550\u2550\u2550\u255d\n\u2588\u2588\u2551   \u2588\u2588\u2551\u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2557      \u2588\u2588\u2551     \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2557  \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2551   \u2588\u2588\u2551   \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557\n\u255a\u2588\u2588\u2557 \u2588\u2588\u2554\u255d\u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2550\u255d      \u2588\u2588\u2551     \u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2550\u255d  \u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2551   \u2588\u2588\u2551   \u255a\u2550\u2550\u2550\u2550\u2588\u2588\u2551\n \u255a\u2588\u2588\u2588\u2588\u2554\u255d \u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557    \u255a\u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2588\u2588\u2551  \u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2588\u2588\u2551  \u2588\u2588\u2551   \u2588\u2588\u2551   \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2551\n  \u255a\u2550\u2550\u2550\u255d  \u255a\u2550\u255d\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u255d     \u255a\u2550\u2550\u2550\u2550\u2550\u255d\u255a\u2550\u255d  \u255a\u2550\u255d\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u255d\u255a\u2550\u255d  \u255a\u2550\u255d   \u255a\u2550\u255d   \u255a\u2550\u2550\u2550\u2550\u2550\u2550\u255d\n                          by Drake | v1.0.0\n";
        LOGGER.info(banner);
    }

    public static VieCheatsMod getInstance() {
        return INSTANCE;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public HudManager getHudManager() {
        return this.hudManager;
    }

    public VieCheatsMod.FriendManager getFriendManager() {
        return this.friendManager;
    }

    public AuthManager getAuthManager() {
        return this.authManager;
    }

    static {
        LOGGER = LoggerFactory.getLogger("Vie Cheats");
    }


    class 1 implements SimpleSynchronousResourceReloadListener {
        1(VieCheatsMod this$0) {
        }

        public class_2960 getFabricId() {
            return class_2960.method_60655("vie_cheats", "pipeline_reloader");
        }

        public void method_14491(class_3300 manager) {
            VieCheatsMod.LOGGER.info("[VieCheat] Resources reloaded, invalidating pipelines...");
            ViePipelines.isPrecompiled = false;
        }

    }

    public class FriendManager {
        private final List friends;

        public FriendManager() {
            this.friends = new ArrayList();
        }

        public void add(String name) {
            if (!(this.friends.contains(name.toLowerCase()))) {
                this.friends.add(name.toLowerCase());
            }
        }

        public void remove(String name) {
            this.friends.remove(name.toLowerCase());
        }

        public boolean isFriend(String name) {
            return this.friends.contains(name.toLowerCase());
        }

        public List getFriends() {
            return new ArrayList(this.friends);
        }

    }
}

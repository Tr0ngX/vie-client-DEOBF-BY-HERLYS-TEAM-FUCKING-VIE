package me.drake.viecheats.modules.client;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ButtonSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.KeybindSetting;
import me.drake.viecheats.core.setting.StringSetting;
import me.drake.viecheats.util.RainbowUtil;
import net.minecraft.class_310;
import net.minecraft.class_408;

public class ClientSettings extends Module {
    private static final double LOCKED_GUI_SCALE = 2.0;
    private static final long RPC_RECONNECT_INTERVAL_MS = 10000L;
    private static final double TEXT_RGB_SPEED = 1.0;
    private static final double TEXT_RGB_SATURATION = 1.0;
    private final EnumSetting theme;
    private final ColorSetting primaryColor;
    private final ColorSetting secondaryColor;
    private final ColorSetting textColor;
    private final EnumSetting textColorMode;
    private final EnumSetting moduleListPos;
    private final EnumSetting watermarkPos;
    private final BoolSetting showInfoBar;
    private final BoolSetting showCoords;
    private final BoolSetting showNotifs;
    private final IntSetting topInfoOffsetX;
    private final IntSetting topInfoOffsetY;
    private final IntSetting coordsOffsetX;
    private final IntSetting coordsOffsetY;
    private final IntSetting moduleListOffsetX;
    private final IntSetting moduleListOffsetY;
    private final IntSetting hotkeysOffsetX;
    private final IntSetting hotkeysOffsetY;
    private final IntSetting hotkeysScale;
    private final ButtonSetting hudEditor;
    private final KeybindSetting clickGuiKey;
    private final BoolSetting discordRPC;
    private final StringSetting rpcDetail;
    private DiscordPresence discord;
    private boolean wasRpcEnabled;
    private long lastRpcConnectAttemptAtMs;

    public ClientSettings() {
        super("ClientSettings", "Personalize your client", Category.CLIENT);
        this.theme = ((EnumSetting) addSetting(new EnumSetting("Theme", "Theme preset", ClientSettings.Theme.CUSTOM)));
        this.primaryColor = ((ColorSetting) addSetting(new ColorSetting("PrimaryColor", "Main accent color", -38476)));
        this.secondaryColor = ((ColorSetting) addSetting(new ColorSetting("SecondaryColor", "Secondary accent color", -4160260)));
        this.textColor = ((ColorSetting) addSetting(new ColorSetting("TextColor", "Client text color", -986891)));
        this.textColorMode = ((EnumSetting) addSetting(new EnumSetting("TextColorMode", "Text color mode", ClientSettings.TextColorMode.STATIC)));
        this.moduleListPos = ((EnumSetting) addSetting(new EnumSetting("ModuleList", "Module list position", ClientSettings.HudCorner.TOP_RIGHT)));
        this.watermarkPos = ((EnumSetting) addSetting(new EnumSetting("Watermark", "Watermark position", ClientSettings.WatermarkPos.TOP_LEFT)));
        this.showInfoBar = ((BoolSetting) addSetting(new BoolSetting("InfoBar", "Show FPS/Ping bar", true)));
        this.showCoords = ((BoolSetting) addSetting(new BoolSetting("Coordinates", "Show coordinate bar", true)));
        this.showNotifs = ((BoolSetting) addSetting(new BoolSetting("Notifications", "Show module toggle notifications", true)));
        this.topInfoOffsetX = ((IntSetting) addSetting(new IntSetting("TopInfoOffsetX", "Top info horizontal offset", 0, -4000, 4000, 1)));
        this.topInfoOffsetY = ((IntSetting) addSetting(new IntSetting("TopInfoOffsetY", "Top info vertical offset", 0, -4000, 4000, 1)));
        this.coordsOffsetX = ((IntSetting) addSetting(new IntSetting("CoordsOffsetX", "Coordinates HUD horizontal offset", 0, -4000, 4000, 1)));
        this.coordsOffsetY = ((IntSetting) addSetting(new IntSetting("CoordsOffsetY", "Coordinates HUD vertical offset", 0, -4000, 4000, 1)));
        this.moduleListOffsetX = ((IntSetting) addSetting(new IntSetting("ModuleListOffsetX", "Module list horizontal offset", 0, -4000, 4000, 1)));
        this.moduleListOffsetY = ((IntSetting) addSetting(new IntSetting("ModuleListOffsetY", "Module list vertical offset", 0, -4000, 4000, 1)));
        this.hotkeysOffsetX = ((IntSetting) addSetting(new IntSetting("HotkeysOffsetX", "Hotkeys panel horizontal offset", 0, -4000, 4000, 1)));
        this.hotkeysOffsetY = ((IntSetting) addSetting(new IntSetting("HotkeysOffsetY", "Hotkeys panel vertical offset", 0, -4000, 4000, 1)));
        this.hotkeysScale = ((IntSetting) addSetting(new IntSetting("HotkeysScale", "Hotkeys panel scale", 10, 6, 20, 1)));
        this.hudEditor = ((ButtonSetting) addSetting(new ButtonSetting("HudEditor", "Open chat HUD-move mode", () -> {
    class_310 mc = class_310.method_1551();
    if (mc != null) {
        mc.method_1507(new class_408("", false));
    }
})));
        this.clickGuiKey = ((KeybindSetting) addSetting(new KeybindSetting("ClickGuiKey", "Key to open ClickGUI", 344)));
        this.discordRPC = ((BoolSetting) addSetting(new BoolSetting("DiscordRPC", "Show Discord Rich Presence", false)));
        this.rpcDetail = ((StringSetting) addSetting(new StringSetting("RPCDetail", "Discord status detail", "Playing Vie Cheats Client")));
        this.wasRpcEnabled = false;
        this.lastRpcConnectAttemptAtMs = 0L;
        this.topInfoOffsetX.setVisibleInGui(false);
        this.topInfoOffsetY.setVisibleInGui(false);
        this.coordsOffsetX.setVisibleInGui(false);
        this.coordsOffsetY.setVisibleInGui(false);
        this.moduleListOffsetX.setVisibleInGui(false);
        this.moduleListOffsetY.setVisibleInGui(false);
        this.hotkeysOffsetX.setVisibleInGui(false);
        this.hotkeysOffsetY.setVisibleInGui(false);
    }

    public void onEnable() {
        super.onEnable();
        this.wasRpcEnabled = ((Boolean) this.discordRPC.get()).booleanValue();
        this.lastRpcConnectAttemptAtMs = 0L;
    }

    public void onDisable() {
        super.onDisable();
        stopDiscord();
        this.wasRpcEnabled = false;
        this.lastRpcConnectAttemptAtMs = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        boolean rpcOn = ((Boolean) this.discordRPC.get()).booleanValue();
        if (!(!rpcOn) && !this.wasRpcEnabled) {
            startDiscord();
            break label_6;
        }
        if ((rpcOn) || (!this.wasRpcEnabled)) break label_6;
        stopDiscord();
        label_6:
        this.wasRpcEnabled = rpcOn;
        if (!rpcOn) return;
        if (((mc == null) || (mc.field_1724 == null)) || (mc.field_1687 == null)) {
            return;
        }
        if (this.discord != null) {
            if (!(this.discord.isConnected())) {
            }
            long now = System.currentTimeMillis();
            if ((now - this.lastRpcConnectAttemptAtMs) >= 10000L) {
                startDiscord();
            }
            return;
        }
        this.discord.tick(((String) this.rpcDetail.get()));
    }

    private void startDiscord() {
        if (this.discord == null) {
            this.discord = new DiscordPresence();
        }
        this.lastRpcConnectAttemptAtMs = System.currentTimeMillis();
        if (this.discord.connect()) {
            this.discord.tick(((String) this.rpcDetail.get()));
        }
    }

    private void stopDiscord() {
        if (this.discord != null) {
            this.discord.disconnect();
        }
    }

    public int resolveTextColor(long rgbOffset) {
        int baseTextColor = getTextColor();
        if (this.textColorMode.get() == ClientSettings.TextColorMode.RGB) {
            int alpha = ((baseTextColor >>> 24) & 255);
            if (alpha <= 0) {
                alpha = 255;
            }
            return RainbowUtil.rainbow(1.0, 1.0, rgbOffset, alpha);
        }
        return baseTextColor;
    }

    public boolean isRgbTextMode() {
        return this.textColorMode.get() == ClientSettings.TextColorMode.RGB;
    }

    public ClientSettings.Theme getTheme() {
        return ((ClientSettings.Theme) this.theme.get());
    }

    public int getPrimaryColor() {
        return resolveThemePrimaryColor();
    }

    public int getSecondaryColor() {
        return resolveThemeSecondaryColor();
    }

    public int getTextColor() {
        return resolveThemeTextColor();
    }

    public ClientSettings.TextColorMode getTextColorMode() {
        return ((ClientSettings.TextColorMode) this.textColorMode.get());
    }

    public ClientSettings.HudCorner getModuleListPos() {
        return ((ClientSettings.HudCorner) this.moduleListPos.get());
    }

    public ClientSettings.WatermarkPos getWatermarkPos() {
        return ((ClientSettings.WatermarkPos) this.watermarkPos.get());
    }

    public double getGuiScale() {
        return 2.0;
    }

    public boolean showInfoBar() {
        return ((Boolean) this.showInfoBar.get()).booleanValue();
    }

    public boolean showCoords() {
        return ((Boolean) this.showCoords.get()).booleanValue();
    }

    public boolean showNotifs() {
        return ((Boolean) this.showNotifs.get()).booleanValue();
    }

    public int getTopInfoOffsetX() {
        return ((Integer) this.topInfoOffsetX.get()).intValue();
    }

    public int getTopInfoOffsetY() {
        return ((Integer) this.topInfoOffsetY.get()).intValue();
    }

    public int getCoordsOffsetX() {
        return ((Integer) this.coordsOffsetX.get()).intValue();
    }

    public int getCoordsOffsetY() {
        return ((Integer) this.coordsOffsetY.get()).intValue();
    }

    public int getModuleListOffsetX() {
        return ((Integer) this.moduleListOffsetX.get()).intValue();
    }

    public int getModuleListOffsetY() {
        return ((Integer) this.moduleListOffsetY.get()).intValue();
    }

    public int getHotkeysOffsetX() {
        return ((Integer) this.hotkeysOffsetX.get()).intValue();
    }

    public int getHotkeysOffsetY() {
        return ((Integer) this.hotkeysOffsetY.get()).intValue();
    }

    public float getHotkeysScale() {
        return (((float) ((Integer) this.hotkeysScale.get()).intValue()) / 10.0f);
    }

    public void setTopInfoOffsetX(int value) {
        this.topInfoOffsetX.set(value);
    }

    public void setTopInfoOffsetY(int value) {
        this.topInfoOffsetY.set(value);
    }

    public void setCoordsOffsetX(int value) {
        this.coordsOffsetX.set(value);
    }

    public void setCoordsOffsetY(int value) {
        this.coordsOffsetY.set(value);
    }

    public void setModuleListOffsetX(int value) {
        this.moduleListOffsetX.set(value);
    }

    public void setModuleListOffsetY(int value) {
        this.moduleListOffsetY.set(value);
    }

    public void setHotkeysOffsetX(int value) {
        this.hotkeysOffsetX.set(value);
    }

    public void setHotkeysOffsetY(int value) {
        this.hotkeysOffsetY.set(value);
    }

    public int getClickGuiKey() {
        return ((Integer) this.clickGuiKey.get()).intValue();
    }

    public int getAccentColor() {
        return getPrimaryColor();
    }

    public double getScale() {
        return 1.0;
    }

    public static ClientSettings get() {
        return ((ClientSettings) VieCheatsMod.getInstance().getModuleManager().getModule(ClientSettings.class));
    }

    private int resolveThemePrimaryColor() {
        switch (((ClientSettings.Theme) this.theme.get()).ordinal()) {
            case 0 -> { break label_6; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_7;
        label_3:
        break label_7;
        label_4:
        break label_7;
        label_5:
        break label_7;
        label_6:
        label_7:
        return ((Integer) this.primaryColor.get()).intValue();
    }

    private int resolveThemeSecondaryColor() {
        switch (((ClientSettings.Theme) this.theme.get()).ordinal()) {
            case 0 -> { break label_6; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_7;
        label_3:
        break label_7;
        label_4:
        break label_7;
        label_5:
        break label_7;
        label_6:
        label_7:
        return ((Integer) this.secondaryColor.get()).intValue();
    }

    private int resolveThemeTextColor() {
        switch (((ClientSettings.Theme) this.theme.get()).ordinal()) {
            case 0 -> { break label_6; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_7;
        label_3:
        break label_7;
        label_4:
        break label_7;
        label_5:
        break label_7;
        label_6:
        label_7:
        return ((Integer) this.textColor.get()).intValue();
    }


    public enum TextColorMode {
        STATIC,
        RGB;


        private TextColorMode() {
        }

    }

    public enum HudCorner {
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT;


        private HudCorner() {
        }

    }

    public enum WatermarkPos {
        TOP_LEFT,
        TOP_RIGHT,
        HIDDEN;


        private WatermarkPos() {
        }

    }

    public enum Theme {
        CUSTOM,
        PINK,
        OCEAN,
        AMETHYST,
        CRIMSON;


        private Theme() {
        }

    }
}

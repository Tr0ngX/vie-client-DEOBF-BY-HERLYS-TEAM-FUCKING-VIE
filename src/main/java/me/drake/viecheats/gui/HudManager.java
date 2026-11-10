package me.drake.viecheats.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.modules.client.HUD;
import me.drake.viecheats.modules.donut.RegionMap;
import me.drake.viecheats.modules.render.SpotifyHUD;
import me.drake.viecheats.render.ExternalGlTextRenderer;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.util.ClickGuiTheme;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_640;
import org.lwjgl.glfw.GLFW;

public class HudManager {
    private static final boolean ENABLE_NANOVG_HUD = true;
    private static final long TOP_INFO_REFRESH_MS = 220L;
    private static final long MODULE_CACHE_REFRESH_MS = 250L;
    private static final long KEYBIND_CACHE_REFRESH_MS = 250L;
    private static final long NOTIF_LIFE_MS = 2600L;
    private static final long ACH_LIFE_MS = 3600L;
    private static final int MAX_NOTIFICATIONS = 5;
    private static final int MAX_ACHIEVEMENTS = 3;
    private static final String WATERMARK_BASE = "Vie";
    private static final String WATERMARK_PLUS = "+";
    private static final int WATERMARK_PLUS_GAP = 1;
    private static final int DEF_HUD_BG = -15657182;
    private static final int DEF_HUD_BORDER = -13880252;
    private static final int DEF_HUD_ACCENT = -10897153;
    private static final int DEF_TEXT = -657153;
    private static final int DEF_TEXT_DIM = -4734767;
    private static final int DEF_TEXT_ACCENT = -7548673;
    private static int HUD_BG;
    private static int HUD_BORDER;
    private static int HUD_ACCENT;
    private static int DEFAULT_TEXT;
    private static int DEFAULT_TEXT_DIM;
    private static int DEFAULT_TEXT_ACCENT;
    private final List notifications;
    private final List achievements;
    private final Map moduleLabelCache;
    private final List moduleOrderByWidth;
    private final List keybindCache;
    private long moduleLabelLastRefreshMs;
    private long keybindLastRefreshMs;
    private long topInfoLastRefreshMs;
    private String topInfoFpsText;
    private String topInfoPingText;
    private int topInfoPingColor;
    private String topInfoTicksText;
    private String topInfoBpsText;
    private long lastBpsSampleAtMs;
    private double lastBpsX;
    private double lastBpsZ;
    private HudManager.HudBounds topInfoBounds;
    private HudManager.HudBounds coordsBounds;
    private HudManager.HudBounds moduleListBounds;
    private HudManager.HudBounds hotkeysBounds;
    private HudManager.HudDragTarget activeDragTarget;
    private boolean chatDragMouseDown;
    private int dragMouseOffsetX;
    private int dragMouseOffsetY;
    private int dragStartMouseX;
    private int dragStartMouseY;
    private int dragStartValueX;
    private int dragStartValueY;
    private int dragTargetW;
    private int dragTargetH;

    public HudManager() {
        this.notifications = new ArrayList();
        this.achievements = new ArrayList();
        this.moduleLabelCache = new HashMap();
        this.moduleOrderByWidth = new ArrayList();
        this.keybindCache = new ArrayList();
        this.moduleLabelLastRefreshMs = 0L;
        this.keybindLastRefreshMs = 0L;
        this.topInfoLastRefreshMs = 0L;
        this.topInfoFpsText = "0 FPS";
        this.topInfoPingText = "N/A";
        this.topInfoPingColor = -8355696;
        this.topInfoTicksText = "20.0 TICKS";
        this.topInfoBpsText = "0.0 BPS";
        this.lastBpsSampleAtMs = 0L;
        this.lastBpsX = 0.0;
        this.lastBpsZ = 0.0;
        this.topInfoBounds = HudManager.HudBounds.hidden();
        this.coordsBounds = HudManager.HudBounds.hidden();
        this.moduleListBounds = HudManager.HudBounds.hidden();
        this.hotkeysBounds = HudManager.HudBounds.hidden();
        this.activeDragTarget = HudManager.HudDragTarget.NONE;
        this.chatDragMouseDown = false;
    }

    public void onHudRender(class_332 ctx, float tickDelta) {
        try {
            renderHud(ctx);
        } catch (Exception ex2) {
            Exception e = ex2;
            VieCheatsMod.LOGGER.error("[VieCheat] HUD render error", e);
        }
    }

    private void renderHud(class_332 ctx) {
        resetHudEditorBounds();
        class_310 mc = class_310.method_1551();
        if (((mc == null) || (mc.field_1724 == null)) || (mc.field_1687 == null)) {
            return;
        }
        class_327 tr = mc.field_1772;
        if (tr == null) return;
        int sw = mc.method_22683().method_4486();
        int sh = mc.method_22683().method_4502();
        if ((sw <= 0) || (sh <= 0)) {
            return;
        }
        ClientSettings cs = ClientSettings.get();
        refreshClientThemeColors(cs);
        if (!(isMainHudEnabled())) return;
        renderTopInfo(ctx, tr, mc, cs, sw);
        renderCoords(ctx, tr, mc, cs, sw, sh);
        renderModuleList(ctx, tr, cs, sw, sh);
        renderKeybindList(ctx, tr, cs, sw, sh);
        if (cs != null) {
            if (cs.showNotifs()) {
            }
            renderNotifications(ctx, tr, cs, sw, sh);
            renderAchievements(ctx, tr, cs, sw);
        }
        handleChatDragMode(ctx, tr, mc, cs, sw, sh);
    }

    public HudManager.HudBounds getTopInfoBounds() {
        return this.topInfoBounds;
    }

    public HudManager.HudBounds getCoordsBounds() {
        return this.coordsBounds;
    }

    public HudManager.HudBounds getModuleListBounds() {
        return this.moduleListBounds;
    }

    public HudManager.HudBounds getHotkeysBounds() {
        return this.hotkeysBounds;
    }

    private void resetHudEditorBounds() {
        this.topInfoBounds = HudManager.HudBounds.hidden();
        this.coordsBounds = HudManager.HudBounds.hidden();
        this.moduleListBounds = HudManager.HudBounds.hidden();
        this.hotkeysBounds = HudManager.HudBounds.hidden();
    }

    private void handleChatDragMode(class_332 ctx, class_327 tr, class_310 mc, ClientSettings cs, int sw, int sh) {
        boolean chatOpen = (mc.field_1755 instanceof class_408);
        if (!chatOpen) {
            if (this.activeDragTarget != HudManager.HudDragTarget.NONE) {
                this.activeDragTarget = HudManager.HudDragTarget.NONE;
                requestSaveConfig();
            }
            this.chatDragMouseDown = false;
            return;
        }
        List targets = buildDragTargets();
        if (targets.isEmpty()) {
            this.activeDragTarget = HudManager.HudDragTarget.NONE;
            this.chatDragMouseDown = false;
            return;
        }
        int mx = scaledMouseX(mc, sw);
        int my = scaledMouseY(mc, sh);
        HudManager.DragTargetRect hovered = findHoveredTarget(targets, mx, my);
        boolean leftDown = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 0) == 1;
        if (!(!leftDown) && !this.chatDragMouseDown && hovered != null) {
            beginDrag(hovered, mx, my, cs);
        }
        if ((!leftDown) || (this.activeDragTarget == HudManager.HudDragTarget.NONE)) break label_26;
        HudManager.DragTargetRect active = findByType(targets, this.activeDragTarget);
        if (active == null) break label_26;
        this.dragTargetW = active.w();
        this.dragTargetH = active.h();
        applyDrag(active, mx, my, sw, sh, cs);
        label_26:
        if (!leftDown && !(!this.chatDragMouseDown) && this.activeDragTarget != HudManager.HudDragTarget.NONE) {
            this.activeDragTarget = HudManager.HudDragTarget.NONE;
            requestSaveConfig();
        }
        this.chatDragMouseDown = leftDown;
        drawDragOverlay(ctx, tr, targets, hovered);
    }

    private List buildDragTargets() {
        List targets = new ArrayList(6);
        addBoundsTarget(targets, HudManager.HudDragTarget.TOP_INFO, "Top Info", this.topInfoBounds, false);
        addBoundsTarget(targets, HudManager.HudDragTarget.COORDS, "Coordinates", this.coordsBounds, false);
        addBoundsTarget(targets, HudManager.HudDragTarget.MODULE_LIST, "Functions (Grouped)", this.moduleListBounds, false);
        addBoundsTarget(targets, HudManager.HudDragTarget.HOTKEYS, "Hotkeys", this.hotkeysBounds, false);
        addSpotifyTarget(targets);
        addRegionMapTarget(targets);
        return targets;
    }

    private static void addBoundsTarget(List targets, HudManager.HudDragTarget target, String label, HudManager.HudBounds bounds, boolean absolute) {
        if ((bounds == null) || (!(bounds.visible()))) {
            return;
        }
        targets.add(new HudManager.DragTargetRect(target, label, bounds.x(), bounds.y(), bounds.width(), bounds.height(), absolute));
    }

    private void addSpotifyTarget(List targets) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        SpotifyHUD hud = ((SpotifyHUD) instance.getModuleManager().getModule(SpotifyHUD.class));
        if ((hud == null) || (!(hud.isEnabled()))) {
            return;
        }
        float sc = hud.getScale();
        int artSize = ((int) (54.0f * sc));
        int pad = ((int) (10.0f * sc));
        int gap = ((int) (12.0f * sc));
        int lineH = ((int) (9.0f * sc));
        int barH = ((int) (4.0f * sc));
        int timeGap = ((int) (5.0f * sc));
        int btnSize = Math.max(7, Math.min(10, Math.round((7.2f * sc))));
        int titleYoffset = pad;
        int artistYoffset = (titleYoffset + lineH);
        int barYoffset = (((artistYoffset + lineH) + timeGap) + ((int) (8.0f * sc)));
        int timeYoffset = ((barYoffset + barH) + timeGap);
        int btnYoffset = ((timeYoffset + lineH) + ((int) (8.0f * sc)));
        int contentBottom = (btnYoffset + (btnSize / 2));
        int panelH = (contentBottom + pad);
        int panelW = (((artSize + gap) + ((int) (175.0f * sc))) + (pad * 2));
        targets.add(new HudManager.DragTargetRect(HudManager.HudDragTarget.SPOTIFY, "Spotify HUD", hud.getPosX(), hud.getPosY(), panelW, panelH, true));
    }

    private void addRegionMapTarget(List targets) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        RegionMap map = ((RegionMap) instance.getModuleManager().getModule(RegionMap.class));
        if ((map == null) || (!(map.isEnabled()))) {
            return;
        }
        targets.add(new HudManager.DragTargetRect(HudManager.HudDragTarget.REGION_MAP, "Region Map", map.getPosX(), map.getPosY(), map.getHudWidth(), map.getHudHeight(), true));
    }

    private static int scaledMouseX(class_310 mc, int scaledW) {
        double raw = mc.field_1729.method_1603();
        double nativeW = Math.max(1.0, ((double) mc.method_22683().method_4480()));
        return ((int) Math.round(((raw * ((double) scaledW)) / nativeW)));
    }

    private static int scaledMouseY(class_310 mc, int scaledH) {
        double raw = mc.field_1729.method_1604();
        double nativeH = Math.max(1.0, ((double) mc.method_22683().method_4507()));
        return ((int) Math.round(((raw * ((double) scaledH)) / nativeH)));
    }

    private static HudManager.DragTargetRect findHoveredTarget(List targets, int mx, int my) {
        int i = (targets.size() - 1);
        while (i >= 0) {
            HudManager.DragTargetRect rect = ((HudManager.DragTargetRect) targets.get(i));
            if (rect.contains(mx, my)) return rect;
            i--;
        }
        return null;
    }

    private static HudManager.DragTargetRect findByType(List targets, HudManager.HudDragTarget target) {
        Object var2 = targets.iterator();
        while (var2.hasNext()) {
            HudManager.DragTargetRect rect = ((HudManager.DragTargetRect) var2.next());
            if (rect.target() == target) return rect;
        }
        return null;
    }

    private void beginDrag(HudManager.DragTargetRect target, int mx, int my, ClientSettings cs) {
        this.activeDragTarget = target.target();
        this.dragTargetW = target.w();
        this.dragTargetH = target.h();
        if (target.absolute()) {
            this.dragMouseOffsetX = (mx - target.x());
            this.dragMouseOffsetY = (my - target.y());
        } else {
            this.dragStartMouseX = mx;
            this.dragStartMouseY = my;
            this.dragStartValueX = getOffsetX(cs, target.target());
            this.dragStartValueY = getOffsetY(cs, target.target());
        }
    }

    private void applyDrag(HudManager.DragTargetRect target, int mx, int my, int sw, int sh, ClientSettings cs) {
        if (target.absolute()) {
            int maxX = Math.max(0, (sw - Math.max(1, this.dragTargetW)));
            int maxY = Math.max(0, (sh - Math.max(1, this.dragTargetH)));
            int newX = clampInt((mx - this.dragMouseOffsetX), 0, maxX);
            int newY = clampInt((my - this.dragMouseOffsetY), 0, maxY);
            setAbsolutePos(target.target(), newX, newY);
            return;
        }
        int dx = (mx - this.dragStartMouseX);
        int dy = (my - this.dragStartMouseY);
        int offsetX = (this.dragStartValueX + dx);
        int offsetY = (this.dragStartValueY + dy);
        setOffsetPos(cs, target.target(), offsetX, offsetY);
    }

    private static int getOffsetX(ClientSettings cs, HudManager.HudDragTarget target) {
        if (cs == null) return 0;
        switch (target.ordinal()) {
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            default -> { break label_6; }
        }
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
        return 0;
    }

    private static int getOffsetY(ClientSettings cs, HudManager.HudDragTarget target) {
        if (cs == null) return 0;
        switch (target.ordinal()) {
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            default -> { break label_6; }
        }
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
        return 0;
    }

    private static void setOffsetPos(ClientSettings cs, HudManager.HudDragTarget target, int x, int y) {
        if (cs == null) return;
        switch (target.ordinal()) {
            case 3 -> { break label_2; }
            case 4 -> { break label_5; }
            case 5 -> { break label_8; }
            case 6 -> { break label_11; }
            default -> { break label_14; }
        }
        label_2:
        cs.setTopInfoOffsetX(x);
        cs.setTopInfoOffsetY(y);
        break label_14;
        label_5:
        cs.setCoordsOffsetX(x);
        cs.setCoordsOffsetY(y);
        break label_14;
        label_8:
        cs.setModuleListOffsetX(x);
        cs.setModuleListOffsetY(y);
        break label_14;
        label_11:
        cs.setHotkeysOffsetX(x);
        cs.setHotkeysOffsetY(y);
        break label_14;
        label_14:
    }

    private static void setAbsolutePos(HudManager.HudDragTarget target, int x, int y) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        switch (target.ordinal()) {
            case 1 -> { break label_4; }
            case 2 -> { break label_9; }
            default -> { break label_14; }
        }
        label_4:
        SpotifyHUD hud = ((SpotifyHUD) instance.getModuleManager().getModule(SpotifyHUD.class));
        if (hud != null) {
            hud.posX.set(x);
            hud.posY.set(y);
        }
        break label_14;
        label_9:
        RegionMap map = ((RegionMap) instance.getModuleManager().getModule(RegionMap.class));
        if (map != null) {
            map.setPosX(x);
            map.setPosY(y);
        }
        break label_14;
        label_14:
    }

    private void drawDragOverlay(class_332 ctx, class_327 tr, List targets, HudManager.DragTargetRect hovered) {
        String hint = "Chat HUD Move Mode: hold left mouse to drag HUD blocks";
        drawClientText(ctx, tr, null, hint, 6, 6, withAlpha(-1378049, 0.96f));
        Object var6 = targets.iterator();
        while (var6.hasNext()) {
            HudManager.DragTargetRect target = ((HudManager.DragTargetRect) var6.next());
            if (hovered == null) break label_5;
            boolean isHovered = hovered.target() == target.target();
            boolean isActive = this.activeDragTarget == target.target();
            if (!(!isActive)) {
                break label_13;
            }
            int stroke = (!(!isHovered) ? -11544065 : -7498080);
            if (!(!isActive)) {
                break label_17;
            }
            if (!(!isHovered)) {
                break label_17;
            }
            label_17:
            int fill = withAlpha(-11774624, 0.12f);
            TessellatorRenderer.fillRoundedRect(ctx, target.x(), target.y(), target.w(), target.h(), 5, fill);
            TessellatorRenderer.strokeRoundedRect(ctx, target.x(), target.y(), target.w(), target.h(), 5.0f, 1.25f, stroke);
            int labelY = Math.max(16, (target.y() - 10));
            String label = (target.label() + " [" + target.x() + ", " + target.y() + "]");
            drawClientText(ctx, tr, null, label, (target.x() + 2), labelY, stroke);
        }
    }

    private void requestSaveConfig() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance != null && instance.getConfigManager() != null) {
            instance.getConfigManager().requestSave();
        }
    }

    private static boolean isMainHudEnabled() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return true;
        }
        HUD hud = ((HUD) instance.getModuleManager().getModule(HUD.class));
        if (hud != null) {
            if (hud.isEnabled()) {
            }
        } else {
        }
        return false;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderTopInfo(class_332 ctx, class_327 tr, class_310 mc, ClientSettings cs, int sw) {
        if (cs == null) break label_1;
        boolean hideWatermark = cs.getWatermarkPos() == ClientSettings.WatermarkPos.HIDDEN;
        if (cs == null) break label_4;
        boolean right = cs.getWatermarkPos() == ClientSettings.WatermarkPos.TOP_RIGHT;
        refreshTopInfoCache(mc);
        if (cs != null && !(cs.showInfoBar()) && !(!hideWatermark)) {
            this.topInfoBounds = HudManager.HudBounds.hidden();
            return;
        }
        if (cs != null) {
        } else {
        }
        int offsetX = 0;
        if (cs != null) {
        } else {
        }
        int offsetY = 0;
        int baseY = (5 + offsetY);
        int pillH = 16;
        int gap = 3;
        String[] labels = new String[4];
        int[] widths = new int[4];
        int[] colors = new int[4];
        if (cs != null) {
            if (cs.showInfoBar()) {
            }
            labels[0] = this.topInfoFpsText;
            labels[1] = this.topInfoPingText;
            labels[2] = this.topInfoTicksText;
            labels[3] = this.topInfoBpsText;
            widths[0] = (textWidth(tr, cs, labels[0]) + 14);
            widths[1] = (textWidth(tr, cs, labels[1]) + 14);
            widths[2] = (textWidth(tr, cs, labels[2]) + 14);
            widths[3] = (textWidth(tr, cs, labels[3]) + 14);
            colors[0] = DEFAULT_TEXT;
            colors[1] = this.topInfoPingColor;
            colors[2] = DEFAULT_TEXT_DIM;
            colors[3] = DEFAULT_TEXT_DIM;
        }
        if (cs != null) {
            if (cs.showInfoBar()) {
            }
        } else {
        }
        int count = 0;
        boolean drawWatermark = !hideWatermark;
        int totalW = 0;
        if (!(!drawWatermark)) {
            totalW = (totalW + watermarkPillWidth(tr, cs));
        }
        if (count > 0) {
            int i = 0;
            while (i < count) {
                totalW = (totalW + widths[i]);
                i++;
            }
            totalW = (totalW + ((count - 1) * gap));
        }
        if (!(!drawWatermark) && count > 0) {
            totalW = (totalW + gap);
        }
        if (!(!right)) {
        } else {
        }
        int startX = (6 + offsetX);
        if (totalW <= 0) {
            this.topInfoBounds = HudManager.HudBounds.hidden();
            return;
        }
        if (!(!right)) {
        } else {
        }
        int leftX = startX;
        this.topInfoBounds = new HudManager.HudBounds(leftX, baseY, totalW, pillH);
        int fillX = startX;
        if (!(!right)) {
            if (count > 0) {
                int bpsPx = (fillX - widths[3]);
                drawSmallPill(ctx, tr, cs, labels[3], bpsPx, baseY, widths[3], pillH, colors[3]);
                fillX = (bpsPx - gap);
                int ticksPx = (fillX - widths[2]);
                drawSmallPill(ctx, tr, cs, labels[2], ticksPx, baseY, widths[2], pillH, colors[2]);
                fillX = (ticksPx - gap);
                int pingPx = (fillX - widths[1]);
                drawSmallPill(ctx, tr, cs, labels[1], pingPx, baseY, widths[1], pillH, colors[1]);
                fillX = (pingPx - gap);
                int fpsPx = (fillX - widths[0]);
                drawSmallPill(ctx, tr, cs, labels[0], fpsPx, baseY, widths[0], pillH, colors[0]);
                fillX = (fpsPx - gap);
            }
            if (!drawWatermark) break label_90;
            int wmW = watermarkPillWidth(tr, cs);
            int wmPx = (fillX - wmW);
            drawWatermarkPill(ctx, tr, cs, wmPx, baseY, wmW, pillH);
            break label_90;
        }
        if (!(!drawWatermark)) {
            wmW = watermarkPillWidth(tr, cs);
            drawWatermarkPill(ctx, tr, cs, fillX, baseY, wmW, pillH);
            fillX = (fillX + (wmW + gap));
        }
        if (count <= 0) break label_90;
        i = 0;
        while (i < count) {
            drawSmallPill(ctx, tr, cs, labels[i], fillX, baseY, widths[i], pillH, colors[i]);
            fillX = (fillX + widths[i]);
            if (i < (count - 1)) {
                fillX = (fillX + gap);
            }
            i++;
        }
        label_90:
    }

    private void renderCoords(class_332 ctx, class_327 tr, class_310 mc, ClientSettings cs, int sw, int sh) {
        if ((cs == null) || (cs.showCoords())) break label_3;
        this.coordsBounds = HudManager.HudBounds.hidden();
        return;
        label_3:
        if (cs == null) break label_4;
        boolean right = cs.getWatermarkPos() == ClientSettings.WatermarkPos.TOP_RIGHT;
        if (cs != null) {
        } else {
        }
        int offsetX = 0;
        if (cs != null) {
        } else {
        }
        int offsetY = 0;
        double px = mc.field_1724.method_23317();
        double py = mc.field_1724.method_23318();
        double pz = mc.field_1724.method_23321();
        String coordsText = (Math.round(px) + ", " + Math.round(py) + ", " + Math.round(pz));
        String serverIp = resolveServerIp(mc);
        int pillH = 13;
        int gap = 3;
        if (serverIp.isEmpty()) {
            int w = (textWidth(tr, cs, coordsText) + 12);
            if (!(!right)) {
            } else {
            }
            int bx = (6 + offsetX);
            int by = (22 + offsetY);
            drawSmallPill(ctx, tr, cs, coordsText, bx, by, w, pillH, DEFAULT_TEXT);
            this.coordsBounds = new HudManager.HudBounds(bx, by, w, pillH);
        } else {
            int coordsW = (textWidth(tr, cs, coordsText) + 12);
            int serverW = (textWidth(tr, cs, serverIp) + 12);
            by = (22 + offsetY);
            if (!(!right)) {
                int serverPx = ((sw - serverW) - 6);
                int coordsPx = ((serverPx - gap) - coordsW);
            } else {
                coordsPx = 6;
                serverPx = ((coordsPx + coordsW) + gap);
            }
            coordsPx = (coordsPx + offsetX);
            serverPx = (serverPx + offsetX);
            drawSmallPill(ctx, tr, cs, coordsText, coordsPx, by, coordsW, pillH, DEFAULT_TEXT);
            drawSmallPill(ctx, tr, cs, serverIp, serverPx, by, serverW, pillH, HUD_ACCENT);
            int minX = Math.min(coordsPx, serverPx);
            int maxX = Math.max((coordsPx + coordsW), (serverPx + serverW));
            this.coordsBounds = new HudManager.HudBounds(minX, by, Math.max(1, (maxX - minX)), pillH);
        }
    }

    private void drawSmallPill(class_332 ctx, class_327 tr, ClientSettings cs, String text, int x, int y, int w, int h, int textColor) {
        int bg = withAlpha(HUD_BG, 0.95f);
        int border = withAlpha(blendColor(HUD_BORDER, HUD_ACCENT, 0.18f), 0.88f);
        drawPillBackdrop(ctx, x, y, w, h, bg, border);
        drawClientText(ctx, tr, cs, text, (x + 7), (y + 4), resolveTextColor(cs, 0L, textColor));
    }

    private int watermarkPillWidth(class_327 tr, ClientSettings cs) {
        return (((textWidth(tr, cs, "Vie") + 1) + textWidth(tr, cs, "+")) + 16);
    }

    private void drawWatermarkPill(class_332 ctx, class_327 tr, ClientSettings cs, int x, int y, int w, int h) {
        int bg = withAlpha(HUD_BG, 0.96f);
        int border = withAlpha(blendColor(HUD_ACCENT, HUD_BORDER, 0.42f), 0.94f);
        drawPillBackdrop(ctx, x, y, w, h, bg, border);
        int textX = (x + 7);
        int textY = (y + 4);
        int baseColor = resolveTextColor(cs, 0L, DEFAULT_TEXT);
        drawClientText(ctx, tr, cs, "Vie", textX, textY, baseColor);
        int plusX = ((textX + textWidth(tr, cs, "Vie")) + 1);
        int plusColor = withAlpha(HUD_ACCENT, 1.0f);
        drawGlowText(ctx, tr, cs, "+", plusX, textY, plusColor);
    }

    private static void drawGlowText(class_332 ctx, class_327 tr, ClientSettings cs, String text, int x, int y, int color) {
        int glowRgb = blendColor(color, -1, 0.18f);
        if (tr == null) break label_2;
        Objects.requireNonNull(tr);
        break label_3;
        label_2:
        label_3:
        float plusSize = Math.max(9, (((float) 9) * 1.02f));
        int radius = 3;
        float sigma = 1.42f;
        float invTwoSigmaSq = 0.24796669f;
        int dy = -3;
        while (dy <= 3) {
            int dx = -3;
            while (dx <= 3) {
                if (dx == 0 && dy == 0) {
                    break label_19;
                }
                float distSq = ((float) ((dx * dx) + (dy * dy)));
                if (distSq > 9.0f) {
                    break label_19;
                }
                float weight = ((float) Math.exp(((double) (-(distSq) * 0.24796669f))));
                float alpha = (0.018f * weight);
                if (alpha <= 0.002f) {
                    break label_19;
                }
                int sampleColor = withAlpha(glowRgb, alpha);
                ExternalGlTextRenderer.drawText(ctx, text, (x + dx), (y + dy), sampleColor, false, plusSize);
                label_19:
                dx++;
            }
            dy++;
        }
        int innerAura = withAlpha(glowRgb, 0.12f);
        ExternalGlTextRenderer.drawText(ctx, text, x, y, innerAura, false, plusSize);
        int core = withAlpha(color, 1.0f);
        ExternalGlTextRenderer.drawText(ctx, text, x, y, core, false, plusSize);
    }

    private static void drawPillBackdrop(class_332 ctx, int x, int y, int w, int h, int bgColor, int borderColor) {
        if (((ctx == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        ClickGUI.rrPill(ctx, x, y, w, h, bgColor);
        float radius = Math.max(2.0f, (((float) h) * 0.5f));
        TessellatorRenderer.strokeRoundedRect(ctx, (((float) x) + 0.5f), (((float) y) + 0.5f), (((float) w) - 1.0f), (((float) h) - 1.0f), radius, 1.0f, borderColor);
    }

    private static void drawSmallHudBackdrop(class_332 ctx, int x, int y, int w, int h, int bgColor, int borderColor) {
        if (((ctx == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        ClickGUI.rrSmall(ctx, x, y, w, h, bgColor);
        float radius = Math.max(1.5f, Math.min(3.0f, (((float) Math.min(w, h)) * 0.5f)));
        TessellatorRenderer.strokeRoundedRect(ctx, (((float) x) + 0.5f), (((float) y) + 0.5f), (((float) w) - 1.0f), (((float) h) - 1.0f), radius, 1.0f, borderColor);
    }

    private static String resolveServerIp(class_310 mc) {
        if ((mc == null) || (mc.method_1558() == null)) break label_1;
        if (mc.method_1558().field_3761 == null) {
            label_1:
            return "";
        }
        String value = mc.method_1558().field_3761.trim();
        if (value.isEmpty()) return "";
        if (value.startsWith("http://")) {
            value = value.substring(7);
        }
        if (value.startsWith("https://")) {
            value = value.substring(8);
        }
        int slash = value.indexOf(47);
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        int space = value.indexOf(32);
        if (space >= 0) {
            value = value.substring(0, space);
        }
        if (value.endsWith(":25565")) {
            value = value.substring(0, (value.length() - 6));
        }
        return value.trim();
    }

    private void renderModuleList(class_332 ctx, class_327 tr, ClientSettings cs, int sw, int sh) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) break label_2;
        if (instance.getModuleManager() != null) break label_4;
        label_2:
        this.moduleListBounds = HudManager.HudBounds.hidden();
        return;
        label_4:
        List all = instance.getModuleManager().getModules();
        if (all.isEmpty()) {
            this.moduleListBounds = HudManager.HudBounds.hidden();
            return;
        }
        long now = System.currentTimeMillis();
        refreshModuleLabelCacheIfNeeded(all, tr, cs, now);
        List visible = new ArrayList();
        ClientSettings.HudCorner corner = this.moduleOrderByWidth.iterator();
        while (corner.hasNext()) {
            Module mod = ((Module) corner.next());
            if (!(mod.isEnabled())) {
                break label_12;
            }
            if (mod.getCategory() == Category.CLIENT) {
                break label_12;
            }
            HudManager.ModuleLabelCache cache = ((HudManager.ModuleLabelCache) this.moduleLabelCache.get(mod));
            if (cache != null) {
                visible.add(cache);
            }
        }
        if (visible.isEmpty()) {
            this.moduleListBounds = HudManager.HudBounds.hidden();
            return;
        }
        if (cs != null) {
        } else {
        }
        corner = ClientSettings.HudCorner.TOP_RIGHT;
        if (corner != ClientSettings.HudCorner.TOP_RIGHT) {
            if (corner == ClientSettings.HudCorner.BOTTOM_RIGHT) {
            }
        } else {
        }
        boolean right = false;
        if (corner != ClientSettings.HudCorner.BOTTOM_LEFT) {
            if (corner == ClientSettings.HudCorner.BOTTOM_RIGHT) {
            }
        } else {
        }
        boolean bottom = false;
        if (cs != null) {
        } else {
        }
        int offsetX = 0;
        if (cs != null) {
        } else {
        }
        int offsetY = 0;
        int rowStep = 13;
        if (!(!bottom)) {
        } else {
        }
        int y = (28 + offsetY);
        int textColor = resolveTextColor(cs, 6L, DEFAULT_TEXT);
        int firstY = y;
        int minX = 2147483647;
        int maxX = -2147483648;
        int height = visible.iterator();
        while (height.hasNext()) {
            HudManager.ModuleLabelCache item = ((HudManager.ModuleLabelCache) height.next());
            String label = item.label();
            int tw = (textWidth(tr, cs, label) + 12);
            if (!(!right)) {
            } else {
            }
            int x = (6 + offsetX);
            drawModulePill(ctx, tr, cs, label, x, y, tw, right, textColor);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, (x + tw));
            y = (y + rowStep);
        }
        height = Math.max(11, (((y - firstY) - rowStep) + 11));
        if (minX != 2147483647) {
            if (maxX <= minX) {
            }
            this.moduleListBounds = HudManager.HudBounds.hidden();
        } else {
            this.moduleListBounds = new HudManager.HudBounds(minX, firstY, (maxX - minX), height);
        }
    }

    private void renderKeybindList(class_332 ctx, class_327 tr, ClientSettings cs, int sw, int sh) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) break label_2;
        if (instance.getModuleManager() != null) break label_4;
        label_2:
        this.hotkeysBounds = HudManager.HudBounds.hidden();
        return;
        label_4:
        List all = instance.getModuleManager().getModules();
        if (!(all.isEmpty())) break label_8;
        this.hotkeysBounds = HudManager.HudBounds.hidden();
        return;
        label_8:
        long now = System.currentTimeMillis();
        refreshKeybindCacheIfNeeded(all, tr, cs, now);
        if (this.keybindCache.isEmpty()) {
            this.hotkeysBounds = HudManager.HudBounds.hidden();
            return;
        }
        int moduleBottomLeftTop = getBottomLeftModuleListTop(all, cs, sh);
        if (moduleBottomLeftTop > 0) {
        } else {
        }
        int bottomLimit = (sh - 6);
        if (cs != null) {
        } else {
        }
        float hotkeysScale = 1.0f;
        hotkeysScale = (clamp01((hotkeysScale / 2.0f)) * 2.0f);
        if (hotkeysScale < 0.6f) {
            hotkeysScale = 0.6f;
        }
        int headerH = scaleUi(13, hotkeysScale);
        int rowStep = scaleUi(14, hotkeysScale);
        int panelPad = scaleUi(8, hotkeysScale);
        int minTop = scaleUi(6, hotkeysScale);
        int availableH = (bottomLimit - minTop);
        if (availableH <= (headerH + 10)) {
            this.hotkeysBounds = HudManager.HudBounds.hidden();
            return;
        }
        int maxRows = Math.max(1, ((availableH - (headerH + scaleUi(6, hotkeysScale))) / rowStep));
        boolean truncated = this.keybindCache.size() > maxRows;
        if (!(!truncated)) {
        } else {
        }
        int rows = this.keybindCache.size();
        int drawnRows = (rows + (!(!truncated) ? 1 : 0));
        String title = "HOTKEYS";
        int contentW = textWidthScaled(tr, title, hotkeysScale);
        int i = 0;
        while (i < rows) {
            contentW = Math.max(contentW, textWidthScaled(tr, ((HudManager.KeybindHudEntry) this.keybindCache.get(i)).label(), hotkeysScale));
            i++;
        }
        String moreText = null;
        if (!(!truncated)) {
            int hidden = Math.max(0, (this.keybindCache.size() - rows));
            moreText = ("+" + hidden + " MORE");
            contentW = Math.max(contentW, textWidthScaled(tr, moreText, hotkeysScale));
        }
        int desiredW = ((contentW + (panelPad * 2)) + scaleUi(14, hotkeysScale));
        int panelMinW = scaleUi(170, hotkeysScale);
        int panelMaxW = Math.max(scaleUi(120, hotkeysScale), (sw - 12));
        int panelW = Math.max(panelMinW, Math.min(panelMaxW, desiredW));
        int panelH = (((headerH + scaleUi(4, hotkeysScale)) + (drawnRows * rowStep)) + scaleUi(4, hotkeysScale));
        if (cs != null) {
        } else {
        }
        int offsetX = 0;
        if (cs != null) {
        } else {
        }
        int offsetY = 0;
        int x = (6 + offsetX);
        int y = (((bottomLimit - panelH) - scaleUi(24, hotkeysScale)) + offsetY);
        if (y < minTop) {
            y = minTop;
        }
        int panelBg = withAlpha(HUD_BG, 0.93f);
        int panelBorder = withAlpha(blendColor(HUD_BORDER, HUD_ACCENT, 0.24f), 0.9f);
        ClickGUI.rr(ctx, x, y, panelW, panelH, panelBg);
        float panelRadius = Math.max(3.0f, Math.min(4.0f, (((float) Math.min(panelW, panelH)) * 0.5f)));
        TessellatorRenderer.strokeRoundedRect(ctx, (((float) x) + 0.5f), (((float) y) + 0.5f), (((float) panelW) - 1.0f), (((float) panelH) - 1.0f), panelRadius, 1.0f, panelBorder);
        drawClientTextScaled(ctx, tr, title, (x + panelPad), (y + scaleUi(4, hotkeysScale)), resolveTextColor(cs, 14L, DEFAULT_TEXT_ACCENT), hotkeysScale);
        fillRect(ctx, (x + panelPad), (y + headerH), ((x + panelW) - panelPad), ((y + headerH) + 1), withAlpha(HUD_ACCENT, 0.58f));
        int rowW = (panelW - scaleUi(8, hotkeysScale));
        int rowY = ((y + headerH) + scaleUi(3, hotkeysScale));
        i = 0;
        while (i < rows) {
            HudManager.KeybindHudEntry entry = ((HudManager.KeybindHudEntry) this.keybindCache.get(i));
            String rowLabel = trimToWidthScaled(tr, entry.label(), (rowW - scaleUi(12, hotkeysScale)), hotkeysScale);
            drawScaledChip(ctx, tr, rowLabel, (x + scaleUi(4, hotkeysScale)), rowY, rowW, resolveTextColor(cs, (15L + ((long) i)), DEFAULT_TEXT), hotkeysScale);
            rowY = (rowY + rowStep);
            i++;
        }
        if (moreText != null) {
            drawScaledChip(ctx, tr, trimToWidthScaled(tr, moreText, (rowW - scaleUi(12, hotkeysScale)), hotkeysScale), (x + scaleUi(4, hotkeysScale)), rowY, rowW, resolveTextColor(cs, 99L, DEFAULT_TEXT_DIM), hotkeysScale);
        }
        this.hotkeysBounds = new HudManager.HudBounds(x, y, panelW, panelH);
    }

    private int getBottomLeftModuleListTop(List all, ClientSettings cs, int sh) {
        if (cs == null) break label_1;
        break label_2;
        label_1:
        label_2:
        ClientSettings.HudCorner corner = ClientSettings.HudCorner.TOP_RIGHT;
        if (corner != ClientSettings.HudCorner.BOTTOM_LEFT) return -1;
        int visibleCount = 0;
        int baseTop = all.iterator();
        while (baseTop.hasNext()) {
            Module mod = ((Module) baseTop.next());
            if (!(mod.isEnabled())) {
                break label_6;
            }
            if (mod.getCategory() == Category.CLIENT) {
                break label_6;
            }
            visibleCount++;
        }
        if (visibleCount <= 0) return -1;
        baseTop = ((sh - 28) - (visibleCount * 13));
        if (cs != null) {
        } else {
        }
        int offsetY = 0;
        return (baseTop + offsetY);
    }

    private void refreshKeybindCacheIfNeeded(List all, class_327 tr, ClientSettings cs, long now) {
        if ((now - this.keybindLastRefreshMs) < 250L) return;
        this.keybindLastRefreshMs = now;
        this.keybindCache.clear();
        Object var6 = all.iterator();
        while (var6.hasNext()) {
            Module mod = ((Module) var6.next());
            if ((mod == null) || (mod.getKeybind() == null)) break label_4;
            if (!(mod.getKeybind().isBound())) {
                break label_4;
            }
            String keyName = compactKeyName(mod.getKeybind().getKeyName());
            if (keyName.isEmpty()) break label_4;
            if (!(!"NONE".equals(keyName))) {
                break label_4;
            }
            String moduleName = mod.getName().toUpperCase(Locale.ROOT);
            String label = (keyName + " - " + moduleName);
            this.keybindCache.add(new HudManager.KeybindHudEntry(keyName, moduleName, label, textWidth(tr, cs, label)));
        }
        this.keybindCache.sort((a, b) -> {
    int cmp = a.key().compareToIgnoreCase(b.key());
    if (cmp != 0) return cmp;
    return a.module().compareToIgnoreCase(b.module());
});
    }

    private static String compactKeyName(String raw) {
        if (raw == null) break label_1;
        if (!(raw.isBlank())) break label_2;
        label_1:
        return "";
        label_2:
        String key = raw.trim().toUpperCase(Locale.ROOT);
        if (key.startsWith("MB")) {
            int end = 2;
            while (end < key.length() && Character.isDigit(key.charAt(end))) {
                end++;
            }
            if (end > 2) return ("M" + key.substring(2, end));
            int space = key.indexOf(32);
            if (space > 0) {
            } else {
            }
            String core = key;
            return core.replace("MB", "M");
        }
        if (!(key.startsWith("KEY_"))) {
            return key;
            try {
            }
            int code = Integer.parseInt(key.substring(4));
        } catch (NumberFormatException ex17) {
            NumberFormatException ignored = ex17;
            return key;
        }
        switch (code) {
            case 32 -> { break label_20; }
            case 256 -> { break label_24; }
            case 257 -> { break label_21; }
            case 258 -> { break label_23; }
            case 259 -> { break label_22; }
            case 260 -> { break label_31; }
            case 261 -> { break label_32; }
            case 262 -> { break label_33; }
            case 263 -> { break label_34; }
            case 264 -> { break label_35; }
            case 265 -> { break label_36; }
            case 335 -> { break label_21; }
            case 340 -> { break label_25; }
            case 341 -> { break label_27; }
            case 342 -> { break label_29; }
            case 344 -> { break label_26; }
            case 345 -> { break label_28; }
            case 346 -> { break label_30; }
            default -> { break label_37; }
        }
        label_20:
        break label_38;
        label_21:
        break label_38;
        label_22:
        break label_38;
        label_23:
        break label_38;
        label_24:
        break label_38;
        label_25:
        break label_38;
        label_26:
        break label_38;
        label_27:
        break label_38;
        label_28:
        break label_38;
        label_29:
        break label_38;
        label_30:
        break label_38;
        label_31:
        break label_38;
        label_32:
        break label_38;
        label_33:
        break label_38;
        label_34:
        break label_38;
        label_35:
        break label_38;
        label_36:
        break label_38;
        label_37:
        label_38:
        return key;
    }

    private void refreshModuleLabelCacheIfNeeded(List all, class_327 tr, ClientSettings cs, long now) {
        if ((now - this.moduleLabelLastRefreshMs) < 250L && this.moduleLabelCache.size() <= all.size()) return;
        this.moduleLabelLastRefreshMs = now;
        this.moduleLabelCache.keySet().removeIf((mod) -> !(all.contains(mod)));
        Object var6 = all.iterator();
        while (var6.hasNext()) {
            Module mod = ((Module) var6.next());
            if (mod.getCategory() == Category.CLIENT) {
                break label_6;
            }
            String label = buildModuleLabel(mod);
            this.moduleLabelCache.put(mod, new HudManager.ModuleLabelCache(label, textWidth(tr, cs, label)));
        }
        this.moduleOrderByWidth.clear();
        this.moduleOrderByWidth.addAll(this.moduleLabelCache.keySet());
        this.moduleOrderByWidth.sort((right) -> {
    HudManager.ModuleLabelCache a = ((HudManager.ModuleLabelCache) this.moduleLabelCache.get(left));
    HudManager.ModuleLabelCache b = ((HudManager.ModuleLabelCache) this.moduleLabelCache.get(right));
    if (a == null) {
    } else {
    }
    int aw = a.width();
    if (b == null) {
    } else {
    }
    int bw = b.width();
    int cmp = Integer.compare(bw, aw);
    if (cmp != 0) return cmp;
    return left.getName().compareToIgnoreCase(right.getName());
});
    }

    private static String buildModuleLabel(Module mod) {
        return mod.getName().toUpperCase(Locale.ROOT);
    }

    public void addNotification(String text, int color) {
        if ((text == null) || (text.isBlank())) {
            return;
        }
        this.notifications.add(new HudManager.Notification(text, color, System.currentTimeMillis()));
        while (this.notifications.size() > 5) {
            this.notifications.remove(0);
        }
    }

    public void addAchievement(String title, String desc) {
        if (title == null) break label_1;
        if (title.isBlank()) {
            label_1:
            if ((desc == null) || (desc.isBlank())) {
                return;
            }
        }
        this.achievements.add(new HudManager.Achievement((title == null ? "" : title), (desc == null ? "" : desc), System.currentTimeMillis()));
        while (this.achievements.size() > 3) {
            this.achievements.remove(0);
        }
    }

    private void renderNotifications(class_332 ctx, class_327 tr, ClientSettings cs, int sw, int sh) {
        long now = System.currentTimeMillis();
        this.notifications.removeIf((n) -> (now - n.createdAt) > 2600L);
        if (this.notifications.isEmpty()) return;
        int y = (sh - 20);
        int i = (this.notifications.size() - 1);
        while (i >= 0) {
            HudManager.Notification n = ((HudManager.Notification) this.notifications.get(i));
            float alpha = notificationAlpha((now - n.createdAt), 2600L);
            if (alpha <= 0.01f) {
            } else {
                String text = n.text;
                int w = (chipWidth(tr, cs, text) + 4);
                int x = (((sw - w) - 6) + Math.round(((1.0f - alpha) * ((float) (w + 6)))));
                drawChip(ctx, tr, cs, text, x, y, w, true, withAlpha(n.color, alpha));
                y -= 15;
            }
            i--;
        }
    }

    private void renderAchievements(class_332 ctx, class_327 tr, ClientSettings cs, int sw) {
        long now = System.currentTimeMillis();
        this.achievements.removeIf((a) -> (now - a.createdAt) > 3600L);
        if (this.achievements.isEmpty()) return;
        int y = 22;
        Object var8 = this.achievements.iterator();
        while (var8.hasNext()) {
            HudManager.Achievement a = ((HudManager.Achievement) var8.next());
            float alpha = notificationAlpha((now - a.createdAt), 3600L);
            if (alpha <= 0.01f) {
                break label_5;
            }
            if (a.title.isBlank()) {
            } else {
                if (a.desc.isBlank()) {
                } else {
                }
            }
            String text = ("" + (" - " + a.desc));
            int w = Math.min(280, (chipWidth(tr, cs, text) + 8));
            int x = ((sw - w) / 2);
            drawChip(ctx, tr, cs, trimToWidth(tr, cs, text, (w - 14)), x, y, w, true, withAlpha(DEFAULT_TEXT_ACCENT, alpha));
            y += 16;
        }
    }

    private static float notificationAlpha(long age, long life) {
        long inMs = 180L;
        long outMs = 240L;
        if ((age < 0L) || (age > life)) {
            return 0.0f;
        }
        if (age < 180L) return clamp01((((float) age) / 180.0f));
        if (age > (life - 240L)) return clamp01((1.0f - (((float) (age - (life - 240L))) / 240.0f)));
        return 1.0f;
    }

    private void drawModulePill(class_332 ctx, class_327 tr, ClientSettings cs, String text, int x, int y, int w, boolean rightSide, int textColor) {
        int h = 11;
        int bg = withAlpha(HUD_BG, 0.9f);
        int border = withAlpha(blendColor(HUD_BORDER, HUD_ACCENT, 0.28f), 0.88f);
        int accentCore = withAlpha(HUD_ACCENT, 0.95f);
        drawSmallHudBackdrop(ctx, x, y, w, h, bg, border);
        int accentW = 1;
        int accentH = h;
        int accentY = y;
        int accentX = ((x + w) - accentW);
        fillRect(ctx, accentX, accentY, (accentX + accentW), (accentY + accentH), accentCore);
        int textX = (x + 6);
        drawClientText(ctx, tr, cs, text, textX, (y + 2), textColor);
    }

    private void drawChip(class_332 ctx, class_327 tr, ClientSettings cs, String text, int x, int y, int w, boolean accent, int textColor) {
        int h = 13;
        int bg = withAlpha(HUD_BG, 0.96f);
        int border = withAlpha(blendColor(HUD_BORDER, HUD_ACCENT, 0.22f), 0.86f);
        drawSmallHudBackdrop(ctx, x, y, w, h, bg, border);
        drawClientText(ctx, tr, cs, text, (x + 6), (y + 3), textColor);
    }

    private void drawScaledChip(class_332 ctx, class_327 tr, String text, int x, int y, int w, int textColor, float scale) {
        int h = Math.max(9, scaleUi(13, scale));
        int bg = withAlpha(HUD_BG, 0.96f);
        int border = withAlpha(blendColor(HUD_BORDER, HUD_ACCENT, 0.22f), 0.86f);
        drawSmallHudBackdrop(ctx, x, y, w, h, bg, border);
        drawClientTextScaled(ctx, tr, text, (x + scaleUi(6, scale)), (y + scaleUi(3, scale)), textColor, scale);
    }

    private static int chipWidth(class_327 tr, ClientSettings cs, String value) {
        return (textWidth(tr, cs, value) + 12);
    }

    private static String trimToWidth(class_327 tr, ClientSettings cs, String text, int maxWidth) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (textWidth(tr, cs, text) <= maxWidth) return text;
        String suffix = "...";
        int suffixW = textWidth(tr, cs, suffix);
        if (suffixW >= maxWidth) return "";
        int end = text.length();
        while (end > 0 && (textWidth(tr, cs, text.substring(0, end)) + suffixW) > maxWidth) {
            end--;
        }
        if (end <= 0) {
        } else {
        }
        return (text.substring(0, end) + suffix);
    }

    private static int textWidth(class_327 tr, ClientSettings cs, String value) {
        float measured = ((float) TessellatorRenderer.measureTextWidth(tr, value));
        if (measured >= 0.0f) return Math.max(0, ((int) Math.ceil(((double) measured))));
        Objects.requireNonNull(tr);
        return Math.max(0, ((int) Math.ceil(((double) (((float) value.length()) * Math.max(6.0f, (9.0f * 0.58f)))))));
    }

    private static int textWidthScaled(class_327 tr, String value, float scale) {
        if ((value == null) || (value.isEmpty())) {
            return 0;
        }
        int measured = TessellatorRenderer.measureTextWidth(value, scaledTextSize(tr, scale));
        if (measured > 0) return measured;
        if (tr != null) {
            Objects.requireNonNull(tr);
        } else {
        }
        float base = 9.0f;
        return Math.max(0, ((int) Math.ceil(((double) (((float) value.length()) * Math.max(6.0f, ((base * 0.58f) * Math.max(0.6f, scale))))))));
    }

    private static String trimToWidthScaled(class_327 tr, String text, int maxWidth, float scale) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (textWidthScaled(tr, text, scale) <= maxWidth) return text;
        String suffix = "...";
        int suffixW = textWidthScaled(tr, suffix, scale);
        if (suffixW >= maxWidth) return "";
        int end = text.length();
        while (end > 0 && (textWidthScaled(tr, text.substring(0, end), scale) + suffixW) > maxWidth) {
            end--;
        }
        if (end <= 0) {
        } else {
        }
        return (text.substring(0, end) + suffix);
    }

    private static int scaleUi(int base, float scale) {
        return Math.max(1, Math.round((((float) base) * Math.max(0.6f, scale))));
    }

    private static float scaledTextSize(class_327 tr, float scale) {
        if (tr != null) {
            Objects.requireNonNull(tr);
        } else {
        }
        float base = 9.0f;
        return Math.max(6.0f, (base * Math.max(0.6f, scale)));
    }

    private static void drawClientText(class_332 ctx, class_327 tr, ClientSettings cs, String value, int x, int y, int color) {
        TessellatorRenderer.drawTextWithShadow(ctx, tr, value, x, y, color);
    }

    private static void drawClientTextScaled(class_332 ctx, class_327 tr, String value, int x, int y, int color, float scale) {
        TessellatorRenderer.drawTextWithShadow(ctx, value, ((float) x), ((float) y), scaledTextSize(tr, scale), color);
    }

    private static void drawClientText(class_332 ctx, class_327 tr, ClientSettings cs, String value, int x, int y, int color, boolean shadow) {
        TessellatorRenderer.drawText(ctx, tr, value, x, y, color, shadow);
    }

    private static void fillRect(class_332 ctx, int x1, int y1, int x2, int y2, int color) {
        if (ctx == null) return;
        if ((x2 <= x1) || (y2 <= y1)) {
            return;
        }
        TessellatorRenderer.fillRectSharp(ctx, x1, y1, (x2 - x1), (y2 - y1), color);
    }

    private static void drawHudRoundedBase(class_332 ctx, int x, int y, int w, int h, int bgColor) {
        ClickGUI.rr(ctx, x, y, w, h, bgColor);
    }

    private static int resolveTextColor(ClientSettings cs, long rgbOffset, int fallbackColor) {
        if (cs == null) return fallbackColor;
        return cs.resolveTextColor(rgbOffset);
    }

    private void refreshTopInfoCache(class_310 mc) {
        long now = System.currentTimeMillis();
        if ((now - this.topInfoLastRefreshMs) < 220L) return;
        this.topInfoLastRefreshMs = now;
        this.topInfoFpsText = (mc.method_47599() + " FPS");
        int pingVal = getPing(mc);
        this.topInfoPingColor = resolvePingColor(pingVal);
        if (pingVal < 0) {
        } else {
        }
        "0".topInfoPingText = (String.valueOf(pingVal) + " PING");
        this.topInfoTicksText = "20.0 TICKS";
        if (mc.field_1724 != null) {
            if (this.lastBpsSampleAtMs > 0L) {
                double dt = Math.max(0.001, (((double) (now - this.lastBpsSampleAtMs)) / 1000.0));
                double dx = (mc.field_1724.method_23317() - this.lastBpsX);
                double dz = (mc.field_1724.method_23321() - this.lastBpsZ);
                double bps = (Math.sqrt(((dx * dx) + (dz * dz))) / dt);
                if (!(Double.isFinite(bps))) {
                    bps = 0.0;
                }
                var tmp0 = new Object[1];
                tmp0[0] = Math.max(0.0, bps);
                this.topInfoBpsText = String.format(Locale.US, "%.1f BPS", tmp0);
            }
            this.lastBpsSampleAtMs = now;
            this.lastBpsX = mc.field_1724.method_23317();
            this.lastBpsZ = mc.field_1724.method_23321();
        }
    }

    private int resolvePingColor(int ping) {
        if (ping < 0) return blendColor(DEFAULT_TEXT_DIM, HUD_ACCENT, 0.16f);
        if (ping < 50) return blendColor(HUD_ACCENT, -8788568, 0.32f);
        if (ping < 150) return blendColor(DEFAULT_TEXT, HUD_ACCENT, 0.22f);
        return blendColor(DEFAULT_TEXT_DIM, -2135164, 0.54f);
    }

    private int getPing(class_310 mc) {
        try {
            if (mc.method_1562() != null) {
                if (mc.field_1724 == null) {
                }
                return -1;
                try {
                }
                class_640 entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
                if (entry != null) {
                } else {
                }
                return -1;
            } catch (Exception ex7) {
                Exception e = ex7;
                return -1;
            }
        }
    }

    private static String formatOneDecimal(double value) {
        long scaled = Math.round((value * 10.0));
        long whole = (scaled / 10L);
        long tenths = Math.abs((scaled % 10L));
        return (whole + "." + tenths);
    }

    private static void refreshClientThemeColors(ClientSettings cs) {
        ClickGuiTheme.Palette palette = ClickGuiTheme.resolve();
        int accent = opaque(palette.accent());
        int accentSoft = opaque(palette.accentSoft());
        int bg = opaque(palette.bg());
        int border = opaque(palette.panelBorder());
        if (cs == null) {
        } else {
        }
        int txtOn = opaque(cs.resolveTextColor(0L));
        int txtOff = opaque(palette.txtOff());
        HUD hud = HUD.get();
        if (hud != null) {
        } else {
        }
        HUD.Style style = HUD.Style.DEFAULT;
        switch (HudManager.1.$SwitchMap$me$drake$viecheats$modules$client$HUD$Style[style.ordinal()]) {
            case 1 -> { break label_14; }
            case 2 -> { break label_21; }
            case 3 -> { break label_28; }
            default -> { break label_28; }
        }
        label_14:
        accent = blendColor(accent, -23987, 0.62f);
        accentSoft = blendColor(accentSoft, -13942, 0.66f);
        bg = blendColor(bg, -14477812, 0.52f);
        border = blendColor(border, -10798559, 0.58f);
        txtOn = blendColor(txtOn, -3355, 0.25f);
        txtOff = blendColor(txtOff, -2573670, 0.44f);
        break label_28;
        label_21:
        accent = blendColor(accent, -11023361, 0.62f);
        accentSoft = blendColor(accentSoft, -5642241, 0.66f);
        bg = blendColor(bg, -15853529, 0.54f);
        border = blendColor(border, -13940890, 0.56f);
        txtOn = blendColor(txtOn, -1378305, 0.24f);
        txtOff = blendColor(txtOff, -5650206, 0.4f);
        break label_28;
        label_28:
        HUD_ACCENT = accent;
        HUD_BG = bg;
        HUD_BORDER = border;
        DEFAULT_TEXT = txtOn;
        DEFAULT_TEXT_DIM = txtOff;
        DEFAULT_TEXT_ACCENT = accentSoft;
    }

    private static int withAlpha(int color, float alphaMul) {
        float mul = clamp01(alphaMul);
        int a = ((color >>> 24) & 255);
        if (a == 0) {
            a = 255;
        }
        int aa = Math.max(0, Math.min(255, Math.round((((float) a) * mul))));
        return ((aa << 24) | (color & 16777215));
    }

    private static int opaque(int color) {
        return (-16777216 | (color & 16777215));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int blendColor(int a, int b, float t) {
        float f = clamp01(t);
        int aa = ((a >>> 24) & 255);
        int ar = ((a >>> 16) & 255);
        int ag = ((a >>> 8) & 255);
        int ab = (a & 255);
        int ba = ((b >>> 24) & 255);
        int br = ((b >>> 16) & 255);
        int bg = ((b >>> 8) & 255);
        int bb = (b & 255);
        int oa = ((int) (((float) aa) + (((float) (ba - aa)) * f)));
        int or = ((int) (((float) ar) + (((float) (br - ar)) * f)));
        int og = ((int) (((float) ag) + (((float) (bg - ag)) * f)));
        int ob = ((int) (((float) ab) + (((float) (bb - ab)) * f)));
        return ((((oa << 24) | (or << 16)) | (og << 8)) | ob);
    }

    static {
        HUD_BG = -15657182;
        HUD_BORDER = -13880252;
        HUD_ACCENT = -10897153;
        DEFAULT_TEXT = -657153;
        DEFAULT_TEXT_DIM = -4734767;
        DEFAULT_TEXT_ACCENT = -7548673;
    }


    public record HudBounds(int x, int y, int width, int height) {

        public HudBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean visible() {
            if (this.width <= 0) break label_1;
            return this.height > 0;
        }

        public boolean contains(int px, int py) {
            if ((((!(visible())) || (px < this.x)) || (px > (this.x + this.width))) || (py < this.y)) break label_1;
            return py <= (this.y + this.height);
        }

        public static HudManager.HudBounds hidden() {
            return new HudManager.HudBounds(0, 0, 0, 0);
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

    }

    record Achievement(String title, String desc, long createdAt) {

        private Achievement(String title, String desc, long createdAt) {
            this.title = title;
            this.desc = desc;
            this.createdAt = createdAt;
        }

        public String title() {
            return this.title;
        }

        public String desc() {
            return this.desc;
        }

        public long createdAt() {
            return this.createdAt;
        }

    }

    enum HudDragTarget {
        NONE,
        SPOTIFY,
        REGION_MAP,
        TOP_INFO,
        COORDS,
        MODULE_LIST,
        HOTKEYS;


        private HudDragTarget() {
        }

    }

    record DragTargetRect(HudManager.HudDragTarget target, String label, int x, int y, int w, int h, boolean absolute) {

        private DragTargetRect(HudManager.HudDragTarget target, String label, int x, int y, int w, int h, boolean absolute) {
            this.target = target;
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.absolute = absolute;
        }

        boolean contains(int px, int py) {
            if (((px < this.x) || (px > (this.x + this.w))) || (py < this.y)) break label_1;
            return py <= (this.y + this.h);
        }

        public HudManager.HudDragTarget target() {
            return this.target;
        }

        public String label() {
            return this.label;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public int w() {
            return this.w;
        }

        public int h() {
            return this.h;
        }

        public boolean absolute() {
            return this.absolute;
        }

    }

    class 1 {

        static {
            $SwitchMap$me$drake$viecheats$modules$client$HUD$Style = new int[HUD.Style.values().length];
            try {
                $SwitchMap$me$drake$viecheats$modules$client$HUD$Style[HUD.Style.WARM.ordinal()] = 1;
            } catch (NoSuchFieldError ex3) {
                Object var0 = ex3;
                try {
                }
                $SwitchMap$me$drake$viecheats$modules$client$HUD$Style[HUD.Style.COOL.ordinal()] = 2;
            } catch (NoSuchFieldError ex6) {
                var0 = ex6;
                try {
                }
                $SwitchMap$me$drake$viecheats$modules$client$HUD$Style[HUD.Style.DEFAULT.ordinal()] = 3;
            } catch (NoSuchFieldError ex9) {
                var0 = ex9;
            }
        }

    }

    record Notification(String text, int color, long createdAt) {

        private Notification(String text, int color, long createdAt) {
            this.text = text;
            this.color = color;
            this.createdAt = createdAt;
        }

        public String text() {
            return this.text;
        }

        public int color() {
            return this.color;
        }

        public long createdAt() {
            return this.createdAt;
        }

    }

    record ModuleLabelCache(String label, int width) {

        private ModuleLabelCache(String label, int width) {
            this.label = label;
            this.width = width;
        }

        public String label() {
            return this.label;
        }

        public int width() {
            return this.width;
        }

    }

    record KeybindHudEntry(String key, String module, String label, int width) {

        private KeybindHudEntry(String key, String module, String label, int width) {
            this.key = key;
            this.module = module;
            this.label = label;
            this.width = width;
        }

        public String key() {
            return this.key;
        }

        public String module() {
            return this.module;
        }

        public String label() {
            return this.label;
        }

        public int width() {
            return this.width;
        }

    }
}

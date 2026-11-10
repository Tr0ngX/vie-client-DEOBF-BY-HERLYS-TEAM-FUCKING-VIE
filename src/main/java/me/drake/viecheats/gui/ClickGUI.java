package me.drake.viecheats.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ButtonSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.KeybindSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.core.setting.StringSetting;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.render.ImmediateGuiRenderer;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ClickGUI extends class_437 {
    static int BG;
    static int PANEL_BORDER;
    static int ROW_BG;
    static int ROW_BORDER;
    static int ROW_HOVER;
    static int ACCENT;
    static int ACCENT_SOFT;
    static int TGL_OFF;
    static int TGL_ON;
    static int TXT_ON;
    static int TXT_OFF;
    static int SEARCH_BG;
    static final int PANEL_WIDTH = 156;
    static final int PANEL_HEADER_H = 24;
    static final int CATEGORY_BADGE_H = 12;
    static final int MODULE_ROW_H = 19;
    static final int MODULE_ROW_RADIUS = 4;
    static final int PANEL_GAP_X = 6;
    static final int PANEL_START_X = 12;
    static final int PANEL_START_Y = 44;
    static final int SETTINGS_ROW_H = 22;
    static final int INLINE_SETTINGS_HEADER_H = 18;
    static final int INLINE_SETTINGS_GAP_Y = 3;
    static final int SEARCH_COLLAPSED_W = 38;
    static final int SEARCH_EXPANDED_W = 285;
    static final int SEARCH_H = 30;
    static final int SEARCH_SAFE_BOTTOM_MIN = 58;
    static final int PILL_W = 16;
    static final int PILL_H = 9;
    static final int PICKER_SV = 92;
    static final int PICKER_HUE_W = 12;
    static final int PICKER_RGB_BAR_H = 5;
    static final int PICKER_RGB_BAR_GAP = 5;
    private static final double DEFAULT_TARGET_GUI_SCALE = 1.8;
    private static final Object LAYOUT_LOCK;
    private static final Map SAVED_PANEL_STATE;
    private final List panels;
    private final Map toggleAnim;
    private final Map inlineExpandAnim;
    private final List settingHitBoxes;
    private ClickGUI.CategoryPanel lastInteractedPanel;
    private ClickGUI.CategoryPanel draggingPanel;
    private ClickGUI.CategoryPanel headerClickPanel;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean dragMoved;
    private Module selectedModule;
    private Module hoveredModule;
    private String hoveredModuleDescription;
    private int hoveredModuleMouseX;
    private int hoveredModuleMouseY;
    private IntSetting draggingIntSetting;
    private DoubleSetting draggingDoubleSetting;
    private int draggingSliderX;
    private int draggingSliderW;
    private KeybindSetting listeningKeybind;
    private StringSetting editingStringSetting;
    private String editingStringBuffer;
    private IntSetting editingIntSetting;
    private String editingIntBuffer;
    private DoubleSetting editingDoubleSetting;
    private String editingDoubleBuffer;
    private ColorSetting colorPickerSetting;
    private int colorPickerX;
    private int colorPickerY;
    private int colorPickerW;
    private int colorPickerH;
    private float pickHue;
    private float pickSat;
    private float pickVal;
    private int pickR;
    private int pickG;
    private int pickB;
    private boolean pickDragSv;
    private boolean pickDragHue;
    private int pickDragRgb;
    private String searchQuery;
    private boolean searchFocused;
    private float searchExpand;
    private int searchBarX;
    private int searchBarY;
    private int searchBarW;
    private float uiScale;
    private int uiWidth;
    private int uiHeight;
    private class_332 activeDrawContext;
    private final Deque clipStack;
    private long lastFrameNanos;
    private float frameDt;
    private float openProgress;
    private boolean closing;
    private long openedAtMs;

    public ClickGUI() {
        super(class_2561.method_43470("Vie Cheats"));
        this.panels = new ArrayList();
        this.toggleAnim = new HashMap();
        this.inlineExpandAnim = new HashMap();
        this.settingHitBoxes = new ArrayList();
        this.editingStringBuffer = "";
        this.editingIntBuffer = "";
        this.editingDoubleBuffer = "";
        this.colorPickerW = 166;
        this.colorPickerH = 156;
        this.pickDragRgb = -1;
        this.searchQuery = "";
        this.uiScale = 1.0f;
        this.clipStack = new ArrayDeque();
        this.frameDt = 0.016666668f;
    }

    private void updateUiScale() {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
        } else {
        }
        double currentScale = ((double) mc.method_22683().method_4495());
        if (currentScale <= 0.0) {
            currentScale = 1.0;
        }
        double targetScale = resolveTargetGuiScale(this.field_22789, this.field_22790, currentScale);
        double scale = (targetScale / currentScale);
        if (scale < 0.25) {
            scale = 0.25;
        }
        if (scale > 4.0) {
            scale = 4.0;
        }
        this.uiScale = ((float) scale);
        this.uiWidth = toVirtualSize(this.field_22789, this.uiScale);
        this.uiHeight = toVirtualSize(this.field_22790, this.uiScale);
    }

    private static double resolveTargetGuiScale(int screenWidth, int screenHeight, double currentScale) {
        ClientSettings cs = ClientSettings.get();
        if (cs == null) {
        } else {
        }
        double value = cs.getGuiScale();
        if (!(Double.isFinite(value))) {
            value = 1.8;
        }
        if (value < 0.1) {
            value = 0.1;
        }
        int rawWidth = Math.max(1, ((int) Math.round((((double) screenWidth) * Math.max(0.5, currentScale)))));
        int rawHeight = Math.max(1, ((int) Math.round((((double) screenHeight) * Math.max(0.5, currentScale)))));
        if (rawWidth > 1366) {
            if (rawHeight <= 768) {
            }
            value = Math.min(value, 1.45);
        }
        if (rawWidth > 1280) {
            if (rawHeight <= 720) {
            }
            value = Math.min(value, 1.25);
        }
        if (rawWidth > 1024) {
            if (rawHeight <= 600) {
            }
            value = Math.min(value, 1.0);
        }
        return value;
    }

    private static int toVirtualSize(int currentScaledSize, float scale) {
        if ((currentScaledSize <= 0) || (scale <= 0.0f)) {
            return currentScaledSize;
        }
        return Math.max(1, ((int) Math.ceil(((double) (((float) currentScaledSize) / scale)))));
    }

    private int toUiCoord(double coord) {
        if (this.uiScale <= 0.0f) return ((int) coord);
        return ((int) Math.floor((coord / ((double) this.uiScale))));
    }

    protected void method_25426() {
        updateUiScale();
        rebuildPanels();
        this.lastInteractedPanel = null;
        applySavedLayout();
        resolvePanelOverlapsIfNeeded();
        clampPanelsToViewport(false);
        persistCurrentLayout();
        updateSearchBounds();
        this.openedAtMs = System.currentTimeMillis();
        this.openProgress = 0.0f;
        this.closing = false;
        this.lastFrameNanos = 0L;
        this.frameDt = 0.016666668f;
        if (this.selectedModule != null && !(moduleStillExists(this.selectedModule))) {
            this.selectedModule = null;
        }
    }

    private void rebuildPanels() {
        this.panels.clear();
        int x = 12;
        int y = 44;
        int rightLimit = Math.max(168, (this.uiWidth - 12));
        int rowStep = ((panelBodyMaxHeight() + 24) + 12);
        Object var5 = getCategoryOrder();
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Category category = var5[var7];
            List modules = VieCheatsMod.getInstance().getModuleManager().getModules(category);
            if (modules.isEmpty()) {
            } else {
                if ((x + 156) > rightLimit) {
                    x = 12;
                    y = (y + rowStep);
                }
                this.panels.add(new ClickGUI.CategoryPanel(category, new ArrayList(modules), x, y));
                x += 162;
            }
            var7++;
        }
    }

    private boolean moduleStillExists(Module module) {
        if (module == null) return false;
        List mods = VieCheatsMod.getInstance().getModuleManager().getModules();
        return mods.contains(module);
    }

    private static Category[] getCategoryOrder() {
        var tmp0 = new Category[7];
        tmp0[0] = Category.MOVEMENT;
        tmp0[1] = Category.PLAYER;
        tmp0[2] = Category.RENDER;
        tmp0[3] = Category.WORLD;
        tmp0[4] = Category.EXPLOIT;
        tmp0[5] = Category.DONUT_SMP;
        tmp0[6] = Category.CLIENT;
        return tmp0;
    }

    private void applySavedLayout() {
        Object var1 = LAYOUT_LOCK;
        // synchronized (LAYOUT_LOCK)
        if (SAVED_PANEL_STATE.isEmpty()) return;
        Object var2 = this.panels.iterator();
        while (var2.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var2.next());
            ClickGUI.SavedPanelState state = ((ClickGUI.SavedPanelState) SAVED_PANEL_STATE.get(panel.category.name()));
            if (state == null) {
                break label_4;
            }
            panel.x = state.x;
            panel.y = state.y;
            panel.collapsed = state.fold;
            panel.expandAnim = (!(!panel.collapsed) ? 0.0f : 1.0f);
        }
        break label_18;
        Object var5 = null;
        throw var5;
        label_18:
    }

    private void persistCurrentLayout() {
        Object var1 = LAYOUT_LOCK;
        // synchronized (LAYOUT_LOCK)
        SAVED_PANEL_STATE.clear();
        Object var2 = this.panels.iterator();
        while (var2.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var2.next());
            SAVED_PANEL_STATE.put(panel.category.name(), new ClickGUI.SavedPanelState(panel.x, panel.y, panel.collapsed));
        }
        break label_10;
        Object var4 = null;
        throw var4;
        label_10:
    }

    private void requestLayoutSave() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getConfigManager() == null)) {
            return;
        }
        instance.getConfigManager().requestSave();
    }

    public static JsonObject exportLayoutState() {
        JsonObject root = new JsonObject();
        JsonObject panelsObj = new JsonObject();
        Object var2 = LAYOUT_LOCK;
        // synchronized (LAYOUT_LOCK)
        Object var3 = SAVED_PANEL_STATE.entrySet().iterator();
        while (var3.hasNext()) {
            Map.Entry entry = ((Map.Entry) var3.next());
            ClickGUI.SavedPanelState state = ((ClickGUI.SavedPanelState) entry.getValue());
            JsonObject panel = new JsonObject();
            panel.addProperty("x", state.x);
            panel.addProperty("y", state.y);
            panel.addProperty("fold", state.fold);
            panelsObj.add(((String) entry.getKey()), panel);
        }
        break label_16;
        Object var7 = null;
        throw var7;
        label_16:
        root.add("panels", panelsObj);
        return root;
    }

    public static void importLayoutState(JsonObject root) {
        Object var1 = LAYOUT_LOCK;
        // synchronized (LAYOUT_LOCK)
        SAVED_PANEL_STATE.clear();
        if (root == null) return;
        JsonObject panelsObj = root.getAsJsonObject("panels");
        if (panelsObj == null) return;
        Object var3 = panelsObj.entrySet().iterator();
        while (var3.hasNext()) {
            Map.Entry entry = ((Map.Entry) var3.next());
            if (!(((JsonElement) entry.getValue()).isJsonObject())) {
                break label_8;
            }
            JsonObject panel = ((JsonElement) entry.getValue()).getAsJsonObject();
            if (panel.has("x")) {
            } else {
            }
            int x = 12;
            if (panel.has("y")) {
            } else {
            }
            int y = 44;
            if (!(panel.has("fold"))) break label_18;
            boolean fold = panel.get("fold").getAsBoolean();
            SAVED_PANEL_STATE.put(((String) entry.getKey()), new ClickGUI.SavedPanelState(x, y, fold));
        }
        break label_26;
        Object var9 = panel.get("y").getAsInt();
        throw var9;
        label_26:
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
    }

    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        TessellatorRenderer.setContext(ctx);
        this.activeDrawContext = ctx;
        this.clipStack.clear();
        boolean matrixPushed = false;
        updateUiScale();
        ctx.method_51448().pushMatrix();
        matrixPushed = true;
        ctx.method_51448().scale(this.uiScale, this.uiScale);
        if (this.panels.isEmpty()) {
            rebuildPanels();
            applySavedLayout();
        }
        if (this.lastInteractedPanel != null && !(this.panels.contains(this.lastInteractedPanel))) {
            this.lastInteractedPanel = null;
        }
        if (this.lastInteractedPanel != null) {
            bringPanelToFront(this.lastInteractedPanel);
        }
        refreshClientThemeColors();
        tickFrameTime();
        tickOpenAnimation();
        tickSearchAnimation();
        tickPanelCollapseAnimations();
        tickInlineExpandAnimations();
        tickPanelScrollAnimations();
        if (!(!this.closing) && this.openProgress <= 0.001f) {
            finishClose();
            if (this.activeDrawContext == null) break label_28;
            while (!(this.clipStack.isEmpty())) {
                this.clipStack.pop();
                this.activeDrawContext.method_44380();
            }
            label_28:
            this.clipStack.clear();
            if (!(!matrixPushed)) {
                ctx.method_51448().popMatrix();
            }
            this.activeDrawContext = null;
            return;
        }
        clampPanelsToViewport(false);
        int uiMx = toUiCoord(((double) mouseX));
        int uiMy = toUiCoord(((double) mouseY));
        float alphaMul = easeOutCubic(this.openProgress);
        this.settingHitBoxes.clear();
        this.hoveredModule = null;
        this.hoveredModuleDescription = null;
        this.hoveredModuleMouseX = uiMx;
        this.hoveredModuleMouseY = uiMy;
        renderBackdrop(this.uiWidth, this.uiHeight, alphaMul);
        renderSearchBar(uiMx, uiMy, alphaMul);
        Object var9 = this.panels.iterator();
        while (var9.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var9.next());
            renderCategoryPanel(panel, uiMx, uiMy, alphaMul);
        }
        if (this.colorPickerSetting != null) {
            renderColorPicker(uiMx, uiMy, alphaMul);
        } else {
            renderModuleTooltip(alphaMul);
        }
        if (this.activeDrawContext == null) break label_54;
        while (!(this.clipStack.isEmpty())) {
            this.clipStack.pop();
            this.activeDrawContext.method_44380();
        }
        label_54:
        this.clipStack.clear();
        if (!(!matrixPushed)) {
            ctx.method_51448().popMatrix();
        }
        this.activeDrawContext = null;
        break label_69;
        Object var11 = null;
        if (this.activeDrawContext == null) break label_64;
        while (!(this.clipStack.isEmpty())) {
            this.clipStack.pop();
            this.activeDrawContext.method_44380();
        }
        label_64:
        this.clipStack.clear();
        if (!(!matrixPushed)) {
            ctx.method_51448().popMatrix();
        }
        this.activeDrawContext = null;
        throw var11;
        label_69:
    }

    private void tickFrameTime() {
        long now = System.nanoTime();
        if (this.lastFrameNanos != 0L) {
            this.frameDt = (((float) (now - this.lastFrameNanos)) / 1.0E9f);
            this.frameDt = Math.max(0.0f, Math.min(0.05f, this.frameDt));
        }
        this.lastFrameNanos = now;
    }

    private void tickOpenAnimation() {
        float target = (!(!this.closing) ? 0.0f : 1.0f);
        this.openProgress = animate(this.openProgress, target, 10.5f, this.frameDt);
        if (Math.abs((this.openProgress - target)) < 0.0025f) {
            this.openProgress = target;
        }
    }

    private void tickSearchAnimation() {
        updateSearchBounds();
        if (!this.searchFocused) {
            if (!(this.searchQuery.isEmpty())) {
            }
        } else {
        }
        float target = 0.0f;
        this.searchExpand = animate(this.searchExpand, target, 10.5f, this.frameDt);
        if (Math.abs((this.searchExpand - target)) < 0.0025f) {
            this.searchExpand = target;
        }
    }

    private void tickPanelCollapseAnimations() {
        Object var1 = this.panels.iterator();
        while (var1.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var1.next());
            float target = (!(!panel.collapsed) ? 0.0f : 1.0f);
            panel.expandAnim = animate(panel.expandAnim, target, 11.2f, this.frameDt);
            if (Math.abs((panel.expandAnim - target)) < 0.003f) {
                panel.expandAnim = target;
            }
        }
    }

    private void tickInlineExpandAnimations() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        List allModules = instance.getModuleManager().getModules();
        Object var3 = allModules.iterator();
        while (var3.hasNext()) {
            Module module = ((Module) var3.next());
            float target = (this.selectedModule == module ? 1.0f : 0.0f);
            float current = ((Float) this.inlineExpandAnim.getOrDefault(module, 0.0f)).floatValue();
            float next = animate(current, target, 8.8f, this.frameDt);
            if (Math.abs((next - target)) < 0.003f) {
                next = target;
            }
            if (next <= 0.001f && target == 0.0f) {
                this.inlineExpandAnim.remove(module);
            } else {
                this.inlineExpandAnim.put(module, next);
            }
        }
        this.inlineExpandAnim.entrySet().removeIf((entry) -> !(allModules.contains(entry.getKey())));
    }

    private void tickPanelScrollAnimations() {
        Object var1 = this.panels.iterator();
        while (var1.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var1.next());
            if (!panel.collapsed) {
                if (panel.expandAnim < 0.06f) {
                }
                panel.scroll = 0;
                panel.scrollAnim = 0.0f;
                break label_1;
            }
            int maxScroll = getPanelMaxScroll(panel);
            panel.scroll = clamp(panel.scroll, 0, maxScroll);
            panel.scrollAnim = animate(panel.scrollAnim, ((float) panel.scroll), 18.5f, this.frameDt);
            if (Math.abs((panel.scrollAnim - ((float) panel.scroll))) < 0.08f) {
                panel.scrollAnim = ((float) panel.scroll);
            }
        }
    }

    private float getInlineExpandProgress(Module module) {
        if (module == null) return 0.0f;
        return ((Float) this.inlineExpandAnim.getOrDefault(module, 0.0f)).floatValue();
    }

    private void updateSearchBounds() {
        int safeBottom = Math.max(58, Math.round((((float) this.uiHeight) * 0.15f)));
        this.searchBarY = Math.max(80, ((this.uiHeight - 30) - safeBottom));
        int expandedW = Math.min(285, Math.max(190, (this.uiWidth - 120)));
        float eased = easeOutCubic(this.searchExpand);
        this.searchBarW = (38 + Math.round((((float) (expandedW - 38)) * eased)));
        this.searchBarX = ((this.uiWidth - this.searchBarW) / 2);
    }

    private int panelBodyMaxHeight() {
        int maxByScreen = Math.max(120, (this.uiHeight - 190));
        int maxBySearch = Math.max(120, (((this.searchBarY - 44) - 24) - 18));
        return Math.min(maxByScreen, maxBySearch);
    }

    private int getPanelsMaxX() {
        return Math.max(12, ((this.uiWidth - 156) - 10));
    }

    private boolean clampPanelsToViewport(boolean updatePersistedState) {
        boolean changed = false;
        int minX = 12;
        int maxX = getPanelsMaxX();
        int minY = 44;
        Object var6 = this.panels.iterator();
        while (var6.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var6.next());
            int panelH = getPanelHeight(panel);
            int maxY = Math.max(minY, ((this.searchBarY - panelH) - 6));
            int clampedX = clamp(panel.x, minX, maxX);
            int clampedY = clamp(panel.y, minY, maxY);
            int maxScroll = getPanelMaxScroll(panel);
            int clampedScroll = clamp(panel.scroll, 0, maxScroll);
            if (clampedX == panel.x && clampedY == panel.y) {
                if (clampedScroll != panel.scroll) {
                }
                panel.x = clampedX;
                panel.y = clampedY;
                panel.scroll = clampedScroll;
                changed = true;
            }
            panel.scrollAnim = Math.max(0.0f, Math.min(panel.scrollAnim, ((float) maxScroll)));
        }
        if (!(!changed) && !(!updatePersistedState)) {
            persistCurrentLayout();
        }
        return changed;
    }

    private void resolvePanelOverlapsIfNeeded() {
        if (this.panels.size() <= 1) return;
        if (!(hasPanelOverlap())) return;
        int x = 12;
        int y = 44;
        int rightLimit = Math.max(168, (this.uiWidth - 10));
        int rowStep = ((panelBodyMaxHeight() + 24) + 12);
        Object var5 = this.panels.iterator();
        while (var5.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var5.next());
            if ((x + 156) > rightLimit) {
                x = 12;
                y = (y + rowStep);
            }
            panel.x = x;
            panel.y = y;
            panel.scroll = 0;
            panel.scrollAnim = 0.0f;
            x += 162;
        }
        clampPanelsToViewport(false);
    }

    private boolean hasPanelOverlap() {
        int i = 0;
        while (i < this.panels.size()) {
            ClickGUI.CategoryPanel a = ((ClickGUI.CategoryPanel) this.panels.get(i));
            int ax1 = a.x;
            int ay1 = a.y;
            int ax2 = (ax1 + 156);
            int ay2 = (ay1 + getPanelHeight(a));
            int j = (i + 1);
            while (j < this.panels.size()) {
                ClickGUI.CategoryPanel b = ((ClickGUI.CategoryPanel) this.panels.get(j));
                int bx1 = b.x;
                int by1 = b.y;
                int bx2 = (bx1 + 156);
                int by2 = (by1 + getPanelHeight(b));
                int ix = (Math.min(ax2, bx2) - Math.max(ax1, bx1));
                int iy = (Math.min(ay2, by2) - Math.max(ay1, by1));
                if (ix > 0 && iy > 0) return true;
                j++;
            }
            i++;
        }
        return false;
    }

    private void renderBackdrop(int w, int h, float alphaMul) {
        int topDim = alpha(705302813, alphaMul);
        int bottomDim = alpha(503976221, alphaMul);
        TessellatorRenderer.fillGradientRect(0, 0, w, h, topDim, bottomDim, true);
    }

    private void renderSearchBar(int mx, int my, float alphaMul) {
        int x = this.searchBarX;
        int y = this.searchBarY;
        int w = this.searchBarW;
        int h = 30;
        if (!(!this.searchFocused)) {
        } else {
        }
        int baseBg = SEARCH_BG;
        int fill = alpha(baseBg, alphaMul);
        rrNano(x, y, w, h, fill);
        int iconCx = (x + 18);
        int iconCy = (y + (h / 2));
        drawSearchIcon(iconCx, iconCy, alpha(TXT_ON, alphaMul));
        if (this.searchExpand > 0.2f) {
            String shown = (this.searchQuery.isEmpty() ? "Search modules..." : this.searchQuery);
            if (!(!this.searchFocused) && (System.currentTimeMillis() % 900L) < 450L) {
                shown = (shown + "_");
            }
            shown = trimToWidth(shown, (w - 46));
            if (this.searchQuery.isEmpty()) {
            } else {
            }
            int color = alpha(TXT_ON, alphaMul);
            drawText(shown, (x + 34), (y + 10), color);
        }
        if (mx >= x && mx <= (x + w) && my >= y && my <= (y + h)) {
        }
    }

    private void drawSearchIcon(int cx, int cy, int color) {
        TessellatorRenderer.drawCircle((((float) cx) - 1.0f), (((float) cy) - 1.0f), 5.0f, color);
        TessellatorRenderer.drawCircle((((float) cx) - 1.0f), (((float) cy) - 1.0f), 3.4f, alpha(SEARCH_BG, 1.0f));
        TessellatorRenderer.drawLine((((float) cx) + 2.0f), (((float) cy) + 2.0f), (((float) cx) + 6.5f), (((float) cy) + 6.5f), 1.3f, color);
    }

    private void drawCategoryGlyph(Category category, int x, int y, int size, int color) {
        class_332 ctx = this.activeDrawContext;
        if (ctx == null) break label_2;
        if (size > 2) break label_3;
        label_2:
        return;
        label_3:
        int s = Math.max(8, size);
        int x2 = (x + s);
        int y2 = (y + s);
        int cx = (x + (s / 2));
        int cy = (y + (s / 2));
        float stroke = Math.max(1.0f, (((float) s) / 8.0f));
        switch (ClickGUI.1.$SwitchMap$me$drake$viecheats$core$module$Category[category.ordinal()]) {
            case 1 -> { break label_10; }
            case 2 -> { break label_14; }
            case 3 -> { break label_18; }
            case 4 -> { break label_22; }
            case 5 -> { break label_28; }
            case 6 -> { break label_35; }
            case 7 -> { break label_41; }
            case 8 -> { break label_48; }
            default -> { break label_53; }
        }
        label_10:
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y + 1)), ((float) (x2 - 2)), ((float) (y2 - 2)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x2 - 2)), ((float) (y + 1)), ((float) (x + 1)), ((float) (y2 - 2)), stroke, color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), (cy - 1), (cx + 1), (cy + 1), color);
        break label_53;
        label_14:
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) cy), ((float) (x2 - 3)), ((float) cy), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x2 - 5)), ((float) (y + 2)), ((float) (x2 - 2)), ((float) cy), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x2 - 5)), ((float) (y2 - 2)), ((float) (x2 - 2)), ((float) cy), stroke, color);
        break label_53;
        label_18:
        ImmediateGuiRenderer.fillRect(ctx, (cx - 2), (y + 1), (cx + 2), (y + 4), color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) cx), ((float) (y + 4)), ((float) cx), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 2)), ((float) (y + 5)), ((float) (x2 - 2)), ((float) (y + 5)), stroke, color);
        break label_53;
        label_22:
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) cy), ((float) cx), ((float) (y + 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) cx), ((float) (y + 1)), ((float) (x2 - 1)), ((float) cy), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) cy), ((float) cx), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) cx), ((float) (y2 - 1)), ((float) (x2 - 1)), ((float) cy), stroke, color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), (cy - 1), (cx + 1), (cy + 1), color);
        break label_53;
        label_28:
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y + 1)), ((float) (x2 - 1)), ((float) (y + 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y2 - 1)), ((float) (x2 - 1)), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y + 1)), ((float) (x + 1)), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x2 - 1)), ((float) (y + 1)), ((float) (x2 - 1)), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) cx), ((float) (y + 1)), ((float) cx), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) cy), ((float) (x2 - 1)), ((float) cy), stroke, color);
        break label_53;
        label_35:
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y2 - 1)), ((float) cx), ((float) (y + 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) cx), ((float) (y + 1)), ((float) (x2 - 1)), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.drawLine(ctx, ((float) (x + 1)), ((float) (y2 - 1)), ((float) (x2 - 1)), ((float) (y2 - 1)), stroke, color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), (y + 3), (cx + 1), (y2 - 3), color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), (y2 - 2), (cx + 1), (y2 - 1), color);
        break label_53;
        label_41:
        ImmediateGuiRenderer.fillRect(ctx, (x + 2), (y + 1), (x2 - 2), (y + 2), color);
        ImmediateGuiRenderer.fillRect(ctx, (x + 2), (y2 - 2), (x2 - 2), (y2 - 1), color);
        ImmediateGuiRenderer.fillRect(ctx, (x + 1), (y + 2), (x + 2), (y2 - 2), color);
        ImmediateGuiRenderer.fillRect(ctx, (x2 - 2), (y + 2), (x2 - 1), (y2 - 2), color);
        ImmediateGuiRenderer.fillRect(ctx, (cx + 1), (cy - 2), (cx + 2), (cy - 1), color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 2), (cy + 1), (cx - 1), (cy + 2), color);
        break label_53;
        label_48:
        ImmediateGuiRenderer.fillRect(ctx, (cx - 2), (cy - 2), (cx + 2), (cy + 2), color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), y, (cx + 1), (y + 2), color);
        ImmediateGuiRenderer.fillRect(ctx, (cx - 1), (y2 - 2), (cx + 1), y2, color);
        ImmediateGuiRenderer.fillRect(ctx, x, (cy - 1), (x + 2), (cy + 1), color);
        ImmediateGuiRenderer.fillRect(ctx, (x2 - 2), (cy - 1), x2, (cy + 1), color);
        label_53:
    }

    private int categoryAccent(Category category) {
        switch (ClickGUI.1.$SwitchMap$me$drake$viecheats$core$module$Category[category.ordinal()]) {
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            case 5 -> { break label_6; }
            case 6 -> { break label_7; }
            case 7 -> { break label_8; }
            case 8 -> { break label_9; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_10;
        label_3:
        break label_10;
        label_4:
        break label_10;
        label_5:
        break label_10;
        label_6:
        break label_10;
        label_7:
        break label_10;
        label_8:
        break label_10;
        label_9:
        label_10:
        int semantic = -7875841;
        return blendColor(semantic, ACCENT, 0.56f);
    }

    private void renderCategoryPanel(ClickGUI.CategoryPanel panel, int mx, int my, float alphaMul) {
        List visibleModules = getFilteredModules(panel);
        int bodyVisible = getPanelVisibleBodyHeight(panel, visibleModules);
        int panelH = ((24 + bodyVisible) + 6);
        int panelAccent = categoryAccent(panel.category);
        int panelAccentSoft = blendColor(panelAccent, -1, 0.3f);
        panel.scroll = clamp(panel.scroll, 0, getPanelMaxScroll(panel, visibleModules, bodyVisible));
        panel.scrollAnim = Math.max(0.0f, Math.min(panel.scrollAnim, ((float) getPanelMaxScroll(panel, visibleModules, bodyVisible))));
        if (this.panels.isEmpty()) break label_8;
        boolean front = this.panels.get((this.panels.size() - 1)) == panel;
        drawPanelShell(panel.x, panel.y, 156, panelH, front, alphaMul, panelAccent);
        int accentLine = alpha(blendColor(panelAccent, panelAccentSoft, 0.42f), alphaMul);
        TessellatorRenderer.fillRect((panel.x + 7), ((panel.y + 24) - 1), ((panel.x + 156) - 7), (panel.y + 24), accentLine);
        int iconSize = 10;
        int iconX = (panel.x + 8);
        int iconY = (panel.y + 7);
        int iconColor = alpha(blendColor(TXT_ON, panelAccentSoft, 0.45f), alphaMul);
        drawCategoryGlyph(panel.category, iconX, iconY, iconSize, iconColor);
        int titleX = ((iconX + iconSize) + 6);
        int titleY = (panel.y + 8);
        if (!(!panel.collapsed)) {
        } else {
        }
        String right = String.valueOf(visibleModules.size());
        int rw = nanoTextWidth(right);
        int rightX = (((panel.x + 156) - rw) - 8);
        int rightY = (panel.y + 8);
        int titleMaxW = Math.max(26, ((rightX - titleX) - 6));
        String title = trimToWidth(panel.category.getDisplayName(), titleMaxW);
        drawText(title, titleX, titleY, alpha(TXT_ON, alphaMul));
        drawText(right, rightX, rightY, alpha(blendColor(TXT_OFF, panelAccentSoft, 0.35f), alphaMul));
        if (bodyVisible <= 2) return;
        if ((panel.expandAnim < 0.07f) || (bodyVisible < 8)) {
            return;
        }
        float bodyAlphaMul = (alphaMul * clamp01(((panel.expandAnim - 0.06f) / 0.94f)));
        if (bodyAlphaMul <= 0.01f) return;
        int bodyTop = ((panel.y + 24) + 2);
        int bodyBottom = (bodyTop + bodyVisible);
        beginClip((panel.x + 3), bodyTop, ((panel.x + 156) - 3), bodyBottom);
        int rowY = ((bodyTop + 2) - Math.round(panel.scrollAnim));
        int moduleIndex = 0;
        while (moduleIndex < visibleModules.size()) {
            Module module = ((Module) visibleModules.get(moduleIndex));
            int rowX = (panel.x + 6);
            int rowW = 144;
            int rowH = 19;
            boolean roundTop = moduleIndex == 0;
            boolean roundBottom = moduleIndex == (visibleModules.size() - 1);
            if ((rowY < bodyTop) || ((rowY + rowH) > bodyBottom)) break label_79;
            if (((mx < rowX) || (mx > (rowX + rowW))) || (my < rowY)) break label_53;
            boolean hover = my <= (rowY + rowH);
            float mt = toggleProgress(("module:" + module.getName()), module.isEnabled());
            int rowColor = alpha(ROW_BG, bodyAlphaMul);
            if (mt > 0.03f) {
                rowColor = blendColor(rowColor, alpha(panelAccent, bodyAlphaMul), (0.1f + (0.2f * clamp01(mt))));
            }
            if (!(!hover)) {
                rowColor = blendColor(rowColor, alpha(ROW_HOVER, bodyAlphaMul), 0.55f);
            }
            if (module == this.selectedModule) {
                rowColor = blendColor(rowColor, alpha(panelAccent, bodyAlphaMul), 0.22f);
            }
            int rowAlpha = Math.max(0, Math.min(255, Math.round((246.0f * clamp01(bodyAlphaMul)))));
            rowColor = ((rowAlpha << 24) | (rowColor & 16777215));
            fillSelectiveRoundedRect(rowX, rowY, rowW, rowH, rowColor, roundTop, roundBottom);
            drawModuleStatusDot(((rowX + rowW) - 10), (rowY + (rowH / 2)), mt, bodyAlphaMul, panelAccent);
            int labelColor = alpha(blendColor(TXT_ON, panelAccentSoft, mt), bodyAlphaMul);
            String name = trimToWidth(module.getName(), (rowW - 22));
            drawText(name, (rowX + 5), (rowY + 5), labelColor);
            if (!hover) break label_79;
            String desc = module.getDescription();
            if (desc == null) break label_79;
            String normalized = desc.trim();
            if (normalized.isEmpty()) break label_79;
            this.hoveredModule = module;
            this.hoveredModuleDescription = normalized;
            this.hoveredModuleMouseX = mx;
            this.hoveredModuleMouseY = my;
            label_79:
            rowY += 19;
            float expand = easeOutCubic(getInlineExpandProgress(module));
            if (expand > 0.001f) {
                int fullInlineH = getInlineSettingsHeight(module);
                int inlineH = Math.max(1, Math.round((((float) fullInlineH) * expand)));
                rowY = (rowY + renderInlineSettings(panel, module, rowX, rowY, rowW, mx, my, bodyTop, bodyBottom, bodyAlphaMul, inlineH, expand));
            }
            moduleIndex++;
        }
        endClip();
        break label_91;
        Object var43 = "+";
        endClip();
        throw var43;
        label_91:
    }

    private List getFilteredModules(ClickGUI.CategoryPanel panel) {
        if (this.searchQuery.isEmpty()) {
            List visible = new ArrayList();
            String q = panel.modules.iterator();
            while (q.hasNext()) {
                Module module = ((Module) q.next());
                if (module.isVisibleInClickGui()) {
                    visible.add(module);
                }
            }
            return visible;
        }
        List filtered = new ArrayList();
        q = this.searchQuery.toLowerCase(Locale.ROOT);
        module = panel.modules.iterator();
        while (module.hasNext()) {
            module = ((Module) module.next());
            if (!(module.isVisibleInClickGui())) {
                break label_11;
            }
            if (module.getName().toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(module);
            }
        }
        return filtered;
    }

    private int getPanelVisibleBodyHeight(ClickGUI.CategoryPanel panel) {
        return getPanelVisibleBodyHeight(panel, getFilteredModules(panel));
    }

    private int getPanelVisibleBodyHeight(ClickGUI.CategoryPanel panel, List visibleModules) {
        if (panel.expandAnim <= 0.06f) return 0;
        int content = getPanelContentHeight(panel, visibleModules);
        int limit = panelBodyMaxHeight();
        int maxVisible = Math.max(0, Math.min(content, limit));
        float t = easeOutCubic(panel.expandAnim);
        return Math.max(0, Math.round((((float) maxVisible) * t)));
    }

    private int getPanelMaxScroll(ClickGUI.CategoryPanel panel) {
        return getPanelMaxScroll(panel, getFilteredModules(panel), getPanelVisibleBodyHeight(panel));
    }

    private int getPanelMaxScroll(ClickGUI.CategoryPanel panel, List visibleModules, int visibleBodyHeight) {
        int content = getPanelContentHeight(panel, visibleModules);
        if (panel.expandAnim <= 0.001f) return 0;
        return Math.max(0, (content - visibleBodyHeight));
    }

    private int getPanelHeight(ClickGUI.CategoryPanel panel) {
        return ((24 + getPanelVisibleBodyHeight(panel)) + 6);
    }

    private int getPanelContentHeight(ClickGUI.CategoryPanel panel, List visibleModules) {
        int content = 4;
        Object var4 = visibleModules.iterator();
        while (var4.hasNext()) {
            Module module = ((Module) var4.next());
            content += 19;
            float expand = easeOutCubic(getInlineExpandProgress(module));
            if (expand > 0.001f) {
                content = (content + Math.max(1, Math.round((((float) getInlineSettingsHeight(module)) * expand))));
            }
        }
        return content;
    }

    private int getInlineSettingsHeight(Module module) {
        int rowCount = (getVisibleSettings(module).size() + 2);
        int rowsH = ((rowCount * 22) + 6);
        return (((18 + rowsH) + 3) + 4);
    }

    private int renderInlineSettings(ClickGUI.CategoryPanel ownerPanel, Module module, int x, int y, int w, int mx, int my, int bodyTop, int bodyBottom, float alphaMul, int inlineH, float expand) {
        if (inlineH <= 9) return inlineH;
        int panelAccent = categoryAccent(ownerPanel.category);
        int panelAccentSoft = blendColor(panelAccent, -1, 0.28f);
        int blockX = (x + 2);
        int blockY = (y + 3);
        int blockW = (w - 4);
        int blockH = Math.max(1, (inlineH - 3));
        if (((blockY + blockH) <= bodyTop) || (blockY >= bodyBottom)) break label_45;
        int bg = alpha(blendColor(ROW_BG, SEARCH_BG, 0.35f), alphaMul);
        int blockAlpha = Math.max(0, Math.min(255, Math.round((246.0f * clamp01(alphaMul)))));
        bg = ((blockAlpha << 24) | (bg & 16777215));
        rrSmallNano(blockX, blockY, blockW, blockH, bg);
        beginClip(blockX, blockY, (blockX + blockW), (blockY + blockH));
        String title = "Modification";
        drawText(title, (blockX + 6), (blockY + 5), alpha(panelAccentSoft, alphaMul));
        String right = (module.isEnabled() ? "ON" : "OFF");
        int rw = nanoTextWidth(right);
        if (module.isEnabled()) {
        } else {
        }
        int rightColor = alpha(TXT_OFF, alphaMul);
        drawText(right, (((blockX + blockW) - rw) - 8), (blockY + 5), rightColor);
        float contentAlpha = (alphaMul * clamp01(((expand - 0.06f) / 0.94f)));
        if (this.selectedModule != module) break label_25;
        boolean interactive = expand > 0.78f;
        if (this.selectedModule == module && contentAlpha > 0.01f) {
            int rowY = (blockY + 18);
            int rowX = (blockX + 4);
            int rowW = (blockW - 8);
            List visibleSettings = getVisibleSettings(module);
            Object var31 = visibleSettings.iterator();
            while (var31.hasNext()) {
                Setting setting = ((Setting) var31.next());
                renderSettingRow(setting, rowX, rowY, rowW, mx, my, contentAlpha, interactive, panelAccent, panelAccentSoft);
                rowY += 22;
            }
            renderKeybindRow(rowX, rowY, rowW, mx, my, contentAlpha, interactive, panelAccent, panelAccentSoft);
            rowY += 22;
            renderResetRow(rowX, rowY, rowW, mx, my, contentAlpha, interactive, panelAccent, panelAccentSoft);
        }
        endClip();
        break label_45;
        Object var33 = alpha(panelAccent, alphaMul);
        endClip();
        throw var33;
        label_45:
        return inlineH;
    }

    private void renderSettingRow(Setting setting, int x, int y, int w, int mx, int my, float alphaMul, boolean interactive, int panelAccent, int panelAccentSoft) {
        int h = 20;
        if (((mx < x) || (mx > (x + w))) || (my < y)) break label_2;
        boolean hover = my <= (y + h);
        if ((setting instanceof IntSetting)) break label_5;
        if (!((setting instanceof DoubleSetting))) break label_6;
        label_5:
        break label_7;
        label_6:
        label_7:
        boolean sliderSetting = false;
        if (!sliderSetting) break label_10;
        break label_11;
        label_10:
        label_11:
        int controlArea = Math.max(56, Math.min(112, ((w / 2) + 10)));
        int labelMaxW = Math.max(24, ((w - controlArea) - 10));
        int controlMaxX = ((x + w) - 6);
        rrSmallNano(x, y, w, h, alpha(ROW_BG, alphaMul));
        if (!(!hover)) {
            rrSmallNano(x, y, w, h, alpha(ROW_HOVER, alphaMul));
        }
        String label = trimToWidth(setting.getName(), labelMaxW);
        drawText(label, (x + 6), (y + 7), alpha(TXT_OFF, alphaMul));
        if ((setting instanceof BoolSetting)) {
            BoolSetting bs = ((BoolSetting) setting);
            int px = (((x + w) - 16) - 8);
            int py = (y + ((h - 9) / 2));
            pill(px, py, ((Boolean) bs.get()).booleanValue(), ("set:" + this.selectedModule.getName() + ":" + setting.getName()), panelAccent);
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.BOOL, setting, x, y, w, h));
            }
            return;
        }
        if ((setting instanceof IntSetting)) {
            IntSetting is = ((IntSetting) setting);
            if (this.editingIntSetting == is) {
            } else {
            }
            String value = String.valueOf(is.get());
            int valueW = nanoTextWidth(value);
            int boxW = Math.max(40, (valueW + 10));
            int boxX = (controlMaxX - boxW);
            int boxY = (y + 4);
            int boxH = (h - 8);
            if (this.editingIntSetting == is) {
            } else {
            }
            int boxCol = alpha(SEARCH_BG, alphaMul);
            rrSmallNano(boxX, boxY, boxW, boxH, boxCol);
            drawText(value, (boxX + 4), (y + 7), alpha(panelAccentSoft, alphaMul));
            int sliderLabelMaxW = Math.max(24, ((w - boxW) - 16));
            drawText(trimToWidth(setting.getName(), sliderLabelMaxW), (x + 6), (y + 7), alpha(TXT_OFF, alphaMul));
            int sliderX = (x + 8);
            int sliderW = (w - 16);
            float p = (((float) (((Integer) is.get()).intValue() - is.getMin())) / ((float) Math.max(1, (is.getMax() - is.getMin()))));
            int fillW = Math.max(3, Math.round((((float) sliderW) * p)));
            rrSmallNano(sliderX, ((y + h) - 5), sliderW, 3, alpha(ROW_BORDER, alphaMul));
            rrSmallNano(sliderX, ((y + h) - 5), fillW, 3, alpha(panelAccent, alphaMul));
            rrPill(((sliderX + fillW) - 2), ((y + h) - 8), 5, 8, alpha(TXT_ON, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.sliderWithValue(ClickGUI.HitType.INT, setting, x, y, w, h, sliderX, sliderW, boxX, boxY, boxW, boxH));
            }
            return;
        }
        if ((setting instanceof DoubleSetting)) {
            DoubleSetting ds = ((DoubleSetting) setting);
            if (this.editingDoubleSetting == ds) {
            } else {
                var tmp0 = new Object[1];
                tmp0[0] = ds.get();
            }
            value = String.format(Locale.US, "%.2f", tmp0);
            valueW = nanoTextWidth(value);
            boxW = Math.max(46, (valueW + 10));
            boxX = (controlMaxX - boxW);
            boxY = (y + 4);
            boxH = (h - 8);
            if (this.editingDoubleSetting == ds) {
            } else {
            }
            boxCol = alpha(SEARCH_BG, alphaMul);
            rrSmallNano(boxX, boxY, boxW, boxH, boxCol);
            drawText(value, (boxX + 4), (y + 7), alpha(panelAccentSoft, alphaMul));
            sliderLabelMaxW = Math.max(24, ((w - boxW) - 16));
            drawText(trimToWidth(setting.getName(), sliderLabelMaxW), (x + 6), (y + 7), alpha(TXT_OFF, alphaMul));
            sliderX = (x + 8);
            sliderW = (w - 16);
            double denom = Math.max(1.0E-4, (ds.getMax() - ds.getMin()));
            p = ((float) ((((Double) ds.get()).doubleValue() - ds.getMin()) / denom));
            fillW = Math.max(3, Math.round((((float) sliderW) * p)));
            rrSmallNano(sliderX, ((y + h) - 5), sliderW, 3, alpha(ROW_BORDER, alphaMul));
            rrSmallNano(sliderX, ((y + h) - 5), fillW, 3, alpha(panelAccent, alphaMul));
            rrPill(((sliderX + fillW) - 2), ((y + h) - 8), 5, 8, alpha(TXT_ON, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.sliderWithValue(ClickGUI.HitType.DOUBLE, setting, x, y, w, h, sliderX, sliderW, boxX, boxY, boxW, boxH));
            }
            return;
        }
        if ((setting instanceof EnumSetting)) {
            EnumSetting es = ((EnumSetting) setting);
            value = ((Enum) es.get()).name();
            boxW = Math.min(controlArea, Math.max(48, (nanoTextWidth(value) + 16)));
            boxX = (controlMaxX - boxW);
            rrSmallNano(boxX, (y + 4), boxW, (h - 8), alpha(SEARCH_BG, alphaMul));
            drawText(trimToWidth(value, (boxW - 16)), (boxX + 6), (y + 7), alpha(TXT_ON, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.ENUM, setting, x, y, w, h));
            }
            return;
        }
        if ((setting instanceof StringSetting)) {
            StringSetting ss = ((StringSetting) setting);
            if (this.editingStringSetting == ss) {
            } else {
            }
            value = ((String) ss.get());
            if (value == null) {
                value = "";
            }
            boxW = Math.min((controlArea + 8), Math.max(54, (nanoTextWidth(value) + 12)));
            boxX = (controlMaxX - boxW);
            if (this.editingStringSetting == ss) {
            } else {
            }
            boxCol = alpha(SEARCH_BG, alphaMul);
            rrSmallNano(boxX, (y + 4), boxW, (h - 8), boxCol);
            int txtCol = (this.editingStringSetting == ss ? panelAccentSoft : ClickGUI.TXT_ON);
            drawText(trimToWidth(value, (boxW - 8)), (boxX + 4), (y + 7), alpha(txtCol, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.STRING, setting, x, y, w, h));
            }
            return;
        }
        if ((setting instanceof ColorSetting)) {
            ColorSetting cs = ((ColorSetting) setting);
            int swX = (controlMaxX - 14);
            rrSmallNano(swX, (y + 5), 14, 14, alpha(((Integer) cs.get()).intValue(), alphaMul));
            var tmp1 = new Object[1];
            tmp1[0] = (((Integer) cs.get()).intValue() & 16777215);
            String hex = ("#" + String.format("%06X", tmp1));
            drawText(trimToWidth(hex, (controlArea - 24)), Math.max(((x + labelMaxW) + 8), (swX - (controlArea - 24))), (y + 7), alpha(TXT_OFF, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.COLOR, setting, x, y, w, h));
            }
            return;
        }
        if ((setting instanceof ButtonSetting)) {
            String text = setting.getName();
            int bw = Math.min(controlArea, Math.max(50, (nanoTextWidth(text) + 16)));
            int bx = (controlMaxX - bw);
            rrSmallNano(bx, (y + 4), bw, (h - 8), alpha(2000362781, alphaMul));
            drawText(trimToWidth(text, (bw - 10)), (bx + 5), (y + 7), alpha(-29556, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.BUTTON, setting, x, y, w, h));
            }
            return;
        }
        if ((setting instanceof KeybindSetting)) {
            KeybindSetting ks = ((KeybindSetting) setting);
            value = ks.getKeyName();
            bw = Math.min(controlArea, Math.max(50, (nanoTextWidth(value) + 16)));
            bx = (controlMaxX - bw);
            rrSmallNano(bx, (y + 4), bw, (h - 8), alpha(SEARCH_BG, alphaMul));
            drawText(trimToWidth(value, (bw - 10)), (bx + 5), (y + 7), alpha(TXT_ON, alphaMul));
            if (!(!interactive)) {
                this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.KEYBIND, setting, x, y, w, h));
            }
            return;
        }
        value = String.valueOf(setting.get());
        drawText(trimToWidth(value, controlArea), (controlMaxX - controlArea), (y + 7), alpha(TXT_ON, alphaMul));
    }

    private void renderKeybindRow(int x, int y, int w, int mx, int my, float alphaMul, boolean interactive, int panelAccent, int panelAccentSoft) {
        int h = 20;
        if (((mx < x) || (mx > (x + w))) || (my < y)) break label_2;
        boolean hover = my <= (y + h);
        rrSmallNano(x, y, w, h, alpha(ROW_BG, alphaMul));
        if (!(!hover)) {
            rrSmallNano(x, y, w, h, alpha(ROW_HOVER, alphaMul));
        }
        drawText("Keybind", (x + 6), (y + 7), alpha(TXT_OFF, alphaMul));
        if (this.listeningKeybind != null) {
            String value = "Press key...";
        } else {
            value = ("[" + this.selectedModule.getKeybind().getKeyName() + "]");
        }
        int valueW = nanoTextWidth(value);
        int valueCol = (this.listeningKeybind != null ? panelAccentSoft : ClickGUI.TXT_ON);
        drawText(value, (((x + w) - valueW) - 6), (y + 7), alpha(valueCol, alphaMul));
        if (!(!interactive)) {
            this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.KEYBIND, null, x, y, w, h));
        }
    }

    private void renderResetRow(int x, int y, int w, int mx, int my, float alphaMul, boolean interactive, int panelAccent, int panelAccentSoft) {
        int h = 20;
        if (((mx < x) || (mx > (x + w))) || (my < y)) break label_2;
        boolean hover = my <= (y + h);
        int base = blendColor(1714428442, panelAccent, 0.2f);
        int hov = blendColor(-1908793826, panelAccent, 0.26f);
        if (!(!hover)) {
        } else {
        }
        int bg = alpha(base, alphaMul);
        rrSmallNano(x, y, w, h, bg);
        String label = "Reset Module";
        int tw = nanoTextWidth(label);
        drawText(label, (x + ((w - tw) / 2)), (y + 7), alpha(blendColor(-30070, panelAccentSoft, 0.35f), alphaMul));
        if (!(!interactive)) {
            this.settingHitBoxes.add(ClickGUI.SettingHitBox.of(ClickGUI.HitType.RESET, null, x, y, w, h));
        }
    }

    private void renderColorPicker(int mx, int my, float alphaMul) {
        if (this.colorPickerSetting == null) return;
        rrNano(this.colorPickerX, this.colorPickerY, this.colorPickerW, this.colorPickerH, alpha(SEARCH_BG, alphaMul));
        drawText("Color Picker", (this.colorPickerX + 8), (this.colorPickerY + 6), alpha(ACCENT, alphaMul));
        int svX = (this.colorPickerX + 8);
        int svY = (this.colorPickerY + 20);
        int hueX = ((svX + 92) + 8);
        int hueY = svY;
        int infoX = ((hueX + 12) + 8);
        renderSvSquare(svX, svY, 92, alphaMul);
        renderHueBar(hueX, hueY, 12, 92, alphaMul);
        int sx = (svX + Math.round((this.pickSat * 91.0f)));
        int sy = (svY + Math.round(((1.0f - this.pickVal) * 91.0f)));
        drawCrosshair(sx, sy, alphaMul);
        int hy = (hueY + Math.round((this.pickHue * 91.0f)));
        TessellatorRenderer.fillRectSharp((hueX - 2), (hy - 1), 16, 2, alpha(-1, alphaMul));
        int rgb = (((this.pickR << 16) | (this.pickG << 8)) | this.pickB);
        int preview = (-16777216 | (rgb & 16777215));
        rrNano(infoX, svY, 24, 16, alpha(preview, alphaMul));
        drawText(("H " + Math.round((this.pickHue * 360.0f))), infoX, (svY + 22), alpha(TXT_OFF, alphaMul));
        drawText(("S " + Math.round((this.pickSat * 100.0f)) + "%"), infoX, (svY + 34), alpha(TXT_OFF, alphaMul));
        drawText(("V " + Math.round((this.pickVal * 100.0f)) + "%"), infoX, (svY + 46), alpha(TXT_OFF, alphaMul));
        var tmp0 = new Object[1];
        tmp0[0] = rgb;
        drawText(("#" + String.format("%06X", tmp0)), infoX, (svY + 58), alpha(TXT_ON, alphaMul));
        int rgbX = svX;
        int rgbY = ((svY + 92) + 6);
        int rgbW = 112;
        renderRgbBar(rgbX, rgbY, rgbW, 5, this.pickR, -45747, "R", alphaMul);
        renderRgbBar(rgbX, ((rgbY + 5) + 5), rgbW, 5, this.pickG, -11216533, "G", alphaMul);
        renderRgbBar(rgbX, (rgbY + 20), rgbW, 5, this.pickB, -11694593, "B", alphaMul);
    }

    private void renderSvSquare(int x, int y, int size, float alphaMul) {
        int steps = 24;
        int iy = 0;
        while (iy < steps) {
            float v0 = (1.0f - (((float) iy) / ((float) steps)));
            float v1 = (1.0f - (((float) (iy + 1)) / ((float) steps)));
            int y0 = (y + ((iy * size) / steps));
            int y1 = (y + (((iy + 1) * size) / steps));
            int h = Math.max(1, (y1 - y0));
            int ix = 0;
            while (ix < steps) {
                float s0 = (((float) ix) / ((float) steps));
                int x0 = (x + ((ix * size) / steps));
                int x1 = (x + (((ix + 1) * size) / steps));
                int w = Math.max(1, (x1 - x0));
                int c = (-16777216 | hsvToRgb(this.pickHue, s0, ((v0 + v1) * 0.5f)));
                TessellatorRenderer.fillRectSharp(x0, y0, w, h, alpha(c, alphaMul));
                ix++;
            }
            iy++;
        }
    }

    private void renderHueBar(int x, int y, int w, int h, float alphaMul) {
        int steps = 48;
        int i = 0;
        while (i < steps) {
            float h0 = (((float) i) / ((float) steps));
            int y0 = (y + ((i * h) / steps));
            int y1 = (y + (((i + 1) * h) / steps));
            int segH = Math.max(1, (y1 - y0));
            int c = (-16777216 | hsvToRgb(h0, 1.0f, 1.0f));
            TessellatorRenderer.fillRectSharp(x, y0, w, segH, alpha(c, alphaMul));
            i++;
        }
    }

    private void drawCrosshair(int x, int y, float alphaMul) {
        int c = alpha(-1, alphaMul);
        TessellatorRenderer.fillRectSharp((x - 3), y, 7, 1, c);
        TessellatorRenderer.fillRectSharp(x, (y - 3), 1, 7, c);
        TessellatorRenderer.fillRectSharp((x - 2), (y - 2), 5, 5, alpha(-1879048192, alphaMul));
        TessellatorRenderer.fillRectSharp((x - 2), y, 5, 1, c);
        TessellatorRenderer.fillRectSharp(x, (y - 2), 1, 5, c);
    }

    private void renderRgbBar(int x, int y, int w, int h, int value, int barColor, String label, float alphaMul) {
        int track = blendColor(SEARCH_BG, ROW_BG, 0.45f);
        TessellatorRenderer.fillRoundedRect(x, y, w, h, 2.0f, alpha(track, alphaMul));
        int fillW = Math.round(((((float) clamp(value, 0, 255)) / 255.0f) * ((float) Math.max(1, (w - 1)))));
        if (fillW > 0) {
            TessellatorRenderer.fillRoundedRect(x, y, fillW, h, 2.0f, alpha(barColor, alphaMul));
        }
        int knobX = (x + fillW);
        TessellatorRenderer.fillRectSharp((knobX - 1), (y - 1), 2, (h + 2), alpha(-1, alphaMul));
        drawText((label + " " + value), ((x + w) + 6), (y - 1), alpha(TXT_OFF, alphaMul));
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        updateUiScale();
        int mx = toUiCoord(click.comp_4798());
        int my = toUiCoord(click.comp_4799());
        int button = click.method_74245();
        if (this.listeningKeybind != null) {
            this.listeningKeybind.set((1000 + button));
            this.listeningKeybind = null;
            return true;
        }
        if (this.editingStringSetting != null && button == 0) {
            finishStringEdit(true);
        }
        if (this.editingIntSetting != null && button == 0) {
            finishIntEdit(true);
        }
        if (this.editingDoubleSetting != null && button == 0) {
            finishDoubleEdit(true);
        }
        if (this.colorPickerSetting != null) {
            if (handleColorPickerClick(mx, my, button)) return true;
            closeColorPicker();
            return true;
        }
        if (inRect(mx, my, this.searchBarX, this.searchBarY, this.searchBarW, 30)) {
            this.searchFocused = true;
            return true;
        }
        if (!(!this.searchFocused)) {
            this.searchFocused = false;
        }
        ClickGUI.CategoryPanel hoveredPanel = findTopPanelAt(mx, my);
        ClickGUI.CategoryPanel selectedPanel = findPanelForModule(this.selectedModule);
        if ((selectedPanel == null) || (!(inRect(mx, my, selectedPanel.x, selectedPanel.y, 156, getPanelHeight(selectedPanel))))) break label_33;
        if (handleSettingsClick(mx, my, button)) {
            bringPanelToFront(selectedPanel);
            return true;
        }
        if (!(handlePanelClick(selectedPanel, mx, my, button))) break label_33;
        bringPanelToFront(selectedPanel);
        return true;
        label_33:
        if (hoveredPanel != null) {
            if (hoveredPanel != selectedPanel) break label_37;
        }
        if (!(handleSettingsClick(mx, my, button))) break label_37;
        bringPanelToFront(selectedPanel);
        return true;
        label_37:
        if (hoveredPanel != null) {
            if (handlePanelClick(hoveredPanel, mx, my, button)) {
                bringPanelToFront(hoveredPanel);
                return true;
            }
            return true;
        }
        return super.method_25402(click, doubled);
    }

    private boolean handlePanelClick(ClickGUI.CategoryPanel panel, int mx, int my, int button) {
        if (inRect(mx, my, panel.x, panel.y, 156, 24)) {
            if (button == 0) {
                bringPanelToFront(panel);
                this.draggingPanel = panel;
                this.headerClickPanel = panel;
                this.dragOffsetX = (mx - panel.x);
                this.dragOffsetY = (my - panel.y);
                this.dragMoved = false;
                break label_17;
            }
            if (button != 1) break label_17;
            bringPanelToFront(panel);
            panel.collapsed = !panel.collapsed;
            panel.scroll = 0;
            panel.scrollAnim = 0.0f;
            persistCurrentLayout();
            requestLayoutSave();
            label_17:
            return true;
        }
        if (!(!panel.collapsed)) return true;
        List visible = getFilteredModules(panel);
        int bodyTop = ((panel.y + 24) + 4);
        int rowY = (bodyTop - Math.round(panel.scrollAnim));
        Object var8 = visible.iterator();
        while (var8.hasNext()) {
            Module module = ((Module) var8.next());
            int rowX = (panel.x + 6);
            int rowW = 144;
            if (inRect(mx, my, rowX, rowY, rowW, 19)) {
                bringPanelToFront(panel);
                if (button == 0) {
                    module.toggle();
                    break label_40;
                }
                if (button == 1) {
                    if (this.selectedModule == module) {
                        this.selectedModule = null;
                        closeColorPicker();
                    } else {
                        this.selectedModule = module;
                    }
                    this.editingStringSetting = null;
                    this.editingIntSetting = null;
                    this.listeningKeybind = null;
                    break label_40;
                }
                if (button != 2) break label_40;
                module.getResetButton().press();
                label_40:
                return true;
            }
            rowY += 19;
            float expand = easeOutCubic(getInlineExpandProgress(module));
            if (expand > 0.001f) {
                int expandedH = Math.max(1, Math.round((((float) getInlineSettingsHeight(module)) * expand)));
                int hitH = Math.max(1, (expandedH - 3));
                if (inRect(mx, my, (rowX + 2), (rowY + 3), (rowW - 4), hitH)) {
                    bringPanelToFront(panel);
                    return true;
                }
                rowY = (rowY + expandedH);
            }
        }
        return true;
    }

    private boolean handleSettingsClick(int mx, int my, int button) {
        if (this.selectedModule == null) return false;
        int i = (this.settingHitBoxes.size() - 1);
        while (i >= 0) {
            ClickGUI.SettingHitBox hit = ((ClickGUI.SettingHitBox) this.settingHitBoxes.get(i));
            if (inRect(mx, my, hit.x, hit.y, hit.w, hit.h)) break label_5;
            break label_70;
            label_5:
            bringPanelToFront(findPanelForModule(this.selectedModule));
            switch (hit.type.ordinal()) {
                case 0 -> { break label_7; }
                case 1 -> { break label_10; }
                case 2 -> { break label_25; }
                case 3 -> { break label_40; }
                case 4 -> { break label_44; }
                case 5 -> { break label_48; }
                case 6 -> { break label_52; }
                case 7 -> { break label_56; }
                case 8 -> { break label_67; }
                default -> { break label_70; }
            }
            label_7:
            if (button != 0) break label_9;
            boolean sliderIntent = hit.setting;
            if (!((sliderIntent instanceof BoolSetting))) break label_9;
            BoolSetting bs = ((BoolSetting) sliderIntent);
            bs.toggle();
            label_9:
            return true;
            label_10:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof IntSetting))) break label_24;
            IntSetting is = ((IntSetting) sliderIntent);
            if (button == 1) {
                beginIntEdit(is);
                break label_24;
            }
            if (button != 0) break label_24;
            sliderIntent = my >= ((hit.y + hit.h) - 9);
            if (!(hit.inValueArea(mx, my))) {
                if (!sliderIntent) {
                }
                beginIntEdit(is);
                break label_24;
            }
            this.draggingIntSetting = is;
            this.draggingDoubleSetting = null;
            this.draggingSliderX = hit.sliderX;
            this.draggingSliderW = hit.sliderW;
            updateDraggingInt(mx);
            label_24:
            return true;
            label_25:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof DoubleSetting))) break label_39;
            DoubleSetting ds = ((DoubleSetting) sliderIntent);
            if (button == 1) {
                beginDoubleEdit(ds);
                break label_39;
            }
            if (button != 0) break label_39;
            sliderIntent = my >= ((hit.y + hit.h) - 9);
            if (!(hit.inValueArea(mx, my))) {
                if (!sliderIntent) {
                }
                beginDoubleEdit(ds);
                break label_39;
            }
            this.draggingDoubleSetting = ds;
            this.draggingIntSetting = null;
            this.draggingSliderX = hit.sliderX;
            this.draggingSliderW = hit.sliderW;
            updateDraggingDouble(mx);
            label_39:
            return true;
            label_40:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof EnumSetting))) break label_43;
            EnumSetting es = ((EnumSetting) sliderIntent);
            if (button == 1) {
                es.cycle(true);
                break label_43;
            }
            es.cycle(false);
            label_43:
            return true;
            label_44:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof StringSetting))) break label_47;
            StringSetting ss = ((StringSetting) sliderIntent);
            if (button != 0) break label_47;
            beginStringEdit(ss);
            label_47:
            return true;
            label_48:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof ColorSetting))) break label_51;
            ColorSetting cs = ((ColorSetting) sliderIntent);
            if (button != 0) break label_51;
            openColorPicker(cs, ((hit.x + hit.w) + 6), (hit.y - 4));
            label_51:
            return true;
            label_52:
            sliderIntent = hit.setting;
            if (!((sliderIntent instanceof ButtonSetting))) break label_55;
            ButtonSetting btn = ((ButtonSetting) sliderIntent);
            if (button != 0) break label_55;
            btn.press();
            label_55:
            return true;
            label_56:
            sliderIntent = hit.setting;
            if ((sliderIntent instanceof KeybindSetting)) {
                KeybindSetting ks = ((KeybindSetting) sliderIntent);
                this.listeningKeybind = ks;
                this.editingStringSetting = null;
                this.editingIntSetting = null;
                this.editingDoubleSetting = null;
                break label_66;
            }
            if ((button != 0) || (this.selectedModule == null)) break label_66;
            this.listeningKeybind = this.selectedModule.getKeybind();
            this.editingStringSetting = null;
            this.editingIntSetting = null;
            this.editingDoubleSetting = null;
            label_66:
            return true;
            label_67:
            if (button == 0 && this.selectedModule != null) {
                this.selectedModule.getResetButton().press();
            }
            return true;
            label_70:
            i--;
        }
        return false;
    }

    public boolean method_25406(class_11909 click) {
        if (!(click.method_74245()) && this.draggingPanel != null) {
            if (!this.dragMoved && this.headerClickPanel == this.draggingPanel) {
                this.draggingPanel.collapsed = !this.draggingPanel.collapsed;
                this.draggingPanel.scroll = 0;
                this.draggingPanel.scrollAnim = 0.0f;
            }
            this.draggingPanel = null;
            this.headerClickPanel = null;
            persistCurrentLayout();
            requestLayoutSave();
        }
        this.draggingIntSetting = null;
        this.draggingDoubleSetting = null;
        this.pickDragSv = false;
        this.pickDragHue = false;
        this.pickDragRgb = -1;
        return super.method_25406(click);
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        updateUiScale();
        int mx = toUiCoord(click.comp_4798());
        int my = toUiCoord(click.comp_4799());
        if (this.colorPickerSetting == null) break label_6;
        if (!this.pickDragSv && !this.pickDragHue) {
            if (this.pickDragRgb == -1) break label_6;
        }
        updatePickerFromMouse(mx, my);
        return true;
        label_6:
        if (this.draggingIntSetting != null) {
            updateDraggingInt(mx);
            return true;
        }
        if (this.draggingDoubleSetting != null) {
            updateDraggingDouble(mx);
            return true;
        }
        if (this.draggingPanel != null) {
            bringPanelToFront(this.draggingPanel);
            int nx = clamp((mx - this.dragOffsetX), 12, getPanelsMaxX());
            int panelH = getPanelHeight(this.draggingPanel);
            int maxY = Math.max(44, ((this.searchBarY - panelH) - 6));
            int ny = clamp((my - this.dragOffsetY), 44, maxY);
            if (Math.abs((nx - this.draggingPanel.x)) <= 1) {
                if (Math.abs((ny - this.draggingPanel.y)) > 1) {
                }
                this.dragMoved = true;
            }
            this.draggingPanel.x = nx;
            this.draggingPanel.y = ny;
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    public boolean method_25401(double mx, double my, double h, double v) {
        updateUiScale();
        int uiMx = toUiCoord(mx);
        int uiMy = toUiCoord(my);
        if (Math.abs(v) < 1.0E-4) return true;
        int i = (this.panels.size() - 1);
        while (i >= 0) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) this.panels.get(i));
            if (!(inRect(uiMx, uiMy, panel.x, panel.y, 156, getPanelHeight(panel)))) {
            } else {
                bringPanelToFront(panel);
                int delta = ((int) Math.round((v * 19.0)));
                if (delta == 0) {
                    delta = (v > 0.0 ? 1 : -1);
                }
                panel.scroll = (panel.scroll - delta);
                panel.scroll = clamp(panel.scroll, 0, getPanelMaxScroll(panel));
                return true;
            }
            i--;
        }
        return super.method_25401(mx, my, h, v);
    }

    private ClickGUI.CategoryPanel findPanelForModule(Module module) {
        if (module == null) return null;
        Object var2 = this.panels.iterator();
        while (var2.hasNext()) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) var2.next());
            if (panel.modules.contains(module)) return panel;
        }
        return null;
    }

    private ClickGUI.CategoryPanel findTopPanelAt(int mx, int my) {
        int i = (this.panels.size() - 1);
        while (i >= 0) {
            ClickGUI.CategoryPanel panel = ((ClickGUI.CategoryPanel) this.panels.get(i));
            if (inRect(mx, my, panel.x, panel.y, 156, getPanelHeight(panel))) return panel;
            i--;
        }
        return null;
    }

    private void bringPanelToFront(ClickGUI.CategoryPanel panel) {
        if ((panel == null) || (this.panels.isEmpty())) {
            return;
        }
        this.lastInteractedPanel = panel;
        int idx = this.panels.indexOf(panel);
        if ((idx < 0) || (idx == (this.panels.size() - 1))) {
            return;
        }
        this.panels.remove(idx);
        this.panels.add(panel);
    }

    public boolean method_25404(class_11908 input) {
        int key = input.comp_4795();
        if (this.editingIntSetting != null) {
            if (key == 257) break label_3;
            if (key != 335) break label_5;
            label_3:
            finishIntEdit(true);
            return true;
            label_5:
            if (key == 256) {
                finishIntEdit(false);
                return true;
            }
            if (key == 259) {
                if (!(this.editingIntBuffer.isEmpty())) {
                    this.editingIntBuffer = this.editingIntBuffer.substring(0, (this.editingIntBuffer.length() - 1));
                }
                return true;
            }
            return true;
        }
        if (this.editingDoubleSetting != null) {
            if (key != 257) {
                if (key == 335) {
                }
                finishDoubleEdit(true);
                return true;
            }
            if (key == 256) {
                finishDoubleEdit(false);
                return true;
            }
            if (key == 259) {
                if (!(this.editingDoubleBuffer.isEmpty())) {
                    this.editingDoubleBuffer = this.editingDoubleBuffer.substring(0, (this.editingDoubleBuffer.length() - 1));
                }
                return true;
            }
            if (input.method_74243()) {
                if ((this.field_22787 == null) || (this.field_22787.field_1774 == null)) break label_29;
                String clip = this.field_22787.field_1774.method_1460();
                if ((clip == null) || (clip.isEmpty())) break label_29;
                this.editingDoubleBuffer = (this.editingDoubleBuffer + clip.replace(44, 46));
                label_29:
                return true;
            }
            return true;
        }
        if (this.editingStringSetting != null) {
            if (key != 257) {
                if (key == 335) {
                }
                finishStringEdit(true);
                return true;
            }
            if (key == 256) {
                finishStringEdit(false);
                return true;
            }
            if (key == 259) {
                if (!(this.editingStringBuffer.isEmpty())) {
                    this.editingStringBuffer = this.editingStringBuffer.substring(0, (this.editingStringBuffer.length() - 1));
                }
                return true;
            }
            if (input.method_74243()) {
                if ((this.field_22787 == null) || (this.field_22787.field_1774 == null)) break label_46;
                clip = this.field_22787.field_1774.method_1460();
                if ((clip == null) || (clip.isEmpty())) break label_46;
                this.editingStringBuffer = (this.editingStringBuffer + clip);
                label_46:
                return true;
            }
            return true;
        }
        if (this.listeningKeybind != null) {
            this.listeningKeybind.set((input.method_74231() ? -1 : key));
            this.listeningKeybind = null;
            return true;
        }
        if (key == 259 && !(!this.searchFocused) && !(this.searchQuery.isEmpty())) {
            this.searchQuery = this.searchQuery.substring(0, (this.searchQuery.length() - 1));
            return true;
        }
        if (key != 257) {
            if (key != 335) break label_61;
        }
        if (!this.searchFocused) break label_61;
        this.searchFocused = false;
        return true;
        label_61:
        if (key == 256) {
            if (this.colorPickerSetting != null) {
                closeColorPicker();
                return true;
            }
            if (!(!this.searchFocused) && !(this.searchQuery.isEmpty())) {
                this.searchQuery = "";
                return true;
            }
            if (!(!this.searchFocused)) {
                this.searchFocused = false;
                return true;
            }
            startClosing();
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        char c = ((char) input.comp_4793());
        if (this.editingIntSetting != null) {
            if (c >= 48) {
                if (c > 57) {
                }
                if (c == 45 && this.editingIntBuffer.isEmpty()) {
                }
                this.editingIntBuffer = (this.editingIntBuffer + c);
            }
            return true;
        }
        if (this.editingDoubleSetting != null) {
            if (c >= 48 && c <= 57) {
                this.editingDoubleBuffer = (this.editingDoubleBuffer + c);
                break label_16;
            }
            if (c == 45 && this.editingDoubleBuffer.isEmpty()) {
                this.editingDoubleBuffer = (this.editingDoubleBuffer + c);
                break label_16;
            }
            if (c != 46) {
                if (c != 44) break label_16;
            }
            if ((this.editingDoubleBuffer.contains(".")) || (this.editingDoubleBuffer.contains(","))) break label_16;
            this.editingDoubleBuffer = (this.editingDoubleBuffer + ".");
            label_16:
            return true;
        }
        if (this.editingStringSetting != null) {
            if (c >= 32 && c < 127) {
                this.editingStringBuffer = (this.editingStringBuffer + c);
            }
            return true;
        }
        if (this.listeningKeybind != null) return true;
        if (!(!this.searchFocused) && c >= 32 && c < 127) {
            this.searchQuery = (this.searchQuery + c);
            return true;
        }
        if (!this.searchFocused && c >= 32 && c < 127) {
            this.searchFocused = true;
            this.searchQuery = (this.searchQuery + c);
            return true;
        }
        return super.method_25400(input);
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }

    public void method_25419() {
        startClosing();
    }

    private void startClosing() {
        this.closing = true;
        this.searchFocused = false;
        this.draggingPanel = null;
        this.draggingIntSetting = null;
        this.draggingDoubleSetting = null;
        this.pickDragSv = false;
        this.pickDragHue = false;
        this.pickDragRgb = -1;
    }

    private void finishClose() {
        persistCurrentLayout();
        requestLayoutSave();
        if (this.field_22787 != null) {
            this.field_22787.method_1507(null);
        }
    }

    public void method_25432() {
        persistCurrentLayout();
        requestLayoutSave();
        super.method_25432();
    }

    private void beginStringEdit(StringSetting setting) {
        this.editingStringSetting = setting;
        if (setting.get() == null) {
        } else {
        }
        "".editingStringBuffer = ((String) setting.get());
        this.editingIntSetting = null;
        this.editingDoubleSetting = null;
        this.listeningKeybind = null;
        this.searchFocused = false;
    }

    private void finishStringEdit(boolean commit) {
        if (this.editingStringSetting == null) return;
        if (!(!commit)) {
            this.editingStringSetting.set(this.editingStringBuffer);
        }
        this.editingStringSetting = null;
        this.editingStringBuffer = "";
    }

    private void beginIntEdit(IntSetting setting) {
        this.editingIntSetting = setting;
        this.editingIntBuffer = String.valueOf(setting.get());
        this.editingDoubleSetting = null;
        this.editingStringSetting = null;
        this.listeningKeybind = null;
        this.searchFocused = false;
    }

    private void finishIntEdit(boolean commit) {
        if (this.editingIntSetting == null) return;
        if (((!commit) || (this.editingIntBuffer.isEmpty())) || ("-".equals(this.editingIntBuffer))) break label_6;
        try {
            int value = Integer.parseInt(this.editingIntBuffer);
            this.editingIntSetting.set(value);
        } catch (NumberFormatException ex5) {
            value = ex5;
        }
        label_6:
        this.editingIntSetting = null;
        this.editingIntBuffer = "";
    }

    private void beginDoubleEdit(DoubleSetting setting) {
        this.editingDoubleSetting = setting;
        var tmp0 = new Object[1];
        tmp0[0] = setting.get();
        this.editingDoubleBuffer = String.format(Locale.US, "%.2f", tmp0);
        this.editingIntSetting = null;
        this.editingStringSetting = null;
        this.listeningKeybind = null;
        this.searchFocused = false;
    }

    private void finishDoubleEdit(boolean commit) {
        if (this.editingDoubleSetting == null) return;
        if (((!commit) || (this.editingDoubleBuffer.isEmpty())) || ("-".equals(this.editingDoubleBuffer))) break label_6;
        try {
            double value = Double.parseDouble(this.editingDoubleBuffer.replace(44, 46));
            this.editingDoubleSetting.set(value);
        } catch (NumberFormatException ex5) {
            value = ex5;
        }
        label_6:
        this.editingDoubleSetting = null;
        this.editingDoubleBuffer = "";
    }

    private void updateDraggingInt(int mouseX) {
        if (this.draggingIntSetting == null) return;
        float p = clamp01((((float) (mouseX - this.draggingSliderX)) / ((float) Math.max(1, this.draggingSliderW))));
        int value = (this.draggingIntSetting.getMin() + Math.round((((float) (this.draggingIntSetting.getMax() - this.draggingIntSetting.getMin())) * p)));
        this.draggingIntSetting.set(value);
    }

    private void updateDraggingDouble(int mouseX) {
        if (this.draggingDoubleSetting == null) return;
        float p = clamp01((((float) (mouseX - this.draggingSliderX)) / ((float) Math.max(1, this.draggingSliderW))));
        double value = (this.draggingDoubleSetting.getMin() + ((this.draggingDoubleSetting.getMax() - this.draggingDoubleSetting.getMin()) * ((double) p)));
        this.draggingDoubleSetting.set(value);
    }

    private boolean handleColorPickerClick(int mx, int my, int button) {
        if (this.colorPickerSetting == null) return false;
        if (!(inRect(mx, my, this.colorPickerX, this.colorPickerY, this.colorPickerW, this.colorPickerH))) return false;
        if (button != 0) return true;
        int svX = (this.colorPickerX + 8);
        int svY = (this.colorPickerY + 20);
        int hueX = ((svX + 92) + 8);
        int hueY = svY;
        int rgbX = svX;
        int rgbY = ((svY + 92) + 6);
        int rgbW = 112;
        int rgbStride = 10;
        if (inRect(mx, my, svX, svY, 92, 92)) {
            this.pickDragSv = true;
            this.pickDragHue = false;
            this.pickDragRgb = -1;
            updatePickerFromMouse(mx, my);
            return true;
        }
        if (inRect(mx, my, hueX, hueY, 12, 92)) {
            this.pickDragHue = true;
            this.pickDragSv = false;
            this.pickDragRgb = -1;
            updatePickerFromMouse(mx, my);
            return true;
        }
        int i = 0;
        while (i < 3) {
            int y = (rgbY + (i * rgbStride));
            if (inRect(mx, my, rgbX, y, rgbW, 5)) {
                this.pickDragRgb = i;
                this.pickDragSv = false;
                this.pickDragHue = false;
                updatePickerFromMouse(mx, my);
                return true;
            }
            i++;
        }
        return true;
    }

    private void updatePickerFromMouse(int mx, int my) {
        if (this.colorPickerSetting == null) return;
        int svX = (this.colorPickerX + 8);
        int svY = (this.colorPickerY + 20);
        int hueY = svY;
        if (this.pickDragRgb != -1) {
            int rgbX = (this.colorPickerX + 8);
            int rgbW = 112;
            int value = clamp(Math.round((clamp01((((float) (mx - rgbX)) / ((float) Math.max(1, (rgbW - 1))))) * 255.0f)), 0, 255);
            if (!this.pickDragRgb) {
                this.pickR = value;
            }
            if (this.pickDragRgb == 1) {
                this.pickG = value;
            }
            if (this.pickDragRgb == 2) {
                this.pickB = value;
            }
            float[] hsv = rgbToHsv(this.pickR, this.pickG, this.pickB);
            this.pickHue = hsv[0];
            this.pickSat = hsv[1];
            this.pickVal = hsv[2];
        } else {
            if (!(!this.pickDragSv)) {
                this.pickSat = clamp01((((float) (mx - svX)) / ((float) Math.max(1, 91))));
                this.pickVal = (1.0f - clamp01((((float) (my - svY)) / ((float) Math.max(1, 91)))));
            }
            if (!(!this.pickDragHue)) {
                this.pickHue = clamp01((((float) (my - hueY)) / ((float) Math.max(1, 91))));
            }
            int rgb = hsvToRgb(this.pickHue, this.pickSat, this.pickVal);
            this.pickR = ((rgb >> 16) & 255);
            this.pickG = ((rgb >> 8) & 255);
            this.pickB = (rgb & 255);
        }
        this.colorPickerSetting.setRGBA(this.pickR, this.pickG, this.pickB, this.colorPickerSetting.getAlpha());
    }

    private void openColorPicker(ColorSetting setting, int x, int y) {
        this.colorPickerSetting = setting;
        float[] hsv = rgbToHsv(setting.getRed(), setting.getGreen(), setting.getBlue());
        this.pickHue = hsv[0];
        this.pickSat = hsv[1];
        this.pickVal = hsv[2];
        this.pickR = setting.getRed();
        this.pickG = setting.getGreen();
        this.pickB = setting.getBlue();
        this.pickDragSv = false;
        this.pickDragHue = false;
        this.pickDragRgb = -1;
        this.colorPickerX = clamp(x, 10, ((this.uiWidth - this.colorPickerW) - 10));
        this.colorPickerY = clamp(y, 44, ((this.uiHeight - this.colorPickerH) - 14));
    }

    private void closeColorPicker() {
        this.colorPickerSetting = null;
        this.pickDragSv = false;
        this.pickDragHue = false;
        this.pickDragRgb = -1;
    }

    private List getVisibleSettings(Module module) {
        List out = new ArrayList();
        Object var3 = module.getSettings().iterator();
        while (var3.hasNext()) {
            Setting setting = ((Setting) var3.next());
            if (setting.isVisibleInGui()) {
                out.add(setting);
            }
        }
        return out;
    }

    private void renderModuleTooltip(float alphaMul) {
        if ((this.hoveredModule == null) || (this.hoveredModuleDescription == null)) break label_1;
        if (!(this.hoveredModuleDescription.isBlank())) break label_2;
        label_1:
        return;
        label_2:
        if (this.activeDrawContext == null) return;
        int maxTextW = Math.max(140, Math.min(320, (this.uiWidth / 3)));
        List lines = wrapTextToWidth(this.hoveredModuleDescription, maxTextW, 3);
        if (lines.isEmpty()) return;
        int textW = 0;
        int padX = lines.iterator();
        while (padX.hasNext()) {
            String line = ((String) padX.next());
            textW = Math.max(textW, nanoTextWidth(line));
        }
        textW = Math.max(textW, nanoTextWidth(this.hoveredModule.getName()));
        padX = 8;
        int padY = 6;
        Objects.requireNonNull(this.field_22793);
        int titleH = 9;
        Objects.requireNonNull(this.field_22793);
        int lineH = (9 + 1);
        int boxW = (textW + (padX * 2));
        int boxH = ((((padY + titleH) + 3) + (lines.size() * lineH)) + padY);
        int x = (this.hoveredModuleMouseX + 12);
        int y = (this.hoveredModuleMouseY + 12);
        if ((x + boxW) > (this.uiWidth - 8)) {
            x = Math.max(8, ((this.hoveredModuleMouseX - boxW) - 10));
        }
        if ((y + boxH) > (this.uiHeight - 8)) {
            y = Math.max(8, ((this.uiHeight - boxH) - 8));
        }
        int bg = alpha(blendColor(BG, SEARCH_BG, 0.68f), alphaMul);
        int border = alpha(blendColor(ACCENT_SOFT, PANEL_BORDER, 0.45f), alphaMul);
        float tooltipRadius = Math.max(3.0f, Math.min((((float) Math.min(boxW, boxH)) * 0.22f), 6.0f));
        TessellatorRenderer.fillRoundedRect(this.activeDrawContext, x, y, boxW, boxH, tooltipRadius, bg);
        TessellatorRenderer.strokeRoundedRect(this.activeDrawContext, (((float) x) + 0.5f), (((float) y) + 0.5f), (((float) boxW) - 1.0f), (((float) boxH) - 1.0f), tooltipRadius, 1.0f, border);
        int ty = (y + padY);
        drawText(trimToWidth(this.hoveredModule.getName(), (boxW - (padX * 2))), (x + padX), ty, alpha(TXT_ON, alphaMul));
        ty = (ty + (titleH + 3));
        Object var17 = lines.iterator();
        while (var17.hasNext()) {
            line = ((String) var17.next());
            drawText(line, (x + padX), ty, alpha(TXT_OFF, alphaMul));
            ty = (ty + lineH);
        }
    }

    private List wrapTextToWidth(String text, int maxWidth, int maxLines) {
        List out = new ArrayList();
        if (((text == null) || (text.isBlank())) || (maxWidth <= 0)) break label_2;
        if (maxLines <= 0) {
            label_2:
            return out;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        Object var7 = words;
        int var8 = var7.length;
        int var9 = 0;
        while (var9 < var8) {
            String word = var7[var9];
            if (line.isEmpty()) {
            } else {
            }
            String candidate = (String.valueOf(line) + " " + word);
            if (nanoTextWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
                break label_22;
            }
            if (!(line.isEmpty())) {
                out.add(line.toString());
                if (out.size() >= maxLines) return out;
                line.setLength(0);
            }
            if (nanoTextWidth(word) <= maxWidth) {
                line.append(word);
                break label_22;
            }
            out.add(trimToWidth(word, maxWidth));
            if (out.size() >= maxLines) return out;
            label_22:
            var9++;
        }
        if (!(line.isEmpty()) && out.size() < maxLines) {
            out.add(line.toString());
        }
        return out;
    }

    private static void rrNano(int x, int y, int w, int h, int col) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int radius = Math.max(2, Math.min((Math.min(w, h) / 2), 4));
        TessellatorRenderer.fillRoundedRect(x, y, w, h, radius, col);
    }

    private static void rrSmallNano(int x, int y, int w, int h, int col) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int radius = Math.max(0, Math.min((Math.min(w, h) / 2), 3));
        TessellatorRenderer.fillRoundedRect(x, y, w, h, radius, col);
    }

    private static void rrPillNano(int x, int y, int w, int h, int col) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        TessellatorRenderer.fillRoundedRect(x, y, w, h, (h / 2), col);
    }

    private static void rrPill(int x, int y, int w, int h, int col) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        TessellatorRenderer.fillRoundedRect(x, y, w, h, (h / 2), col);
    }

    private static void drawPanelShell(int x, int y, int w, int h, boolean front, float alphaMul, int accentColor) {
        int tint = blendColor(accentColor, ACCENT_SOFT, 0.35f);
        if (!(!front)) {
        } else {
        }
        int base = blendColor(BG, tint, 0.06f);
        int rgb = (base & 16777215);
        int hardAlpha = 255;
        int main = alpha(((hardAlpha << 24) | rgb), alphaMul);
        rrNano(x, y, w, h, main);
    }

    private void drawModuleStatusDot(int cx, int cy, float t, float alphaMul, int accentColor) {
        float clamped = clamp01(t);
        int size = 4;
        int dotColor = alpha(blendColor(TXT_OFF, accentColor, clamped), alphaMul);
        rrPill((cx - (size / 2)), (cy - (size / 2)), size, size, dotColor);
    }

    private static void fillSelectiveRoundedRect(int x, int y, int w, int h, int color, boolean roundTop, boolean roundBottom) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        if (!roundTop && !roundBottom) {
            TessellatorRenderer.fillRectSharp(x, y, w, h, color);
            return;
        }
        int radius = Math.max(1, Math.min(4, (Math.min(w, h) / 2)));
        float r = ((float) Math.max(0, Math.min(radius, (Math.min(w, h) / 2))));
        float zero = 0.0f;
        float tl = (!(!roundTop) ? r : zero);
        float tr = (!(!roundTop) ? r : zero);
        float bl = (!(!roundBottom) ? r : zero);
        float br = (!(!roundBottom) ? r : zero);
        TessellatorRenderer.fillRoundedRectVarying(x, y, w, h, tl, tr, br, bl, color);
    }

    private void pill(int x, int y, boolean on, String key, int accentColor) {
        float t = toggleProgress(key, on);
        int onColor = blendColor(TGL_ON, accentColor, 0.68f);
        int bg = blendColor(TGL_OFF, onColor, t);
        rrPill(x, y, 16, 9, bg);
        int kx = ((x + 1) + Math.round((7.0f * t)));
        int ks = 7;
        rrPill(kx, (y + 1), ks, ks, -1);
    }

    private void beginClip(int x1, int y1, int x2, int y2) {
        if (this.activeDrawContext == null) return;
        int nx1 = Math.min(x1, x2);
        int ny1 = Math.min(y1, y2);
        int nx2 = Math.max(x1, x2);
        int ny2 = Math.max(y1, y2);
        if (!(this.clipStack.isEmpty())) {
            ClickGUI.ClipRect parent = ((ClickGUI.ClipRect) this.clipStack.peek());
            nx1 = Math.max(nx1, parent.x1);
            ny1 = Math.max(ny1, parent.y1);
            nx2 = Math.min(nx2, parent.x2);
            ny2 = Math.min(ny2, parent.y2);
        }
        if (nx2 <= nx1) {
            nx2 = (nx1 + 1);
        }
        if (ny2 <= ny1) {
            ny2 = (ny1 + 1);
        }
        ClickGUI.ClipRect clip = new ClickGUI.ClipRect(nx1, ny1, nx2, ny2);
        this.clipStack.push(clip);
        int sx1 = Math.round((((float) clip.x1) * this.uiScale));
        int sy1 = Math.round((((float) clip.y1) * this.uiScale));
        int sx2 = Math.round((((float) clip.x2) * this.uiScale));
        int sy2 = Math.round((((float) clip.y2) * this.uiScale));
        if (sx2 <= sx1) {
            sx2 = (sx1 + 1);
        }
        if (sy2 <= sy1) {
            sy2 = (sy1 + 1);
        }
        this.activeDrawContext.method_44379(sx1, sy1, sx2, sy2);
    }

    private void endClip() {
        if (this.activeDrawContext == null) return;
        if (this.clipStack.isEmpty()) return;
        this.clipStack.pop();
        this.activeDrawContext.method_44380();
        if (!(this.clipStack.isEmpty())) {
            ClickGUI.ClipRect clip = ((ClickGUI.ClipRect) this.clipStack.peek());
            int sx1 = Math.round((((float) clip.x1) * this.uiScale));
            int sy1 = Math.round((((float) clip.y1) * this.uiScale));
            int sx2 = Math.round((((float) clip.x2) * this.uiScale));
            int sy2 = Math.round((((float) clip.y2) * this.uiScale));
            if (sx2 <= sx1) {
                sx2 = (sx1 + 1);
            }
            if (sy2 <= sy1) {
                sy2 = (sy1 + 1);
            }
            this.activeDrawContext.method_44379(sx1, sy1, sx2, sy2);
        }
    }

    private void drawText(String text, int x, int y, int color) {
        if ((text == null) || (text.isEmpty())) {
            return;
        }
        TessellatorRenderer.drawTextWithShadow(this.activeDrawContext, this.field_22793, text, x, y, color);
    }

    private int nanoTextWidth(String text) {
        if ((text == null) || (text.isEmpty())) {
            return 0;
        }
        float measured = ((float) TessellatorRenderer.measureTextWidth(this.field_22793, text));
        if (measured >= 0.0f) return Math.max(0, ((int) Math.ceil(((double) measured))));
        Objects.requireNonNull(this.field_22793);
        return Math.max(0, ((int) Math.ceil(((double) (((float) text.length()) * Math.max(6.0f, (9.0f * 0.58f)))))));
    }

    private String trimToWidth(String text, int maxWidth) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (nanoTextWidth(text) <= maxWidth) return text;
        String suffix = "...";
        int suffixW = nanoTextWidth(suffix);
        if (suffixW >= maxWidth) return "";
        int end = text.length();
        while (end > 0 && (nanoTextWidth(text.substring(0, end)) + suffixW) > maxWidth) {
            end--;
        }
        if (end <= 0) {
        } else {
        }
        return (text.substring(0, end) + suffix);
    }

    private float toggleProgress(String key, boolean on) {
        float cur = ((Float) this.toggleAnim.getOrDefault(key, (!(!on) ? 1.0f : 0.0f))).floatValue();
        float target = (!(!on) ? 1.0f : 0.0f);
        float next = animate(cur, target, 18.0f, this.frameDt);
        if (Math.abs((next - target)) < 0.0025f) {
            next = target;
        }
        this.toggleAnim.put(key, next);
        return next;
    }

    private void refreshClientThemeColors() {
        ClientSettings cs = ClientSettings.get();
        if (cs == null) {
        } else {
        }
        int primary = opaque(cs.getPrimaryColor());
        if (cs == null) {
        } else {
        }
        int secondary = opaque(cs.getSecondaryColor());
        if (cs == null) {
        } else {
        }
        int text = opaque(cs.resolveTextColor(0L));
        int basePanel = -15591130;
        int baseRow = -15130061;
        BG = opaque(blendColor(basePanel, secondary, 0.2f));
        PANEL_BORDER = blendColor(-13352358, secondary, 0.35f);
        ROW_BG = opaque(blendColor(baseRow, secondary, 0.2f));
        ROW_BORDER = blendColor(-1489417881, secondary, 0.3f);
        ROW_HOVER = blendColor(620756991, primary, 0.33f);
        ACCENT = primary;
        ACCENT_SOFT = blendColor(primary, -1, 0.3f);
        TGL_OFF = blendColor(-13352875, secondary, 0.25f);
        TGL_ON = blendColor(primary, -1, 0.15f);
        TXT_ON = text;
        TXT_OFF = blendColor(-5589300, text, 0.28f);
        SEARCH_BG = opaque(blendColor(-15393234, secondary, 0.22f));
    }

    private static int opaque(int color) {
        return (-16777216 | (color & 16777215));
    }

    static float animate(float current, float target, float speed, float dt) {
        float d = Math.max(0.0f, Math.min(0.1f, dt));
        if (d <= 0.0f) return current;
        float k = (1.0f - ((float) Math.exp(((double) (-(Math.max(0.01f, speed)) * d)))));
        return (current + ((target - current) * k));
    }

    static float easeOutCubic(float t) {
        float c = clamp01(t);
        return (1.0f - ((float) Math.pow(((double) (1.0f - c)), 3.0)));
    }

    static int blendColor(int a, int b, float t) {
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

    static int alpha(int color, float mul) {
        float m = clamp01(mul);
        int a = ((color >>> 24) & 255);
        if (a == 0) {
            a = 255;
        }
        int aa = Math.max(0, Math.min(255, Math.round((((float) a) * m))));
        return ((aa << 24) | (color & 16777215));
    }

    static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    static int hsvToRgb(float hue, float saturation, float value) {
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

    static float[] rgbToHsv(int r, int g, int b) {
        float rf = (((float) r) / 255.0f);
        float gf = (((float) g) / 255.0f);
        float bf = (((float) b) / 255.0f);
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = (max - min);
        if (delta == 0.0f) {
            float h = 0.0f;
            break label_15;
        }
        if (max == rf) {
            h = ((((gf - bf) / delta) + (gf < bf ? 6.0f : 0.0f)) / 6.0f);
            break label_15;
        }
        if (max == gf) {
            h = ((((bf - rf) / delta) + 2.0f) / 6.0f);
            break label_15;
        }
        h = ((((rf - gf) / delta) + 4.0f) / 6.0f);
        label_15:
        if (max == 0.0f) {
        } else {
        }
        float s = (delta / max);
        var tmp0 = new float[3];
        tmp0[0] = clamp01(h);
        tmp0[1] = clamp01(s);
        tmp0[2] = max;
        return tmp0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
        if (((mx < x) || (mx > (x + w))) || (my < y)) break label_1;
        return my <= (y + h);
    }

    static void rr(class_332 ctx, int x, int y, int w, int h, int col) {
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, Math.max(2, Math.min((Math.min(w, h) / 2), 4)), col);
    }

    static void rrSmall(class_332 ctx, int x, int y, int w, int h, int col) {
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, Math.max(0, Math.min((Math.min(w, h) / 2), 3)), col);
    }

    static void rrPill(class_332 ctx, int x, int y, int w, int h, int col) {
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, (h / 2), col);
    }

    static void drawBorder(class_332 ctx, int x, int y, int w, int h, int color) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int a = ((color >>> 24) & 255);
        if (a == 0) return;
        float radius = Math.max(2.0f, Math.min((((float) Math.min(w, h)) * 0.28f), 4.0f));
        TessellatorRenderer.strokeRoundedRect(ctx, x, y, w, h, radius, 1.0f, color);
    }

    static {
        BG = -15657182;
        PANEL_BORDER = -13880252;
        ROW_BG = -15196371;
        ROW_BORDER = -14406598;
        ROW_HOVER = 536870911;
        ACCENT = -10897153;
        ACCENT_SOFT = -7548673;
        TGL_OFF = -13024427;
        TGL_ON = -12739585;
        TXT_ON = -657153;
        TXT_OFF = -4734767;
        SEARCH_BG = -15459543;
        LAYOUT_LOCK = new Object();
        SAVED_PANEL_STATE = new HashMap();
    }


    enum HitType {
        BOOL,
        INT,
        DOUBLE,
        ENUM,
        STRING,
        COLOR,
        BUTTON,
        KEYBIND,
        RESET;


        private HitType() {
        }

    }

    record SavedPanelState(int x, int y, boolean fold) {

        private SavedPanelState(int x, int y, boolean fold) {
            this.x = x;
            this.y = y;
            this.fold = fold;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public boolean fold() {
            return this.fold;
        }

    }

    class 1 {

        static {
            $SwitchMap$me$drake$viecheats$core$module$Category = new int[Category.values().length];
            try {
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.COMBAT.ordinal()] = 1;
            } catch (NoSuchFieldError ex3) {
                Object var0 = ex3;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.MOVEMENT.ordinal()] = 2;
            } catch (NoSuchFieldError ex6) {
                var0 = ex6;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.PLAYER.ordinal()] = 3;
            } catch (NoSuchFieldError ex9) {
                var0 = ex9;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.RENDER.ordinal()] = 4;
            } catch (NoSuchFieldError ex12) {
                var0 = ex12;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.WORLD.ordinal()] = 5;
            } catch (NoSuchFieldError ex15) {
                var0 = ex15;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.EXPLOIT.ordinal()] = 6;
            } catch (NoSuchFieldError ex18) {
                var0 = ex18;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.DONUT_SMP.ordinal()] = 7;
            } catch (NoSuchFieldError ex21) {
                var0 = ex21;
                try {
                }
                $SwitchMap$me$drake$viecheats$core$module$Category[Category.CLIENT.ordinal()] = 8;
            } catch (NoSuchFieldError ex24) {
                var0 = ex24;
            }
        }

    }

    final class SettingHitBox {
        final ClickGUI.HitType type;
        final Setting setting;
        final int x;
        final int y;
        final int w;
        final int h;
        final int sliderX;
        final int sliderW;
        final int valueX;
        final int valueY;
        final int valueW;
        final int valueH;

        private SettingHitBox(ClickGUI.HitType type, Setting setting, int x, int y, int w, int h, int sliderX, int sliderW, int valueX, int valueY, int valueW, int valueH) {
            this.type = type;
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.sliderX = sliderX;
            this.sliderW = sliderW;
            this.valueX = valueX;
            this.valueY = valueY;
            this.valueW = valueW;
            this.valueH = valueH;
        }

        static ClickGUI.SettingHitBox of(ClickGUI.HitType type, Setting setting, int x, int y, int w, int h) {
            return new ClickGUI.SettingHitBox(type, setting, x, y, w, h, 0, 0, 0, 0, 0, 0);
        }

        static ClickGUI.SettingHitBox slider(ClickGUI.HitType type, Setting setting, int x, int y, int w, int h, int sliderX, int sliderW) {
            return new ClickGUI.SettingHitBox(type, setting, x, y, w, h, sliderX, sliderW, 0, 0, 0, 0);
        }

        static ClickGUI.SettingHitBox sliderWithValue(ClickGUI.HitType type, Setting setting, int x, int y, int w, int h, int sliderX, int sliderW, int valueX, int valueY, int valueW, int valueH) {
            return new ClickGUI.SettingHitBox(type, setting, x, y, w, h, sliderX, sliderW, valueX, valueY, valueW, valueH);
        }

        boolean inValueArea(int mx, int my) {
            if ((this.valueW <= 0) || (this.valueH <= 0)) {
                return false;
            }
            if (((mx < this.valueX) || (mx > (this.valueX + this.valueW))) || (my < this.valueY)) break label_3;
            return my <= (this.valueY + this.valueH);
        }

    }

    final class ClipRect {
        final int x1;
        final int y1;
        final int x2;
        final int y2;

        ClipRect(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

    }

    final class CategoryPanel {
        final Category category;
        final List modules;
        int x;
        int y;
        int scroll;
        float scrollAnim;
        boolean collapsed;
        float expandAnim;

        CategoryPanel(Category category, List modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
            this.scroll = 0;
            this.scrollAnim = 0.0f;
            this.collapsed = false;
            this.expandAnim = 1.0f;
        }

    }
}

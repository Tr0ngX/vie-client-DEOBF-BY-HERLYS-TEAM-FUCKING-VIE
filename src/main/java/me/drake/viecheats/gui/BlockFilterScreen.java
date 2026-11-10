package me.drake.viecheats.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.modules.render.BlockESP;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.util.ClickGuiTheme;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class BlockFilterScreen extends class_437 {
    private static final int CELL_W = 176;
    private static final int CELL_H = 30;
    private static final int CELL_GAP_X = 8;
    private static final int CELL_GAP_Y = 6;
    private static final int ICON_SIZE = 16;
    private static final int PILL_W = 18;
    private static final int PILL_H = 9;
    private static final int MAX_SEARCH_LEN = 80;
    private static final double DEFAULT_TARGET_GUI_SCALE = 1.8;
    private static final int PICKER_SV = 96;
    private static final int PICKER_HUE_W = 12;
    private static final int PICKER_RGB_BAR_H = 5;
    private static final int PICKER_RGB_BAR_GAP = 5;
    private final BlockESP module;
    private final List allEntries;
    private final List filteredEntries;
    private final List visibleEntries;
    private final BlockFilterScreen.DeferredItemQueue deferredItems;
    private String search;
    private boolean searchFocused;
    private boolean filterDirty;
    private int scrollRow;
    private BlockESP.BlockOption colorTarget;
    private int pickerX;
    private int pickerY;
    private int pickerW;
    private int pickerH;
    private float pickHue;
    private float pickSat;
    private float pickVal;
    private int pickR;
    private int pickG;
    private int pickB;
    private boolean dragSv;
    private boolean dragHue;
    private int dragRgb;
    private float uiScale;
    private int uiWidth;
    private int uiHeight;
    private final BlockFilterScreen.Layout layout;

    public BlockFilterScreen(BlockESP module) {
        super(class_2561.method_43470("Block Filter"));
        this.filteredEntries = new ArrayList();
        this.visibleEntries = new ArrayList();
        this.deferredItems = new BlockFilterScreen.DeferredItemQueue();
        this.search = "";
        this.searchFocused = true;
        this.filterDirty = true;
        this.scrollRow = 0;
        this.pickerW = 160;
        this.pickerH = 156;
        this.dragRgb = -1;
        this.uiScale = 1.0f;
        this.layout = new BlockFilterScreen.Layout();
        this.module = module;
        this.allEntries = module.getBlockOptions();
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
        double targetScale = resolveTargetGuiScale();
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

    private static double resolveTargetGuiScale() {
        ClientSettings cs = ClientSettings.get();
        if (cs == null) return 1.8;
        double value = cs.getGuiScale();
        if (!(Double.isFinite(value))) return 1.8;
        if (value < 0.1) return 0.1;
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

    private void invalidateFilter() {
        this.filterDirty = true;
    }

    private void rebuildFiltered() {
        if (!this.filterDirty) return;
        this.filterDirty = false;
        this.filteredEntries.clear();
        String q = this.search.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            this.filteredEntries.addAll(this.allEntries);
            break label_10;
        }
        Object var2 = this.allEntries.iterator();
        while (var2.hasNext()) {
            BlockESP.BlockOption entry = ((BlockESP.BlockOption) var2.next());
            if (entry.searchKey().contains(q)) {
                this.filteredEntries.add(entry);
            }
        }
        label_10:
    }

    private void computeLayout() {
        rebuildFiltered();
        if (this.uiWidth < 900) break label_3;
        int cols = 3;
        break label_6;
        label_3:
        if (this.uiWidth >= 520) {
            cols = 2;
            break label_6;
        }
        cols = 1;
        label_6:
        int targetPanelW = (((cols * 176) + ((cols - 1) * 8)) + 20);
        int panelW = Math.min(Math.max(240, (this.uiWidth - 12)), targetPanelW);
        int cellW = (((panelW - 20) - ((cols - 1) * 8)) / cols);
        if (cellW < 120 && cols > 1) {
            cols = 1;
            targetPanelW = ((cols * 176) + 20);
            panelW = Math.min(Math.max(240, (this.uiWidth - 12)), targetPanelW);
            cellW = (panelW - 20);
        }
        int panelH = Math.min(Math.max(220, (this.uiHeight - 12)), 560);
        int panelX = ((this.uiWidth - panelW) / 2);
        int panelY = ((this.uiHeight - panelH) / 2);
        int headerH = 48;
        int footerH = 18;
        int gridX = (panelX + 10);
        int gridY = (panelY + headerH);
        int gridH = Math.max(30, ((panelH - headerH) - footerH));
        int gridW = ((cols * cellW) + ((cols - 1) * 8));
        int rowStride = 36;
        int visibleRows = Math.max(1, ((gridH + 6) / rowStride));
        int totalRows = (((this.filteredEntries.size() + cols) - 1) / cols);
        int maxScroll = Math.max(0, (totalRows - visibleRows));
        if (this.scrollRow > maxScroll) {
            this.scrollRow = maxScroll;
        }
        this.visibleEntries.clear();
        int startIndex = (this.scrollRow * cols);
        int maxVisible = (visibleRows * cols);
        int i = 0;
        while (i < maxVisible) {
            int idx = (startIndex + i);
            if (idx >= this.filteredEntries.size()) {
                break label_37;
            }
            this.visibleEntries.add(((BlockESP.BlockOption) this.filteredEntries.get(idx)));
            i++;
        }
        label_37:
        this.layout.panelX = panelX;
        this.layout.panelY = panelY;
        this.layout.panelW = panelW;
        this.layout.panelH = panelH;
        this.layout.gridX = gridX;
        this.layout.gridY = gridY;
        this.layout.gridW = gridW;
        this.layout.gridH = gridH;
        this.layout.cols = cols;
        this.layout.cellW = cellW;
        this.layout.visibleRows = visibleRows;
        this.layout.totalRows = totalRows;
        this.layout.maxScroll = maxScroll;
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
    }

    public void method_25394(class_332 ctx, int mx, int my, float delta) {
        updateUiScale();
        int uiMx = toUiCoord(((double) mx));
        int uiMy = toUiCoord(((double) my));
        computeLayout();
        ClickGuiTheme.Palette palette = ClickGuiTheme.resolve();
        int accent = palette.accent();
        int bg = palette.bg();
        int border = palette.panelBorder();
        int rowBg = palette.rowBg();
        int rowBorder = palette.rowBorder();
        int textWhite = palette.txtOn();
        int textDim = palette.txtOff();
        int hoverBg = palette.rowHover();
        int searchBg = palette.searchBg();
        TessellatorRenderer.setContext(ctx);
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(this.uiScale, this.uiScale);
        this.deferredItems.clear();
        fillRect(ctx, 0, 0, this.uiWidth, this.uiHeight, palette.overlayBg());
        ClickGUI.rr(ctx, this.layout.panelX, this.layout.panelY, this.layout.panelW, this.layout.panelH, bg);
        drawBorder(ctx, this.layout.panelX, this.layout.panelY, this.layout.panelW, this.layout.panelH, border);
        drawText(ctx, "Block Filter", (this.layout.panelX + 8), (this.layout.panelY + 6), textWhite);
        String sub = "LMB Toggle | RMB Color";
        drawText(ctx, sub, (((this.layout.panelX + this.layout.panelW) - textWidth(sub)) - 8), (this.layout.panelY + 6), textDim);
        int searchX = (this.layout.panelX + 8);
        int searchY = (this.layout.panelY + 22);
        int searchW = (this.layout.panelW - 16);
        int searchH = 16;
        if (!(!this.searchFocused)) {
        } else {
        }
        ClickGUI.rrSmall(searchX, searchY, searchW, searchH, ClickGuiTheme.withAlpha(accent, 48), searchBg);
        drawBorder(ctx, searchX, searchY, searchW, searchH, (!(!this.searchFocused) ? accent : rowBorder));
        if (this.search.isEmpty()) {
        } else {
        }
        String searchText = (this.search + (!(!this.searchFocused) ? "_" : ""));
        drawText(ctx, trimToWidth(searchText, (searchW - 8)), (searchX + 4), (searchY + 4), (this.search.isEmpty() ? textDim : textWhite));
        int rowStride = 36;
        int i = 0;
        while (i < this.visibleEntries.size()) {
            BlockESP.BlockOption entry = ((BlockESP.BlockOption) this.visibleEntries.get(i));
            int col = (i % this.layout.cols);
            int row = (i / this.layout.cols);
            int cx = (this.layout.gridX + (col * (this.layout.cellW + 8)));
            int cy = (this.layout.gridY + (row * rowStride));
            boolean enabled = this.module.isBlockEnabled(entry.id());
            if (((uiMx < cx) || (uiMx >= (cx + this.layout.cellW))) || (uiMy < cy)) break label_55;
            boolean hover = uiMy < (cy + 30);
            int typeColor = this.module.getBlockColor(entry.id());
            ClickGUI.rrSmall(ctx, cx, cy, this.layout.cellW, 30, rowBg);
            drawBorder(ctx, cx, cy, this.layout.cellW, 30, rowBorder);
            if (!(!hover)) {
                ClickGUI.rrSmall(ctx, cx, cy, this.layout.cellW, 30, hoverBg);
            }
            if (!(!enabled)) {
                fillRect(ctx, cx, cy, (cx + 2), (cy + 30), accent);
            }
            int iconX = (cx + 8);
            int iconY = (cy + 7);
            drawEntryIcon(ctx, entry, iconX, iconY, palette);
            int labelMax = Math.max(20, (this.layout.cellW - 96));
            String label = trimToWidth(entry.name(), labelMax);
            drawText(ctx, label, (cx + 28), (cy + 11), (!(!enabled) ? accent : textWhite));
            int colorX = ((cx + this.layout.cellW) - 56);
            int colorY = (cy + 9);
            ClickGUI.rrSmall(ctx, colorX, colorY, 12, 12, (-16777216 | (typeColor & 16777215)));
            drawBorder(ctx, colorX, colorY, 12, 12, rowBorder);
            int toggleX = (((cx + this.layout.cellW) - 18) - 8);
            int toggleY = (cy + 10);
            drawToggle(ctx, toggleX, toggleY, enabled, palette);
            i++;
        }
        this.deferredItems.flush(ctx);
        if (this.layout.totalRows > this.layout.visibleRows) {
            int trackX = ((this.layout.panelX + this.layout.panelW) - 4);
            int trackY = this.layout.gridY;
            int trackH = this.layout.gridH;
            fillRect(ctx, trackX, trackY, (trackX + 2), (trackY + trackH), ClickGuiTheme.withAlpha(rowBorder, 90));
            float page = (((float) this.layout.visibleRows) / ((float) Math.max(1, this.layout.totalRows)));
            int knobH = Math.max(16, ((int) (((float) trackH) * page)));
            int travel = Math.max(0, (trackH - knobH));
            if (!this.layout.maxScroll) {
            } else {
            }
            float pos = (((float) this.scrollRow) / ((float) this.layout.maxScroll));
            int knobY = (trackY + Math.round((((float) travel) * pos)));
            fillRect(ctx, trackX, knobY, (trackX + 2), (knobY + knobH), accent);
        }
        int enabledCount = this.module.getEnabledBlockTypeCount();
        String countStr = (enabledCount + "/" + this.module.getTotalBlockTypeCount() + " enabled | " + this.filteredEntries.size() + " shown");
        drawText(ctx, countStr, (this.layout.panelX + 8), ((this.layout.panelY + this.layout.panelH) - 12), textDim);
        String esc = "ESC to close";
        drawText(ctx, esc, (((this.layout.panelX + this.layout.panelW) - textWidth(esc)) - 8), ((this.layout.panelY + this.layout.panelH) - 12), textDim);
        if (this.colorTarget != null) {
            renderColorPicker(ctx, palette);
        }
        ctx.method_51448().popMatrix();
        break label_106;
        Object var41 = 0.0f;
        ctx.method_51448().popMatrix();
        throw var41;
        label_106:
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        updateUiScale();
        computeLayout();
        int mx = toUiCoord(click.comp_4798());
        int my = toUiCoord(click.comp_4799());
        int btn = click.method_74245();
        if (this.colorTarget != null) {
            if (handlePickerClick(mx, my, btn)) return true;
            closePicker();
            return true;
        }
        int searchX = (this.layout.panelX + 8);
        int searchY = (this.layout.panelY + 22);
        int searchW = (this.layout.panelW - 16);
        int searchH = 16;
        if (mx >= searchX && mx < (searchX + searchW) && my >= searchY && my < (searchY + searchH)) {
            this.searchFocused = true;
            return true;
        }
        this.searchFocused = false;
        int rowStride = 36;
        int i = 0;
        while (i < this.visibleEntries.size()) {
            BlockESP.BlockOption entry = ((BlockESP.BlockOption) this.visibleEntries.get(i));
            int col = (i % this.layout.cols);
            int row = (i / this.layout.cols);
            int cx = (this.layout.gridX + (col * (this.layout.cellW + 8)));
            int cy = (this.layout.gridY + (row * rowStride));
            if (mx >= cx && mx < (cx + this.layout.cellW) && my >= cy && my < (cy + 30)) {
                if (btn == 0) {
                    this.module.setBlockEnabled(entry.id(), !(this.module.isBlockEnabled(entry.id())));
                    break label_30;
                }
                if (btn != 1) break label_30;
                openPicker(entry, mx, my);
                label_30:
                return true;
            }
            i++;
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mx, double my, double h, double v) {
        updateUiScale();
        computeLayout();
        int uiMx = toUiCoord(mx);
        int uiMy = toUiCoord(my);
        if (uiMx >= this.layout.panelX && uiMx < (this.layout.panelX + this.layout.panelW) && uiMy >= this.layout.gridY && uiMy < (this.layout.gridY + this.layout.gridH)) {
            if (this.layout.maxScroll > 0) {
                int step = ((int) Math.round(v));
                if (step == 0) {
                    step = (v > 0.0 ? 1 : -1);
                }
                this.scrollRow = Math.max(0, Math.min(this.layout.maxScroll, (this.scrollRow - step)));
            }
            return true;
        }
        return super.method_25401(mx, my, h, v);
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        updateUiScale();
        if (this.colorTarget == null) break label_4;
        if (!this.dragSv && !this.dragHue) {
            if (this.dragRgb == -1) break label_4;
        }
        updatePickerFromMouse(toUiCoord(click.comp_4798()), toUiCoord(click.comp_4799()));
        return true;
        label_4:
        return super.method_25403(click, dx, dy);
    }

    public boolean method_25406(class_11909 click) {
        this.dragSv = false;
        this.dragHue = false;
        this.dragRgb = -1;
        return super.method_25406(click);
    }

    public boolean method_25404(class_11908 input) {
        if (input.method_74231() && this.colorTarget != null) {
            closePicker();
            return true;
        }
        if (!this.searchFocused) break label_23;
        int key = input.comp_4795();
        if (key == 259) {
            if (!(this.search.isEmpty())) {
                this.search = this.search.substring(0, (this.search.length() - 1));
                this.scrollRow = 0;
                invalidateFilter();
            }
            return true;
        }
        if (input.method_74243()) {
            if ((this.field_22787 == null) || (this.field_22787.field_1774 == null)) break label_19;
            String clip = this.field_22787.field_1774.method_1460();
            if ((clip == null) || (clip.isEmpty())) break label_19;
            String cleaned = clip.replace(10, 32).replace(13, 32);
            this.search = limitSearch((this.search + cleaned));
            this.scrollRow = 0;
            invalidateFilter();
            label_19:
            return true;
        }
        if (key != 257) {
            if (key != 335) break label_23;
        }
        this.searchFocused = false;
        return true;
        label_23:
        if (input.method_74231() && !(this.search.isEmpty())) {
            this.search = "";
            this.scrollRow = 0;
            invalidateFilter();
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        if (!this.searchFocused) return super.method_25400(input);
        char c = ((char) input.comp_4793());
        if (c >= 32 && c < 127) {
            this.search = limitSearch((this.search + c));
            this.scrollRow = 0;
            invalidateFilter();
            return true;
        }
        return super.method_25400(input);
    }

    public void method_25432() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance != null && instance.getConfigManager() != null) {
            instance.getConfigManager().requestSave();
        }
        super.method_25432();
    }

    private String limitSearch(String value) {
        if (value.length() <= 80) return value;
        return value.substring(0, 80);
    }

    private void openPicker(BlockESP.BlockOption entry, int mx, int my) {
        this.colorTarget = entry;
        int rgb = this.module.getBlockColor(entry.id());
        float[] hsv = rgbToHsv(((rgb >> 16) & 255), ((rgb >> 8) & 255), (rgb & 255));
        this.pickHue = hsv[0];
        this.pickSat = hsv[1];
        this.pickVal = hsv[2];
        this.pickR = ((rgb >> 16) & 255);
        this.pickG = ((rgb >> 8) & 255);
        this.pickB = (rgb & 255);
        this.dragSv = false;
        this.dragHue = false;
        this.dragRgb = -1;
        this.pickerW = 160;
        this.pickerH = 156;
        this.pickerX = Math.max(8, Math.min(((this.uiWidth - this.pickerW) - 8), (mx + 8)));
        this.pickerY = Math.max(30, Math.min(((this.uiHeight - this.pickerH) - 8), (my - 8)));
    }

    private void closePicker() {
        this.colorTarget = null;
        this.dragSv = false;
        this.dragHue = false;
        this.dragRgb = -1;
    }

    private void renderColorPicker(class_332 ctx, ClickGuiTheme.Palette palette) {
        if (this.colorTarget == null) return;
        ClickGUI.rr(ctx, this.pickerX, this.pickerY, this.pickerW, this.pickerH, palette.bg());
        drawBorder(ctx, this.pickerX, this.pickerY, this.pickerW, this.pickerH, palette.panelBorder());
        drawText(ctx, "Color Picker", (this.pickerX + 8), (this.pickerY + 6), palette.accent());
        int svX = (this.pickerX + 8);
        int svY = (this.pickerY + 18);
        int hueX = ((svX + 96) + 8);
        int hueY = svY;
        int infoX = ((hueX + 12) + 8);
        renderSvSquare(ctx, svX, svY, 96);
        drawBorder(ctx, svX, svY, 96, 96, ClickGuiTheme.withAlpha(palette.accent(), 170));
        renderHueBar(ctx, hueX, hueY, 12, 96);
        drawBorder(ctx, hueX, hueY, 12, 96, ClickGuiTheme.withAlpha(palette.accent(), 170));
        int sx = (svX + Math.round((this.pickSat * 95.0f)));
        int sy = (svY + Math.round(((1.0f - this.pickVal) * 95.0f)));
        drawCrosshair(ctx, sx, sy, palette);
        int hy = (hueY + Math.round((this.pickHue * 95.0f)));
        fillRect(ctx, (hueX - 2), (hy - 1), ((hueX + 12) + 2), (hy + 1), palette.accentSoft());
        int rgb = (((this.pickR << 16) | (this.pickG << 8)) | this.pickB);
        ClickGUI.rr(ctx, infoX, svY, 24, 16, (-16777216 | rgb));
        drawText(ctx, ("H " + Math.round((this.pickHue * 360.0f))), infoX, (svY + 22), palette.txtOff());
        drawText(ctx, ("S " + Math.round((this.pickSat * 100.0f)) + "%"), infoX, (svY + 34), palette.txtOff());
        drawText(ctx, ("V " + Math.round((this.pickVal * 100.0f)) + "%"), infoX, (svY + 46), palette.txtOff());
        var tmp0 = new Object[1];
        tmp0[0] = rgb;
        drawText(ctx, ("#" + String.format("%06X", tmp0)), infoX, (svY + 58), palette.txtOn());
        int rgbX = svX;
        int rgbY = ((svY + 96) + 6);
        int rgbW = 116;
        renderRgbBar(ctx, rgbX, rgbY, rgbW, 5, this.pickR, -45747, "R", palette);
        renderRgbBar(ctx, rgbX, ((rgbY + 5) + 5), rgbW, 5, this.pickG, -11216533, "G", palette);
        renderRgbBar(ctx, rgbX, (rgbY + 20), rgbW, 5, this.pickB, -11694593, "B", palette);
    }

    private boolean handlePickerClick(int mx, int my, int btn) {
        if (((mx < this.pickerX) || (mx > (this.pickerX + this.pickerW))) || (my < this.pickerY)) break label_1;
        if (my <= (this.pickerY + this.pickerH)) break label_2;
        label_1:
        return false;
        label_2:
        if (btn != 0) return true;
        int svX = (this.pickerX + 8);
        int svY = (this.pickerY + 18);
        int hueX = ((svX + 96) + 8);
        int hueY = svY;
        int rgbX = svX;
        int rgbY = ((svY + 96) + 6);
        int rgbW = 116;
        int rgbStride = 10;
        if (mx >= svX && mx < (svX + 96) && my >= svY && my < (svY + 96)) {
            this.dragSv = true;
            this.dragHue = false;
            this.dragRgb = -1;
            updatePickerFromMouse(mx, my);
            return true;
        }
        if (mx >= hueX && mx < (hueX + 12) && my >= hueY && my < (hueY + 96)) {
            this.dragHue = true;
            this.dragSv = false;
            this.dragRgb = -1;
            updatePickerFromMouse(mx, my);
            return true;
        }
        int i = 0;
        while (i < 3) {
            int y = (rgbY + (i * rgbStride));
            if (mx >= rgbX && mx < (rgbX + rgbW) && my >= y && my < (y + 5)) {
                this.dragRgb = i;
                this.dragSv = false;
                this.dragHue = false;
                updatePickerFromMouse(mx, my);
                return true;
            }
            i++;
        }
        return true;
    }

    private void updatePickerFromMouse(int mx, int my) {
        if (this.colorTarget == null) return;
        int svX = (this.pickerX + 8);
        int svY = (this.pickerY + 18);
        int hueY = svY;
        if (this.dragRgb != -1) {
            int rgbX = (this.pickerX + 8);
            int rgbW = 116;
            int value = clamp(Math.round((clamp01((((float) (mx - rgbX)) / ((float) Math.max(1, (rgbW - 1))))) * 255.0f)), 0, 255);
            if (!this.dragRgb) {
                this.pickR = value;
            }
            if (this.dragRgb == 1) {
                this.pickG = value;
            }
            if (this.dragRgb == 2) {
                this.pickB = value;
            }
            float[] hsv = rgbToHsv(this.pickR, this.pickG, this.pickB);
            this.pickHue = hsv[0];
            this.pickSat = hsv[1];
            this.pickVal = hsv[2];
        } else {
            if (!(!this.dragSv)) {
                this.pickSat = clamp01((((float) (mx - svX)) / ((float) Math.max(1, 95))));
                this.pickVal = (1.0f - clamp01((((float) (my - svY)) / ((float) Math.max(1, 95)))));
            }
            if (!(!this.dragHue)) {
                this.pickHue = clamp01((((float) (my - hueY)) / ((float) Math.max(1, 95))));
            }
            int rgb = hsvToRgb(this.pickHue, this.pickSat, this.pickVal);
            this.pickR = ((rgb >> 16) & 255);
            this.pickG = ((rgb >> 8) & 255);
            this.pickB = (rgb & 255);
        }
        this.module.setBlockColor(this.colorTarget.id(), (((this.pickR << 16) | (this.pickG << 8)) | this.pickB));
    }

    private void renderSvSquare(class_332 ctx, int x, int y, int size) {
        int steps = 24;
        int iy = 0;
        while (iy < steps) {
            float v0 = (1.0f - (((float) iy) / ((float) steps)));
            float v1 = (1.0f - (((float) (iy + 1)) / ((float) steps)));
            int y0 = (y + ((iy * size) / steps));
            int y1 = (y + (((iy + 1) * size) / steps));
            int ix = 0;
            while (ix < steps) {
                float s0 = (((float) ix) / ((float) steps));
                int x0 = (x + ((ix * size) / steps));
                int x1 = (x + (((ix + 1) * size) / steps));
                int c = (-16777216 | hsvToRgb(this.pickHue, s0, ((v0 + v1) * 0.5f)));
                fillRect(ctx, x0, y0, x1, y1, c);
                ix++;
            }
            iy++;
        }
    }

    private void renderHueBar(class_332 ctx, int x, int y, int w, int h) {
        int steps = 48;
        int i = 0;
        while (i < steps) {
            float h0 = (((float) i) / ((float) steps));
            int y0 = (y + ((i * h) / steps));
            int y1 = (y + (((i + 1) * h) / steps));
            int c = (-16777216 | hsvToRgb(h0, 1.0f, 1.0f));
            fillRect(ctx, x, y0, (x + w), y1, c);
            i++;
        }
    }

    private void drawCrosshair(class_332 ctx, int x, int y, ClickGuiTheme.Palette palette) {
        int fg = palette.txtOn();
        int bg = ClickGuiTheme.withAlpha(palette.bg(), 170);
        fillRect(ctx, (x - 3), y, (x + 4), (y + 1), fg);
        fillRect(ctx, x, (y - 3), (x + 1), (y + 4), fg);
        fillRect(ctx, (x - 2), (y - 2), (x + 3), (y + 3), bg);
        fillRect(ctx, (x - 2), y, (x + 3), (y + 1), fg);
        fillRect(ctx, x, (y - 2), (x + 1), (y + 3), fg);
    }

    private void renderRgbBar(class_332 ctx, int x, int y, int w, int h, int value, int barColor, String label, ClickGuiTheme.Palette palette) {
        int track = blend(palette.searchBg(), palette.rowBg(), 0.45f);
        ClickGUI.rrSmall(ctx, x, y, w, h, track);
        int fillW = Math.round(((((float) clamp(value, 0, 255)) / 255.0f) * ((float) Math.max(1, (w - 1)))));
        if (fillW > 0) {
            ClickGUI.rrSmall(ctx, x, y, fillW, h, barColor);
        }
        int knobX = (x + fillW);
        fillRect(ctx, (knobX - 1), (y - 1), (knobX + 1), ((y + h) + 1), palette.txtOn());
        drawText(ctx, (label + " " + value), ((x + w) + 6), (y - 1), palette.txtOff());
    }

    private void drawToggle(class_332 ctx, int x, int y, boolean on, ClickGuiTheme.Palette palette) {
        if (!(!on)) {
        } else {
        }
        int bg = palette.tglOff();
        if (!(!on)) {
            ClickGUI.rrPill(ctx, (x - 1), (y - 1), 20, 11, ClickGuiTheme.withAlpha(palette.accent(), 40));
        }
        ClickGUI.rrPill(ctx, x, y, 18, 9, bg);
        if (!(!on)) {
        } else {
        }
        int knobX = (x + 1);
        ClickGUI.rrPill(ctx, knobX, (y + 1), 7, 7, palette.txtOn());
    }

    private void drawEntryIcon(class_332 ctx, BlockESP.BlockOption entry, int x, int y, ClickGuiTheme.Palette palette) {
        class_1799 icon = entry.icon();
        if (icon != null && !(icon.method_7960())) {
            ClickGUI.rrSmall(ctx, (x - 1), (y - 1), 18, 18, ClickGuiTheme.withAlpha(palette.rowBorder(), 110));
            this.deferredItems.queue(icon, x, y);
            return;
        }
        float t = (((float) ((entry.id().hashCode() >>> 1) & 255)) / 255.0f);
        int badgeColor = blend(palette.rowBg(), palette.accent(), (0.22f + (0.3f * t)));
        ClickGUI.rrSmall(ctx, x, y, 16, 16, badgeColor);
        String label = entry.name();
        if ((label == null) || (label.isEmpty())) {
            return;
        }
        char ch = Character.toUpperCase(label.charAt(0));
        TessellatorRenderer.drawTextWithShadow(ctx, this.field_22793, String.valueOf(ch), (x + 5), (y + 4), palette.txtOn());
    }

    private void drawBorder(class_332 ctx, int x, int y, int w, int h, int color) {
        ClickGuiTheme.drawBorder(ctx, x, y, w, h, color);
    }

    private String trimToWidth(String text, int maxWidth) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (textWidth(text) <= maxWidth) return text;
        String suffix = "...";
        int suffixW = textWidth(suffix);
        int end = text.length();
        while (end > 0 && (textWidth(text.substring(0, end)) + suffixW) > maxWidth) {
            end--;
        }
        if (end <= 0) {
        } else {
        }
        return (text.substring(0, end) + suffix);
    }

    private boolean useNano() {
        return true;
    }

    private void fillRect(class_332 ctx, int x1, int y1, int x2, int y2, int color) {
        if ((x2 <= x1) || (y2 <= y1)) {
            return;
        }
        TessellatorRenderer.fillRect(ctx, x1, y1, x2, y2, color);
    }

    private void drawText(class_332 ctx, String value, int x, int y, int color) {
        if ((value == null) || (value.isEmpty())) {
            return;
        }
        TessellatorRenderer.drawTextWithShadow(ctx, this.field_22793, value, x, y, color);
    }

    private int textWidth(String value) {
        if ((value == null) || (value.isEmpty())) {
            return 0;
        }
        int width = TessellatorRenderer.measureTextWidth(this.field_22793, value);
        if (width >= 0) return Math.max(0, width);
        Objects.requireNonNull(this.field_22793);
        return Math.max(0, ((int) Math.ceil(((double) (((float) value.length()) * Math.max(6.0f, (9.0f * 0.58f)))))));
    }

    private static int blend(int a, int b, float t) {
        float f = Math.max(0.0f, Math.min(1.0f, t));
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

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
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

    private static float[] rgbToHsv(int r, int g, int b) {
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
        float v = max;
        var tmp0 = new float[3];
        tmp0[0] = clamp01(h);
        tmp0[1] = clamp01(s);
        tmp0[2] = clamp01(v);
        return tmp0;
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return true;
    }


    final class Layout {
        int panelX;
        int panelY;
        int panelW;
        int panelH;
        int gridX;
        int gridY;
        int gridW;
        int gridH;
        int cols;
        int cellW;
        int visibleRows;
        int totalRows;
        int maxScroll;

        private Layout() {
        }

    }

    record ItemDrawCall(class_1799 stack, int x, int y) {

        private ItemDrawCall(class_1799 stack, int x, int y) {
            this.stack = stack;
            this.x = x;
            this.y = y;
        }

        public class_1799 stack() {
            return this.stack;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

    }

    final class DeferredItemQueue {
        private final List calls;

        private DeferredItemQueue() {
            this.calls = new ArrayList();
        }

        void queue(class_1799 stack, int x, int y) {
            if ((stack == null) || (stack.method_7960())) {
                return;
            }
            this.calls.add(new BlockFilterScreen.ItemDrawCall(stack.method_7972(), x, y));
        }

        void flush(class_332 ctx) {
            if ((ctx == null) || (this.calls.isEmpty())) {
                return;
            }
            Object var2 = this.calls.iterator();
            while (var2.hasNext()) {
                BlockFilterScreen.ItemDrawCall call = ((BlockFilterScreen.ItemDrawCall) var2.next());
                ctx.method_51427(call.stack(), call.x(), call.y());
            }
            ctx.method_73199();
            this.calls.clear();
        }

        void clear() {
            this.calls.clear();
        }

    }
}

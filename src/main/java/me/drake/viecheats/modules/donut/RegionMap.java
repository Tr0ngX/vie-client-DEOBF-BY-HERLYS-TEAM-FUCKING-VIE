package me.drake.viecheats.modules.donut;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.util.ClickGuiTheme;
import net.minecraft.class_1937;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class RegionMap extends Module {
    private static final int GRID_SIZE = 9;
    private static final double ORIGIN_OFFSET = 225000.0;
    private static final double CELL_WORLD_SIZE = 50000.0;
    private static final int[][] REGION_GRID;
    private static final Map REGION_NAMES;
    private boolean mappedWorld;
    private String worldMessage;
    private int regionId;
    private String regionName;
    private int gridX;
    private int gridZ;
    private static final int HUD_DEFAULT_X = 8;
    private static final int HUD_DEFAULT_Y = 66;
    private static final int HUD_CELL = 14;
    private static final int HUD_GAP = 1;
    private static final int HUD_MAP_W = 134;
    private static final int HUD_PANEL_W = 160;
    private static final int HUD_PANEL_H = 214;
    public final IntSetting posX;
    public final IntSetting posY;
    public final IntSetting scale;

    public RegionMap() {
        super("Region Map", "Shows Donut region map HUD", Category.DONUT_SMP);
        this.mappedWorld = true;
        this.worldMessage = "Overworld";
        this.regionId = -1;
        this.regionName = "Unknown";
        this.gridX = -1;
        this.gridZ = -1;
        this.posX = ((IntSetting) addSetting(new IntSetting("PosX", "", 8, 0, 10000, 1)));
        this.posY = ((IntSetting) addSetting(new IntSetting("PosY", "", 66, 0, 10000, 1)));
        this.scale = ((IntSetting) addSetting(new IntSetting("Scale", "Region map scale", 10, 6, 20, 1)));
    }

    public void onEnable() {
        this.posX.setVisibleInGui(false);
        this.posY.setVisibleInGui(false);
        super.onEnable();
        refreshRegionState();
    }

    public String getInfo() {
        if (!this.mappedWorld) return "N/A";
        if (this.regionId == -1) {
        } else {
        }
        return (this.regionName + " (" + this.regionId + ")");
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        refreshRegionState();
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.HUD_2D) return;
        if (!(isInGame())) return;
        class_332 ctx = event.getDrawContext();
        if ((ctx == null) || (!(TessellatorRenderer.isInFrame()))) {
            return;
        }
        renderHud(ctx);
    }

    private void refreshRegionState() {
        if (mc.field_1687 == null) break label_1;
        if (mc.field_1724 != null) break label_2;
        label_1:
        return;
        label_2:
        if (mc.field_1687.method_27983() == class_1937.field_25180) {
            this.mappedWorld = false;
            this.worldMessage = "The Nether is not mapped.";
            this.regionId = -1;
            this.regionName = "Unknown";
            this.gridX = -1;
            this.gridZ = -1;
            return;
        }
        if (mc.field_1687.method_27983() == class_1937.field_25181) {
            this.mappedWorld = false;
            this.worldMessage = "The End is not mapped.";
            this.regionId = -1;
            this.regionName = "Unknown";
            this.gridX = -1;
            this.gridZ = -1;
            return;
        }
        this.mappedWorld = true;
        this.worldMessage = "Overworld";
        double x = mc.field_1724.method_23317();
        double z = mc.field_1724.method_23321();
        this.gridX = ((int) Math.floor(((x + 225000.0) / 50000.0)));
        this.gridZ = ((int) Math.floor(((z + 225000.0) / 50000.0)));
        if (this.gridX >= 0 && this.gridX < 9 && this.gridZ >= 0 && this.gridZ < 9) {
            this.regionId = REGION_GRID[this.gridZ][this.gridX];
            this.regionName = ((String) REGION_NAMES.getOrDefault(this.regionId, "Unknown"));
        } else {
            this.regionId = -1;
            this.regionName = "Outside mapped world";
        }
    }

    private void renderHud(class_332 ctx) {
        class_327 tr = mc.field_1772;
        ClickGuiTheme.Palette palette = ClickGuiTheme.resolve();
        float hudScale = getScale();
        int x = ((Integer) this.posX.get()).intValue();
        int y = ((Integer) this.posY.get()).intValue();
        int cell = Math.max(8, scaled(14, hudScale));
        int gap = Math.max(1, scaled(1, hudScale));
        float idSize = Math.max(6.0f, (7.0f * hudScale));
        float titleSize = Math.max(7.0f, (8.0f * hudScale));
        float bodySize = Math.max(6.5f, (7.0f * hudScale));
        int mapW = ((9 * cell) + (8 * gap));
        int mapH = ((9 * cell) + (8 * gap));
        int sidePad = Math.max(10, scaled(13, hudScale));
        int legendColW = Math.max(scaled(32, hudScale), (textWidth("EU CENTRAL", bodySize) + scaled(18, hudScale)));
        int panelW = Math.max((mapW + (sidePad * 2)), ((legendColW * 3) + scaled(10, hudScale)));
        int legendStartY = (((y + scaled(24, hudScale)) + mapH) + scaled(20, hudScale));
        int legendRowStep = Math.max(10, scaled(14, hudScale));
        int panelH = Math.max(scaled(214, hudScale), (((legendStartY + (legendRowStep * 2)) + scaled(8, hudScale)) - y));
        int panelBg = ClickGuiTheme.withAlpha(palette.bg(), 248);
        int panelBorder = palette.panelBorder();
        int txtOn = palette.txtOn();
        int txtOff = palette.txtOff();
        int selectedLabelColor = palette.accent();
        TessellatorRenderer.fillRoundedRect(ctx, x, y, panelW, panelH, Math.max(6, scaled(9, hudScale)), panelBg);
        ClickGuiTheme.drawBorder(ctx, x, y, panelW, panelH, panelBorder);
        drawText(ctx, "REGION MAP", (x + scaled(9, hudScale)), (y + scaled(6, hudScale)), titleSize, txtOn);
        int gx = (x + ((panelW - mapW) / 2));
        int gy = (y + scaled(24, hudScale));
        int mapPad = Math.max(2, scaled(3, hudScale));
        TessellatorRenderer.fillRoundedRect(ctx, (gx - mapPad), (gy - mapPad), (mapW + (mapPad * 2)), (mapH + (mapPad * 2)), Math.max(4, scaled(5, hudScale)), ClickGuiTheme.withAlpha(palette.rowBg(), 190));
        int rz = 0;
        while (rz < 9) {
            int rx = 0;
            while (rx < 9) {
                int cx = (gx + (rx * (cell + gap)));
                int cy = (gy + (rz * (cell + gap)));
                int id = REGION_GRID[rz][rx];
                String group = ((String) REGION_NAMES.getOrDefault(id, "Unknown"));
                int groupColor = regionGroupColor(group, palette);
                if (((!this.mappedWorld) || (this.regionId == -1)) || (rx != this.gridX)) break label_42;
                boolean selected = rz == this.gridZ;
                int cellBg = ClickGuiTheme.withAlpha(groupColor, (!(!selected) ? 120 : 82));
                int cellBorder = ClickGuiTheme.withAlpha(groupColor, (!(!selected) ? 255 : 238));
                if (!(!selected)) {
                    int selPad = Math.max(1, scaled(2, hudScale));
                    TessellatorRenderer.fillRoundedRect(ctx, (cx - selPad), (cy - selPad), (cell + (selPad * 2)), (cell + (selPad * 2)), Math.max(4, scaled(5, hudScale)), ClickGuiTheme.withAlpha(palette.accentSoft(), 146));
                }
                TessellatorRenderer.fillRoundedRect(ctx, cx, cy, cell, cell, Math.max(2, scaled(4, hudScale)), cellBg);
                ClickGuiTheme.drawBorder(ctx, cx, cy, cell, cell, cellBorder);
                String idText = String.valueOf(id);
                int numW = textWidth(idText, idSize);
                int tx = (cx + Math.max(0, ((cell - numW) / 2)));
                int ty = (cy + Math.max(0, ((cell - Math.round(idSize)) / 2)));
                drawText(ctx, idText, tx, ty, idSize, ClickGuiTheme.withAlpha(txtOn, 246));
                rx++;
            }
            rz++;
        }
        if (!(!this.mappedWorld) && this.regionId != -1) {
            int markerX = (gx + (this.gridX * (cell + gap)));
            int markerY = (gy + (this.gridZ * (cell + gap)));
            int dotOuter = Math.max(6, scaled(6, hudScale));
            int dotInner = Math.max(4, scaled(4, hudScale));
            int dotX = (markerX + ((cell - dotInner) / 2));
            int dotY = (markerY + ((cell - dotInner) / 2));
            int outerX = (markerX + ((cell - dotOuter) / 2));
            int outerY = (markerY + ((cell - dotOuter) / 2));
            TessellatorRenderer.fillRoundedRect(ctx, outerX, outerY, dotOuter, dotOuter, Math.max(3, scaled(3, hudScale)), ClickGuiTheme.withAlpha(palette.accentSoft(), 170));
            TessellatorRenderer.fillRoundedRect(ctx, dotX, dotY, dotInner, dotInner, Math.max(2, scaled(2, hudScale)), ClickGuiTheme.withAlpha(palette.accent(), 255));
        }
        if (!this.mappedWorld) {
            String selectedText = this.worldMessage;
            break label_86;
        }
        if (this.regionId == -1) {
            selectedText = "OUTSIDE MAPPED WORLD";
            break label_86;
        }
        selectedText = (this.regionId + " - " + legendLabel(this.regionName));
        label_86:
        selectedText = trimToWidthScaled(selectedText, (panelW - scaled(20, hudScale)), bodySize);
        int selectedW = textWidth(selectedText, bodySize);
        int selectedX = (x + Math.max(scaled(12, hudScale), ((panelW - selectedW) / 2)));
        drawText(ctx, selectedText, selectedX, ((gy + mapH) + scaled(7, hudScale)), bodySize, selectedLabelColor);
        var tmp0 = new RegionMap.LegendEntry[6];
        tmp0[0] = new RegionMap.LegendEntry("NA E", regionGroupColor("NA East", palette));
        tmp0[1] = new RegionMap.LegendEntry("NA W", regionGroupColor("NA West", palette));
        tmp0[2] = new RegionMap.LegendEntry("OCE", regionGroupColor("Oceania", palette));
        tmp0[3] = new RegionMap.LegendEntry("EU C", regionGroupColor("EU Central", palette));
        tmp0[4] = new RegionMap.LegendEntry("EU W", regionGroupColor("EU West", palette));
        tmp0[5] = new RegionMap.LegendEntry("ASIA", regionGroupColor("Asia", palette));
        RegionMap.LegendEntry[] legend = tmp0;
        int colW = legendColW;
        int legendStartX = (x + ((panelW - (colW * 3)) / 2));
        int legendDot = Math.max(6, scaled(7, hudScale));
        int i = 0;
        while (i < legend.length) {
            int row = (i / 3);
            int col = (i % 3);
            int lx = (legendStartX + (col * colW));
            int ly = (legendStartY + (row * legendRowStep));
            TessellatorRenderer.fillRoundedRect(ctx, lx, (ly + scaled(4, hudScale)), legendDot, legendDot, Math.max(3, scaled(4, hudScale)), ClickGuiTheme.withAlpha(legend[i].color, 244));
            drawText(ctx, legend[i].label, (lx + scaled(12, hudScale)), (ly + scaled(3, hudScale)), bodySize, txtOff);
            i++;
        }
    }

    private static void putRegion(String name, int... ids) {
        Object var2 = ids;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            int id = var2[var4];
            REGION_NAMES.put(id, name);
            var4++;
        }
    }

    private static void drawText(class_332 ctx, class_327 tr, String text, int x, int y, int color) {
        TessellatorRenderer.drawTextWithShadow(ctx, tr, text, x, y, color);
    }

    private static int textWidth(class_327 tr, String text) {
        float w = ((float) TessellatorRenderer.measureTextWidth(tr, text));
        if (w > 0.0f) return Math.round(w);
        Objects.requireNonNull(tr);
        return Math.max(0, ((int) Math.ceil(((double) (((float) text.length()) * Math.max(6.0f, (9.0f * 0.58f)))))));
    }

    private static int textWidth(String text, float size) {
        return Math.max(0, TessellatorRenderer.measureTextWidth(text, size));
    }

    private static void drawText(class_332 ctx, String text, int x, int y, float size, int color) {
        TessellatorRenderer.drawTextWithShadow(ctx, text, ((float) x), ((float) y), size, color);
    }

    private static String trimToWidthScaled(String text, int maxWidth, float size) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (textWidth(text, size) <= maxWidth) return text;
        String suffix = "...";
        int suffixW = textWidth(suffix, size);
        if (suffixW >= maxWidth) return "";
        int end = text.length();
        while (end > 0 && (textWidth(text.substring(0, end), size) + suffixW) > maxWidth) {
            end--;
        }
        if (end <= 0) {
        } else {
        }
        return (text.substring(0, end) + suffix);
    }

    private static int scaled(int base, float scale) {
        return Math.max(1, Math.round((((float) base) * Math.max(0.1f, scale))));
    }

    private static int regionGroupColor(String group, ClickGuiTheme.Palette palette) {
        if (group == null) {
            int base = palette.rowBorder();
        } else {
            int themeMix = group;
            int var4 = -1;
            switch (themeMix.hashCode()) {
                case -1896556950 -> { break label_6; }
                case -1896016868 -> { break label_7; }
                case -1719043611 -> { break label_4; }
                case -721032385 -> { break label_5; }
                case 2050282 -> { break label_8; }
                case 28907126 -> { break label_9; }
                default -> { break label_10; }
            }
            label_4:
            if (!(themeMix.equals("EU Central"))) break label_10;
            var4 = 0;
            break label_10;
            label_5:
            if (!(themeMix.equals("EU West"))) break label_10;
            var4 = 1;
            break label_10;
            label_6:
            if (!(themeMix.equals("NA East"))) break label_10;
            var4 = 2;
            break label_10;
            label_7:
            if (!(themeMix.equals("NA West"))) break label_10;
            var4 = 3;
            break label_10;
            label_8:
            if (!(themeMix.equals("Asia"))) break label_10;
            var4 = 4;
            break label_10;
            label_9:
            if (!(themeMix.equals("Oceania"))) break label_10;
            var4 = 5;
            label_10:
            switch (var4) {
                case 0 -> { break label_11; }
                case 1 -> { break label_12; }
                case 2 -> { break label_13; }
                case 3 -> { break label_14; }
                case 4 -> { break label_15; }
                case 5 -> { break label_16; }
                default -> { break label_17; }
            }
            label_11:
            break label_18;
            label_12:
            break label_18;
            label_13:
            break label_18;
            label_14:
            break label_18;
            label_15:
            break label_18;
            label_16:
            break label_18;
            label_17:
            label_18:
            base = palette.rowBorder();
        }
        themeMix = blend(base, palette.accent(), 0.3f);
        return blend(themeMix, palette.rowBorder(), 0.12f);
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

    private static String legendLabel(String regionName) {
        if (regionName == null) return "UNKNOWN";
        Object var1 = regionName;
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1896556950 -> { break label_4; }
            case -1896016868 -> { break label_5; }
            case -1719043611 -> { break label_2; }
            case -721032385 -> { break label_3; }
            case 2050282 -> { break label_7; }
            case 28907126 -> { break label_6; }
            default -> { break label_8; }
        }
        label_2:
        if (!(var1.equals("EU Central"))) break label_8;
        var2 = 0;
        break label_8;
        label_3:
        if (!(var1.equals("EU West"))) break label_8;
        var2 = 1;
        break label_8;
        label_4:
        if (!(var1.equals("NA East"))) break label_8;
        var2 = 2;
        break label_8;
        label_5:
        if (!(var1.equals("NA West"))) break label_8;
        var2 = 3;
        break label_8;
        label_6:
        if (!(var1.equals("Oceania"))) break label_8;
        var2 = 4;
        break label_8;
        label_7:
        if (!(var1.equals("Asia"))) break label_8;
        var2 = 5;
        label_8:
        switch (var2) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_11; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_14; }
            default -> { break label_15; }
        }
        label_9:
        break label_16;
        label_10:
        break label_16;
        label_11:
        break label_16;
        label_12:
        break label_16;
        label_13:
        break label_16;
        label_14:
        break label_16;
        label_15:
        label_16:
        return regionName.toUpperCase();
    }

    public int getPosX() {
        return ((Integer) this.posX.get()).intValue();
    }

    public int getPosY() {
        return ((Integer) this.posY.get()).intValue();
    }

    public void setPosX(int value) {
        this.posX.set(value);
    }

    public void setPosY(int value) {
        this.posY.set(value);
    }

    public float getScale() {
        return (((float) ((Integer) this.scale.get()).intValue()) / 10.0f);
    }

    public int getHudWidth() {
        float hudScale = getScale();
        int cell = Math.max(8, scaled(14, hudScale));
        int gap = Math.max(1, scaled(1, hudScale));
        int mapW = ((9 * cell) + (8 * gap));
        int sidePad = Math.max(10, scaled(13, hudScale));
        int legendColW = Math.max(scaled(32, hudScale), (textWidth("EU CENTRAL", Math.max(6.5f, (7.0f * hudScale))) + scaled(18, hudScale)));
        return Math.max((mapW + (sidePad * 2)), ((legendColW * 3) + scaled(10, hudScale)));
    }

    public int getHudHeight() {
        float hudScale = getScale();
        int cell = Math.max(8, scaled(14, hudScale));
        int gap = Math.max(1, scaled(1, hudScale));
        int mapH = ((9 * cell) + (8 * gap));
        int legendStartY = ((scaled(24, hudScale) + mapH) + scaled(20, hudScale));
        int legendRowStep = Math.max(10, scaled(14, hudScale));
        int dynamicH = ((legendStartY + (legendRowStep * 2)) + scaled(8, hudScale));
        return Math.max(scaled(214, hudScale), dynamicH);
    }

    static {
        var tmp0 = new int[][9];
        var tmp1 = new int[9];
        tmp1[0] = 82;
        tmp1[1] = 100;
        tmp1[2] = 101;
        tmp1[3] = 102;
        tmp1[4] = 103;
        tmp1[5] = 104;
        tmp1[6] = 105;
        tmp1[7] = 106;
        tmp1[8] = 91;
        tmp0[0] = tmp1;
        var tmp2 = new int[9];
        tmp2[0] = 83;
        tmp2[1] = 44;
        tmp2[2] = 75;
        tmp2[3] = 42;
        tmp2[4] = 41;
        tmp2[5] = 40;
        tmp2[6] = 39;
        tmp2[7] = 38;
        tmp2[8] = 92;
        tmp0[1] = tmp2;
        var tmp3 = new int[9];
        tmp3[0] = 84;
        tmp3[1] = 45;
        tmp3[2] = 14;
        tmp3[3] = 13;
        tmp3[4] = 12;
        tmp3[5] = 11;
        tmp3[6] = 10;
        tmp3[7] = 37;
        tmp3[8] = 93;
        tmp0[2] = tmp3;
        var tmp4 = new int[9];
        tmp4[0] = 85;
        tmp4[1] = 46;
        tmp4[2] = 74;
        tmp4[3] = 3;
        tmp4[4] = 2;
        tmp4[5] = 1;
        tmp4[6] = 25;
        tmp4[7] = 36;
        tmp4[8] = 94;
        tmp0[3] = tmp4;
        var tmp5 = new int[9];
        tmp5[0] = 86;
        tmp5[1] = 47;
        tmp5[2] = 72;
        tmp5[3] = 71;
        tmp5[4] = 5;
        tmp5[5] = 4;
        tmp5[6] = 24;
        tmp5[7] = 35;
        tmp5[8] = 95;
        tmp0[4] = tmp5;
        var tmp6 = new int[9];
        tmp6[0] = 87;
        tmp6[1] = 51;
        tmp6[2] = 17;
        tmp6[3] = 9;
        tmp6[4] = 8;
        tmp6[5] = 7;
        tmp6[6] = 23;
        tmp6[7] = 34;
        tmp6[8] = 96;
        tmp0[5] = tmp6;
        var tmp7 = new int[9];
        tmp7[0] = 88;
        tmp7[1] = 54;
        tmp7[2] = 18;
        tmp7[3] = 61;
        tmp7[4] = 62;
        tmp7[5] = 21;
        tmp7[6] = 22;
        tmp7[7] = 33;
        tmp7[8] = 97;
        tmp0[6] = tmp7;
        var tmp8 = new int[9];
        tmp8[0] = 89;
        tmp8[1] = 26;
        tmp8[2] = 27;
        tmp8[3] = 28;
        tmp8[4] = 29;
        tmp8[5] = 30;
        tmp8[6] = 59;
        tmp8[7] = 32;
        tmp8[8] = 98;
        tmp0[7] = tmp8;
        var tmp9 = new int[9];
        tmp9[0] = 90;
        tmp9[1] = 107;
        tmp9[2] = 108;
        tmp9[3] = 109;
        tmp9[4] = 110;
        tmp9[5] = 111;
        tmp9[6] = 112;
        tmp9[7] = 113;
        tmp9[8] = 99;
        tmp0[8] = tmp9;
        REGION_GRID = tmp0;
        REGION_NAMES = new HashMap();
        var tmp10 = new int[26];
        tmp10[0] = 1;
        tmp10[1] = 2;
        tmp10[2] = 4;
        tmp10[3] = 5;
        tmp10[4] = 24;
        tmp10[5] = 25;
        tmp10[6] = 36;
        tmp10[7] = 35;
        tmp10[8] = 94;
        tmp10[9] = 95;
        tmp10[10] = 96;
        tmp10[11] = 93;
        tmp10[12] = 37;
        tmp10[13] = 10;
        tmp10[14] = 11;
        tmp10[15] = 12;
        tmp10[16] = 41;
        tmp10[17] = 40;
        tmp10[18] = 39;
        tmp10[19] = 38;
        tmp10[20] = 92;
        tmp10[21] = 91;
        tmp10[22] = 106;
        tmp10[23] = 105;
        tmp10[24] = 104;
        tmp10[25] = 103;
        putRegion("NA East", tmp10);
        var tmp11 = new int[10];
        tmp11[0] = 100;
        tmp11[1] = 101;
        tmp11[2] = 102;
        tmp11[3] = 44;
        tmp11[4] = 75;
        tmp11[5] = 42;
        tmp11[6] = 45;
        tmp11[7] = 14;
        tmp11[8] = 13;
        tmp11[9] = 3;
        putRegion("NA West", tmp11);
        var tmp12 = new int[6];
        tmp12[0] = 82;
        tmp12[1] = 83;
        tmp12[2] = 84;
        tmp12[3] = 85;
        tmp12[4] = 46;
        tmp12[5] = 74;
        putRegion("Oceania", tmp12);
        var tmp13 = new int[6];
        tmp13[0] = 86;
        tmp13[1] = 47;
        tmp13[2] = 72;
        tmp13[3] = 71;
        tmp13[4] = 87;
        tmp13[5] = 88;
        putRegion("Asia", tmp13);
        var tmp14 = new int[21];
        tmp14[0] = 89;
        tmp14[1] = 90;
        tmp14[2] = 9;
        tmp14[3] = 8;
        tmp14[4] = 7;
        tmp14[5] = 23;
        tmp14[6] = 61;
        tmp14[7] = 62;
        tmp14[8] = 21;
        tmp14[9] = 22;
        tmp14[10] = 33;
        tmp14[11] = 34;
        tmp14[12] = 97;
        tmp14[13] = 27;
        tmp14[14] = 28;
        tmp14[15] = 29;
        tmp14[16] = 30;
        tmp14[17] = 59;
        tmp14[18] = 32;
        tmp14[19] = 98;
        tmp14[20] = 99;
        putRegion("EU Central", tmp14);
        var tmp15 = new int[12];
        tmp15[0] = 51;
        tmp15[1] = 17;
        tmp15[2] = 54;
        tmp15[3] = 18;
        tmp15[4] = 26;
        tmp15[5] = 107;
        tmp15[6] = 108;
        tmp15[7] = 109;
        tmp15[8] = 110;
        tmp15[9] = 111;
        tmp15[10] = 112;
        tmp15[11] = 113;
        putRegion("EU West", tmp15);
    }


    record LegendEntry(String label, int color) {

        private LegendEntry(String label, int color) {
            this.label = label;
            this.color = color;
        }

        public String label() {
            return this.label;
        }

        public int color() {
            return this.color;
        }

    }
}

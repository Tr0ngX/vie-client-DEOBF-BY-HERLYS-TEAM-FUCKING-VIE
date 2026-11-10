package me.drake.viecheats.util;

import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_332;

public final class ClickGuiTheme {
    public static final int BG = -15657182;
    public static final int PANEL_BORDER = -13880252;
    public static final int ROW_BG = -15196371;
    public static final int ROW_BORDER = -14406598;
    public static final int ROW_HOVER = 536870911;
    public static final int ACCENT = -10897153;
    public static final int ACCENT_SOFT = -7548673;
    public static final int TGL_OFF = -13024427;
    public static final int TGL_ON = -12739585;
    public static final int TXT_ON = -657153;
    public static final int TXT_OFF = -4734767;
    public static final int SEARCH_BG = -15459543;
    public static final int OVERLAY_BG = -1610216942;

    public static ClickGuiTheme.Palette resolve() {
        ClientSettings cs = ClientSettings.get();
        if (cs == null) return new ClickGuiTheme.Palette(-10897153, -7548673, -15657182, -13880252, -15196371, -14406598, 536870911, -13024427, -12739585, -657153, -4734767, -15459543, -1610216942);
        int accent = opaque(cs.getPrimaryColor());
        int secondary = opaque(cs.getSecondaryColor());
        int txtOn = opaque(cs.resolveTextColor(0L));
        int txtOff = blend(-4734767, txtOn, 0.22f);
        int bg = blend(-15657182, secondary, 0.2f);
        int panelBorder = blend(-13880252, secondary, 0.34f);
        int rowBg = blend(-15196371, secondary, 0.22f);
        int rowBorder = blend(-14406598, secondary, 0.3f);
        int searchBg = blend(-15459543, secondary, 0.24f);
        int tglOff = blend(-13024427, secondary, 0.26f);
        int tglOn = blend(-12739585, accent, 0.4f);
        return new ClickGuiTheme.Palette(accent, blend(-7548673, accent, 0.3f), bg, panelBorder, rowBg, rowBorder, 536870911, tglOff, tglOn, txtOn, txtOff, searchBg, -1610216942);
    }

    public static void drawBorder(class_332 ctx, int x, int y, int w, int h, int color) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int a = ((color >>> 24) & 255);
        if (a == 0) return;
        float radius = Math.max(1.0f, Math.min((((float) Math.min(w, h)) * 0.28f), 4.0f));
        TessellatorRenderer.strokeRoundedRect(ctx, (((float) x) + 0.5f), (((float) y) + 0.5f), (((float) w) - 1.0f), (((float) h) - 1.0f), radius, 1.0f, color);
    }

    public static int withAlpha(int color, int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        return ((clamped << 24) | (color & 16777215));
    }

    private static int opaque(int color) {
        return (-16777216 | (color & 16777215));
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


    public record Palette(int accent, int accentSoft, int bg, int panelBorder, int rowBg, int rowBorder, int rowHover, int tglOff, int tglOn, int txtOn, int txtOff, int searchBg, int overlayBg) {

        public Palette(int accent, int accentSoft, int bg, int panelBorder, int rowBg, int rowBorder, int rowHover, int tglOff, int tglOn, int txtOn, int txtOff, int searchBg, int overlayBg) {
            this.accent = accent;
            this.accentSoft = accentSoft;
            this.bg = bg;
            this.panelBorder = panelBorder;
            this.rowBg = rowBg;
            this.rowBorder = rowBorder;
            this.rowHover = rowHover;
            this.tglOff = tglOff;
            this.tglOn = tglOn;
            this.txtOn = txtOn;
            this.txtOff = txtOff;
            this.searchBg = searchBg;
            this.overlayBg = overlayBg;
        }

        public int accent() {
            return this.accent;
        }

        public int accentSoft() {
            return this.accentSoft;
        }

        public int bg() {
            return this.bg;
        }

        public int panelBorder() {
            return this.panelBorder;
        }

        public int rowBg() {
            return this.rowBg;
        }

        public int rowBorder() {
            return this.rowBorder;
        }

        public int rowHover() {
            return this.rowHover;
        }

        public int tglOff() {
            return this.tglOff;
        }

        public int tglOn() {
            return this.tglOn;
        }

        public int txtOn() {
            return this.txtOn;
        }

        public int txtOff() {
            return this.txtOff;
        }

        public int searchBg() {
            return this.searchBg;
        }

        public int overlayBg() {
            return this.overlayBg;
        }

    }
}

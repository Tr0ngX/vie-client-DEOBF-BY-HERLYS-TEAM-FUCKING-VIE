package me.drake.viecheats.util;

import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public class HudStyle {
    private static final boolean LOW_COST_HUD_RENDER = true;
    private static final int DEF_PINK = -10897153;
    private static final int DEF_PURPLE = -12559752;
    private static final int DEF_TEXT = -657153;
    public static final int CYAN = -7548673;
    public static final int TEXT_DIM = -4734767;
    public static final int SEPARATOR = 811186431;
    public static final int PINK = -10897153;
    public static final int PURPLE = -12559752;
    public static final int TEXT_WHITE = -657153;
    public static final int BG = -15657182;
    public static final int GLOW = 341424383;

    public static int PINK() {
        ClientSettings cs = ClientSettings.get();
        if (cs != null && cs.isEnabled()) {
        } else {
        }
        return -10897153;
    }

    public static int PURPLE() {
        ClientSettings cs = ClientSettings.get();
        if (cs != null && cs.isEnabled()) {
        } else {
        }
        return -12559752;
    }

    public static int TEXT_WHITE() {
        ClientSettings cs = ClientSettings.get();
        if (cs != null && cs.isEnabled()) {
        } else {
        }
        return -657153;
    }

    public static void drawPanel(class_332 ctx, int x, int y, int w, int h) {
        int r = Math.min(12, (h / 2));
        drawPanelBase(ctx, x, y, w, h, r);
    }

    public static void drawPanelLeft(class_332 ctx, int x, int y, int w, int h) {
        drawPanel(ctx, x, y, w, h);
    }

    public static void drawPanelRight(class_332 ctx, int x, int y, int w, int h) {
        drawPanel(ctx, x, y, w, h);
    }

    public static void drawGradientH(class_332 ctx, int x, int y, int w, int h, int from, int to) {
        int segs = 8;
        int segW = Math.max((w / segs), 1);
        int i = 0;
        while (i < segs) {
            float t = (((float) i) / ((float) (segs - 1)));
            int c = blendColor(from, to, t);
            int sx = (x + (i * segW));
            if (i == (segs - 1)) {
            } else {
            }
            int ex = (sx + segW);
            TessellatorRenderer.fillRect(ctx, sx, y, ex, (y + h), c);
            i++;
        }
    }

    public static void drawSeparator(class_332 ctx, int x, int y, int w) {
        int sep = (805306368 | (PURPLE() & 16777215));
        int edge = (268435456 | (PURPLE() & 16777215));
        TessellatorRenderer.fillRect(ctx, x, y, (x + (w / 4)), (y + 1), edge);
        TessellatorRenderer.fillRect(ctx, (x + (w / 4)), y, (x + ((w * 3) / 4)), (y + 1), sep);
        TessellatorRenderer.fillRect(ctx, (x + ((w * 3) / 4)), y, (x + w), (y + 1), edge);
    }

    public static void drawBottomBar(class_332 ctx, int screenWidth, int y, int h) {
        TessellatorRenderer.fillRect(ctx, 0, y, screenWidth, (y + h), -15657182);
        drawGradientH(ctx, 0, y, screenWidth, 1, PINK(), PURPLE());
    }

    public static void drawRoundedPanel(class_332 ctx, int x, int y, int w, int h) {
        int r = Math.min(10, (h / 2));
        drawPanelBase(ctx, x, y, w, h, r);
    }

    public static void drawInnerCard(class_332 ctx, int x, int y, int w, int h, int accentColor) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int r = Math.min(8, (h / 2));
        int glow = (402653184 | (accentColor & 16777215));
        int cardBg = -938208224;
        TessellatorRenderer.fillRoundedRect(ctx, (x - 1), (y - 1), (w + 2), (h + 2), (r + 1), glow);
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, r, cardBg);
    }

    private static void drawPanelBase(class_332 ctx, int x, int y, int w, int h, int radius) {
        if (w <= 0) break label_1;
        if (h <= 0) {
            label_1:
            return;
        }
        int purple = PURPLE();
        int bg = -15657182;
        int topAccent = (805306368 | (purple & 16777215));
        int bottomAccent = (402653184 | (purple & 16777215));
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, radius, bg);
        if (w > 2 && h > 2) {
            TessellatorRenderer.fillRect(ctx, (x + 1), y, ((x + w) - 1), (y + 1), topAccent);
            TessellatorRenderer.fillRect(ctx, (x + 1), ((y + h) - 1), ((x + w) - 1), (y + h), bottomAccent);
        }
    }

    public static void drawScaled(class_332 ctx, class_327 tr, String text, float x, float y, int color, float scale) {
        TessellatorRenderer.drawTextWithShadow(ctx, tr, text, ((int) x), ((int) y), color);
    }

    public static int blendColor(int c1, int c2, float t) {
        t = class_3532.method_15363(t, 0.0f, 1.0f);
        int r = class_3532.method_48781(t, ((c1 >> 16) & 255), ((c2 >> 16) & 255));
        int g = class_3532.method_48781(t, ((c1 >> 8) & 255), ((c2 >> 8) & 255));
        int b = class_3532.method_48781(t, (c1 & 255), (c2 & 255));
        int a = class_3532.method_48781(t, ((c1 >> 24) & 255), ((c2 >> 24) & 255));
        return ((((a << 24) | (r << 16)) | (g << 8)) | b);
    }

    public static int withAlpha(int color, float alphaMul) {
        float mul = class_3532.method_15363(alphaMul, 0.0f, 1.0f);
        int a = ((color >>> 24) & 255);
        if (a == 0) {
            a = 255;
        }
        int aa = Math.max(0, Math.min(255, Math.round((((float) a) * mul))));
        return ((aa << 24) | (color & 16777215));
    }

    public static void drawProgressBar(class_332 ctx, int x, int y, int w, int h, float progress, int bgColor, int fillColor) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int r = Math.min(Math.max(2, (h / 2)), 10);
        float p = class_3532.method_15363(progress, 0.0f, 1.0f);
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, r, bgColor);
        int fw = Math.max(0, Math.min(w, Math.round((((float) w) * p))));
        if (fw > 0) {
            TessellatorRenderer.fillRoundedRect(ctx, x, y, fw, h, r, fillColor);
        }
    }

}

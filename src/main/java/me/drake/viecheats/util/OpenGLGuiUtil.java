package me.drake.viecheats.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.drake.viecheats.render.ImmediateGuiRenderer;
import net.minecraft.class_332;

public final class OpenGLGuiUtil {
    private static final int MAX_CACHED_RADIUS = 64;
    private static final int[][] CACHED_CORNER_INSETS;
    private static final int[][] CACHED_CORNER_AA;

    public static void fillRoundedRect(class_332 ctx, int x, int y, int w, int h, int radius, int color) {
        fillRoundedRectVarying(ctx, x, y, w, h, radius, radius, radius, radius, color);
    }

    public static void fillRoundedRect(class_332 ctx, int x, int y, int w, int h, float radius, int color) {
        fillRoundedRect(ctx, x, y, w, h, Math.round(radius), color);
    }

    public static void fillRoundedRectVarying(class_332 ctx, int x, int y, int w, int h, int tl, int tr, int br, int bl, int color) {
        fillRoundedRectVarying(ctx, ((RenderPipeline) null), x, y, w, h, tl, tr, br, bl, color);
    }

    public static void fillRoundedRectVarying(class_332 ctx, int x, int y, int w, int h, float tl, float tr, float br, float bl, int color) {
        fillRoundedRectVarying(ctx, x, y, w, h, Math.round(tl), Math.round(tr), Math.round(br), Math.round(bl), color);
    }

    public static void fillRoundedRectVarying(class_332 ctx, RenderPipeline pipeline, int x, int y, int w, int h, int tl, int tr, int br, int bl, int color) {
        if (w <= 0) break label_1;
        if (h > 0) break label_2;
        label_1:
        return;
        label_2:
        int[] radii = normalizeRadii(w, h, tl, tr, br, bl);
        tl = radii[0];
        tr = radii[1];
        br = radii[2];
        bl = radii[3];
        if ((((tl | tr) | br) | bl) == 0) {
            ImmediateGuiRenderer.fillRect(ctx, x, y, (x + w), (y + h), color);
            return;
        }
        int baseA = ((color >>> 24) & 255);
        int rgb = (color & 16777215);
        int row = 0;
        while (row < h) {
            int leftInset = insetForRow(row, h, tl, bl);
            int rightInset = insetForRow(row, h, tr, br);
            int sx = (x + leftInset);
            int ex = ((x + w) - rightInset);
            if (ex > sx) {
                ImmediateGuiRenderer.fillRect(ctx, sx, (y + row), ex, ((y + row) + 1), color);
            }
            if (baseA <= 0) {
                break label_35;
            }
            int leftWeight = aaWeightForRow(row, h, tl, bl);
            if ((leftWeight <= 0) || (leftInset <= 0)) break label_28;
            int aaAlpha = edgeAaAlpha(baseA, leftWeight);
            if (aaAlpha <= 1) break label_28;
            int aaColor = ((aaAlpha << 24) | rgb);
            int aaX = (sx - 1);
            ImmediateGuiRenderer.fillRect(ctx, aaX, (y + row), (aaX + 1), ((y + row) + 1), aaColor);
            label_28:
            int rightWeight = aaWeightForRow(row, h, tr, br);
            if ((rightWeight <= 0) || (rightInset <= 0)) break label_35;
            aaAlpha = edgeAaAlpha(baseA, rightWeight);
            if (aaAlpha <= 1) break label_35;
            aaColor = ((aaAlpha << 24) | rgb);
            aaX = ex;
            ImmediateGuiRenderer.fillRect(ctx, aaX, (y + row), (aaX + 1), ((y + row) + 1), aaColor);
            label_35:
            row++;
        }
    }

    public static void fillRoundedRectSmooth(class_332 ctx, int x, int y, int w, int h, int radius, int color) {
        fillRoundedRectSmooth(ctx, ((RenderPipeline) null), x, y, w, h, radius, color, 2);
    }

    public static void fillRoundedRectSmooth(class_332 ctx, int x, int y, int w, int h, int radius, int color, int softness) {
        fillRoundedRectSmooth(ctx, ((RenderPipeline) null), x, y, w, h, radius, color, softness);
    }

    public static void fillRoundedRectSmooth(class_332 ctx, RenderPipeline pipeline, int x, int y, int w, int h, int radius, int color, int softness) {
        if (w <= 0) break label_1;
        if (h > 0) break label_2;
        label_1:
        return;
        label_2:
        fillRoundedRectVarying(ctx, pipeline, x, y, w, h, radius, radius, radius, radius, color);
        int baseA = ((color >>> 24) & 255);
        if ((baseA <= 0) || (softness <= 0)) {
            return;
        }
        int rgb = (color & 16777215);
        int layers = Math.max(0, Math.min(3, softness));
        int minSize = Math.min(w, h);
        if (minSize <= 10) {
            layers = Math.min(layers, 1);
        }
        if (minSize > 6) {
            if (radius <= 1) {
            }
            layers = 0;
        }
        if (baseA < 56) {
            layers = Math.min(layers, 1);
        }
        if (baseA < 26) {
            layers = 0;
        }
        int i = 1;
        while (i <= layers) {
            int aaAlpha = Math.round((((float) baseA) * (0.15f / (((float) i) + 0.45f))));
            if (aaAlpha < 2) {
                break label_21;
            }
            int aaColor = ((aaAlpha << 24) | rgb);
            fillRoundedRectVarying(ctx, pipeline, (x - i), (y - i), (w + (i * 2)), (h + (i * 2)), (radius + i), (radius + i), (radius + i), (radius + i), aaColor);
            i++;
        }
        label_21:
    }

    public static void strokeRoundedRect(class_332 ctx, int x, int y, int w, int h, int radius, int stroke, int color) {
        strokeRoundedRect(ctx, ((RenderPipeline) null), x, y, w, h, radius, stroke, color);
    }

    public static void strokeRoundedRect(class_332 ctx, RenderPipeline pipeline, int x, int y, int w, int h, int radius, int stroke, int color) {
        if ((w <= 0) || (h <= 0)) break label_1;
        if (stroke > 0) break label_2;
        label_1:
        return;
        label_2:
        int s = Math.max(1, stroke);
        int i = 0;
        while (i < s) {
            int xx = (x + i);
            int yy = (y + i);
            int ww = (w - (i * 2));
            int hh = (h - (i * 2));
            if (ww <= 0) break label_40;
            if (hh <= 0) {
                break label_40;
            }
            int rr = Math.max(0, (radius - i));
            int[] normalized = normalizeRadii(ww, hh, rr, rr, rr, rr);
            int tl = normalized[0];
            int tr = normalized[1];
            int br = normalized[2];
            int bl = normalized[3];
            if ((((tl | tr) | br) | bl) == 0) {
                if (ww != 1) {
                    if (hh == 1) {
                    }
                    ImmediateGuiRenderer.fillRect(ctx, xx, yy, (xx + ww), (yy + hh), color);
                    break label_39;
                }
                ImmediateGuiRenderer.fillRect(ctx, xx, yy, (xx + ww), (yy + 1), color);
                ImmediateGuiRenderer.fillRect(ctx, xx, ((yy + hh) - 1), (xx + ww), (yy + hh), color);
                ImmediateGuiRenderer.fillRect(ctx, xx, (yy + 1), (xx + 1), ((yy + hh) - 1), color);
                ImmediateGuiRenderer.fillRect(ctx, ((xx + ww) - 1), (yy + 1), (xx + ww), ((yy + hh) - 1), color);
                break label_39;
            }
            int row = 0;
            while (row < hh) {
                int leftInset = insetForRow(row, hh, tl, bl);
                int rightInset = insetForRow(row, hh, tr, br);
                int sx = (xx + leftInset);
                int ex = ((xx + ww) - rightInset);
                if (ex <= sx) {
                    break label_38;
                }
                int py = (yy + row);
                if (row != 0) {
                    if (row == (hh - 1)) {
                    }
                    ImmediateGuiRenderer.fillRect(ctx, sx, py, ex, (py + 1), color);
                    break label_38;
                }
                ImmediateGuiRenderer.fillRect(ctx, sx, py, (sx + 1), (py + 1), color);
                if ((ex - sx) <= 1) break label_38;
                ImmediateGuiRenderer.fillRect(ctx, (ex - 1), py, ex, (py + 1), color);
                label_38:
                row++;
            }
            label_39:
            i++;
        }
        label_40:
    }

    private static int[] normalizeRadii(int w, int h, int tl, int tr, int br, int bl) {
        int half = (Math.min(w, h) / 2);
        tl = Math.max(0, Math.min(tl, half));
        tr = Math.max(0, Math.min(tr, half));
        br = Math.max(0, Math.min(br, half));
        bl = Math.max(0, Math.min(bl, half));
        float scale = 1.0f;
        int top = (tl + tr);
        if (top > w && top > 0) {
            scale = Math.min(scale, (((float) w) / ((float) top)));
        }
        int bottom = (bl + br);
        if (bottom > w && bottom > 0) {
            scale = Math.min(scale, (((float) w) / ((float) bottom)));
        }
        int left = (tl + bl);
        if (left > h && left > 0) {
            scale = Math.min(scale, (((float) h) / ((float) left)));
        }
        int right = (tr + br);
        if (right > h && right > 0) {
            scale = Math.min(scale, (((float) h) / ((float) right)));
        }
        if (scale < 1.0f) {
            tl = ((int) Math.floor(((double) (((float) tl) * scale))));
            tr = ((int) Math.floor(((double) (((float) tr) * scale))));
            br = ((int) Math.floor(((double) (((float) br) * scale))));
            bl = ((int) Math.floor(((double) (((float) bl) * scale))));
        }
        tl = Math.min(tl, Math.max(0, (w - tr)));
        tr = Math.min(tr, Math.max(0, (w - tl)));
        bl = Math.min(bl, Math.max(0, (w - br)));
        br = Math.min(br, Math.max(0, (w - bl)));
        tl = Math.min(tl, Math.max(0, (h - bl)));
        bl = Math.min(bl, Math.max(0, (h - tl)));
        tr = Math.min(tr, Math.max(0, (h - br)));
        br = Math.min(br, Math.max(0, (h - tr)));
        var tmp0 = new int[4];
        tmp0[0] = tl;
        tmp0[1] = tr;
        tmp0[2] = br;
        tmp0[3] = bl;
        return tmp0;
    }

    private static int insetForRow(int row, int h, int topRadius, int bottomRadius) {
        if (topRadius > 0 && row < topRadius) return getCornerInsets(topRadius)[row];
        if (bottomRadius > 0 && row >= (h - bottomRadius)) {
            int idx = ((h - 1) - row);
            return getCornerInsets(bottomRadius)[idx];
        }
        return 0;
    }

    private static int aaWeightForRow(int row, int h, int topRadius, int bottomRadius) {
        if (topRadius > 0 && row < topRadius) return getCornerAaWeights(topRadius)[row];
        if (bottomRadius > 0 && row >= (h - bottomRadius)) {
            int idx = ((h - 1) - row);
            return getCornerAaWeights(bottomRadius)[idx];
        }
        return 0;
    }

    private static int edgeAaAlpha(int baseAlpha, int weight) {
        if ((baseAlpha <= 0) || (weight <= 0)) {
            return 0;
        }
        float coverage = ((((float) weight) / 255.0f) * 0.9f);
        int aa = Math.round((((float) baseAlpha) * coverage));
        if (aa >= baseAlpha) {
            aa = (baseAlpha - 1);
        }
        return Math.max(0, aa);
    }

    private static int[] getCornerInsets(int radius) {
        if (radius <= 0) return new int[0];
        if (radius > 64) return buildCornerInsets(radius);
        int[] cached = CACHED_CORNER_INSETS[radius];
        if (cached != null) return cached;
        Object var2 = CACHED_CORNER_INSETS;
        // synchronized (CACHED_CORNER_INSETS)
        cached = CACHED_CORNER_INSETS[radius];
        if (cached == null) {
            CACHED_CORNER_INSETS[radius] = buildCornerInsets(radius);
            CACHED_CORNER_AA[radius] = buildCornerAaWeights(radius, CACHED_CORNER_INSETS[radius]);
            cached = CACHED_CORNER_INSETS[radius];
        }
        break label_14;
        Object var3 = null;
        throw var3;
        label_14:
        return cached;
    }

    private static int[] getCornerAaWeights(int radius) {
        if (radius <= 0) return new int[0];
        if (radius > 64) {
            int[] insets = buildCornerInsets(radius);
            return buildCornerAaWeights(radius, insets);
        }
        int[] cached = CACHED_CORNER_AA[radius];
        if (cached != null) return cached;
        Object var2 = CACHED_CORNER_INSETS;
        // synchronized (CACHED_CORNER_INSETS)
        cached = CACHED_CORNER_AA[radius];
        if (cached == null) {
            CACHED_CORNER_INSETS[radius] = buildCornerInsets(radius);
            CACHED_CORNER_AA[radius] = buildCornerAaWeights(radius, CACHED_CORNER_INSETS[radius]);
            cached = CACHED_CORNER_AA[radius];
        }
        break label_16;
        Object var3 = null;
        throw var3;
        label_16:
        return cached;
    }

    private static int[] buildCornerInsets(int radius) {
        int[] result = new int[radius];
        double rr = (((double) radius) * ((double) radius));
        int i = 0;
        while (i < radius) {
            double dy = (((double) (radius - i)) - 0.5);
            double exactInset = (((double) radius) - Math.sqrt(Math.max(0.0, (rr - (dy * dy)))));
            int inset = ((int) Math.ceil((exactInset - 0.5)));
            if (inset < 0) {
                inset = 0;
            }
            if (inset > radius) {
                inset = radius;
            }
            result[i] = inset;
            i++;
        }
        return result;
    }

    private static int[] buildCornerAaWeights(int radius, int[] insets) {
        int[] result = new int[radius];
        double rr = (((double) radius) * ((double) radius));
        int i = 0;
        while (i < radius) {
            double dy = (((double) (radius - i)) - 0.5);
            double exactInset = (((double) radius) - Math.sqrt(Math.max(0.0, (rr - (dy * dy)))));
            double fraction = Math.max(0.0, Math.min(1.0, ((((double) insets[i]) + 0.5) - exactInset)));
            result[i] = ((int) Math.round((fraction * 255.0)));
            i++;
        }
        return result;
    }

    static {
        CACHED_CORNER_INSETS = new int[][65];
        CACHED_CORNER_AA = new int[][65];
    }

}

package me.drake.viecheats.render;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.class_1043;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class TessellatorRenderer {
    private static final boolean ONE_LAYER_UI = true;
    private static final float UI_TEXT_SCALE = 1.0f;
    private static class_332 ctx;

    private static float softenedRadius(float radius, int w, int h) {
        float clamped = Math.max(0.0f, radius);
        float maxBySize = Math.max(1.0f, (((float) Math.min(w, h)) * 0.32f));
        float maxRadius = Math.min(maxBySize, 5.0f);
        return Math.min(clamped, maxRadius);
    }

    private static int softenedRadiusInt(int radius, int w, int h) {
        return Math.max(0, Math.round(softenedRadius(((float) radius), w, h)));
    }

    public static void setContext(class_332 c) {
        if (ctx != c) {
            SdfRoundedRectRenderer.flipFrame();
        }
        ctx = c;
    }

    public static boolean isContextValid() {
        return true;
    }

    public static boolean isInFrame() {
        return true;
    }

    public static boolean beginFrame(float scale, int w, int h) {
        return true;
    }

    public static void endFrame() {
    }

    public static void scissor(int x, int y, int w, int h) {
    }

    public static void scissor(class_332 c, int x, int y, int w, int h) {
    }

    public static void resetScissor() {
    }

    public static void fillRect(class_332 c, int x1, int y1, int x2, int y2, int argb) {
        ImmediateGuiRenderer.fillRect(c, x1, y1, x2, y2, argb);
    }

    public static void fillRect(int x1, int y1, int x2, int y2, int argb) {
        ImmediateGuiRenderer.fillRect(ctx, x1, y1, x2, y2, argb);
    }

    public static void fillRectSharp(class_332 c, int x, int y, int w, int h, int argb) {
        fillRect(c, x, y, (x + w), (y + h), argb);
    }

    public static void fillRectSharp(int x, int y, int w, int h, int argb) {
        fillRect(x, y, (x + w), (y + h), argb);
    }

    public static void fillRoundedRect(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int ri = softenedRadiusInt(radius, w, h);
        if (ri <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        if (SdfRoundedRectRenderer.draw(target, x, y, w, h, ri, argb)) return;
        ImmediateGuiRenderer.fillRect(target, x, y, (x + w), (y + h), argb);
    }

    public static void fillRoundedRect(class_332 c, int x, int y, int w, int h, float radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        float rr = softenedRadius(radius, w, h);
        if (rr <= 0.0f) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        if (SdfRoundedRectRenderer.draw(target, x, y, w, h, rr, argb)) return;
        ImmediateGuiRenderer.fillRect(target, x, y, (x + w), (y + h), argb);
    }

    public static void fillRoundedRect(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRect(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRect(int x, int y, int w, int h, float radius, int argb) {
        fillRoundedRect(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectSmooth(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRect(c, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectSmooth(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRect(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectSmooth(class_332 c, int x, int y, int w, int h, float radius, int argb) {
        fillRoundedRectSmooth(c, x, y, w, h, ((int) radius), argb);
    }

    public static void fillRoundedRectSmooth(int x, int y, int w, int h, float radius, int argb) {
        fillRoundedRectSmooth(x, y, w, h, ((int) radius), argb);
    }

    public static void fillRoundedRectVarying(class_332 c, int x, int y, int w, int h, float tl, float tr, float br, float bl, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        float ctl = softenedRadius(tl, w, h);
        float ctr = softenedRadius(tr, w, h);
        float cbr = softenedRadius(br, w, h);
        float cbl = softenedRadius(bl, w, h);
        if (SdfRoundedRectRenderer.drawVarying(target, x, y, w, h, ctl, ctr, cbr, cbl, argb)) return;
        ImmediateGuiRenderer.fillRect(target, x, y, (x + w), (y + h), argb);
    }

    public static void fillRoundedRectVarying(int x, int y, int w, int h, float tl, float tr, float br, float bl, int argb) {
        fillRoundedRectVarying(ctx, x, y, w, h, tl, tr, br, bl, argb);
    }

    public static void fillRoundedRectTop(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = softenedRadiusInt(radius, w, h);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, x, (y + r), (x + w), (y + h), argb);
        fillRect(target, (x + r), y, ((x + w) - r), (y + r), argb);
        fillRect(target, x, y, (x + r), (y + r), argb);
        fillRect(target, ((x + w) - r), y, (x + w), (y + r), argb);
    }

    public static void fillRoundedRectTop(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectTop(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectBottom(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = softenedRadiusInt(radius, w, h);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, x, y, (x + w), ((y + h) - r), argb);
        fillRect(target, (x + r), ((y + h) - r), ((x + w) - r), (y + h), argb);
        fillRect(target, x, ((y + h) - r), (x + r), (y + h), argb);
        fillRect(target, ((x + w) - r), ((y + h) - r), (x + w), (y + h), argb);
    }

    public static void fillRoundedRectBottom(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectBottom(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectLeft(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = softenedRadiusInt(radius, w, h);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, (x + r), y, (x + w), (y + h), argb);
        fillRect(target, x, (y + r), (x + r), ((y + h) - r), argb);
        fillRect(target, x, y, (x + r), (y + r), argb);
        fillRect(target, x, ((y + h) - r), (x + r), (y + h), argb);
    }

    public static void fillRoundedRectLeft(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectLeft(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectLeftOnly(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = softenedRadiusInt(radius, w, h);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, (x + r), y, (x + w), (y + h), argb);
        fillRect(target, x, (y + r), (x + r), ((y + h) - r), argb);
        fillRect(target, x, y, (x + r), (y + r), argb);
        fillRect(target, x, ((y + h) - r), (x + r), (y + h), argb);
    }

    public static void fillRoundedRectRightOnly(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = softenedRadiusInt(radius, w, h);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, x, y, ((x + w) - r), (y + h), argb);
        fillRect(target, ((x + w) - r), y, (x + w), (y + r), argb);
        fillRect(target, ((x + w) - r), ((y + h) - r), (x + w), (y + h), argb);
        fillRect(target, x, (y + r), ((x + w) - r), ((y + h) - r), argb);
    }

    public static void fillRoundedRectLeftOnly(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectLeftOnly(ctx, x, y, w, h, radius, argb);
    }

    public static void fillRoundedRectRightOnly(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectRightOnly(ctx, x, y, w, h, radius, argb);
    }

    public static void fillGradientRect(class_332 c, int x, int y, int w, int h, int startColor, int endColor, boolean vertical) {
        if (((c == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        ImmediateGuiRenderer.fillGradientRect(c, x, y, w, h, startColor, endColor, vertical);
    }

    public static void fillGradientRect(int x, int y, int w, int h, int startColor, int endColor, boolean vertical) {
        fillGradientRect(ctx, x, y, w, h, startColor, endColor, vertical);
    }

    private static void fillRoundedRectGradient0(class_332 c, int x, int y, int w, int h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRect(c, x, y, w, h, radius, startColor);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, int w, int h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, h, radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, int w, int h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, h, ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, int w, float h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, int w, float h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, float h, int w, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, float h, int w, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, w, ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, float h, float w, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, ((int) w), ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(class_332 c, int x, int y, float h, float w, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(c, x, y, ((int) w), ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, int w, int h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, h, radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, int w, int h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, h, ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, int w, float h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, int w, float h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, float h, int w, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, float h, int w, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, w, ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, float h, float w, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, ((int) w), ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, int y, float h, float w, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, y, ((int) w), ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, int w, int h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), w, h, radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, int w, int h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), w, h, ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, int w, float h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), w, ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, int w, float h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), w, ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, float w, int h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), ((int) w), h, radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, float w, int h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), ((int) w), h, ((int) radius), startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, float w, float h, int radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), ((int) w), ((int) h), radius, startColor, endColor, vertical);
    }

    public static void fillRoundedRectGradient(int x, float y, float w, float h, float radius, int startColor, int endColor, boolean vertical) {
        fillRoundedRectGradient0(ctx, x, ((int) y), ((int) w), ((int) h), ((int) radius), startColor, endColor, vertical);
    }

    private static void strokeRoundedRect0(class_332 c, int x, int y, int w, int h, float radius, float stroke, int argb) {
    }

    public static void strokeRoundedRect(class_332 c, int x, int y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, y, w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, int y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, y, w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, int y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, y, ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, int y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, y, ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, float y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, ((int) y), w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, float y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, ((int) y), w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, float y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, ((int) y), ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, int x, float y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, x, ((int) y), ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, int y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), y, w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, int y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), y, w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, int y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), y, ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, int y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), y, ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, float y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), ((int) y), w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, float y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), ((int) y), w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, float y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), ((int) y), ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(class_332 c, float x, float y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(c, ((int) x), ((int) y), ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, int y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, y, w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, int y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, y, w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, int y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, y, ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, int y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, y, ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, float y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, ((int) y), w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, float y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, ((int) y), w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, float y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, ((int) y), ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(int x, float y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, x, ((int) y), ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, int y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), y, w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, int y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), y, w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, int y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), y, ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, int y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), y, ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, float y, int w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), ((int) y), w, h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, float y, int w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), ((int) y), w, ((int) h), radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, float y, float w, int h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), ((int) y), ((int) w), h, radius, stroke, argb);
    }

    public static void strokeRoundedRect(float x, float y, float w, float h, float radius, float stroke, int argb) {
        strokeRoundedRect0(ctx, ((int) x), ((int) y), ((int) w), ((int) h), radius, stroke, argb);
    }

    public static void drawCircle(class_332 c, float cx, float cy, float radius, int argb) {
        int r = ((int) radius);
        fillRoundedRect(c, ((int) (cx - radius)), ((int) (cy - radius)), (r * 2), (r * 2), r, argb);
    }

    public static void drawCircle(float cx, float cy, float radius, int argb) {
        drawCircle(ctx, cx, cy, radius, argb);
    }

    public static void drawLine(class_332 c, float x1, float y1, float x2, float y2, float stroke, int argb) {
        ImmediateGuiRenderer.drawLine(c, x1, y1, x2, y2, stroke, argb);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float stroke, int argb) {
        drawLine(ctx, x1, y1, x2, y2, stroke, argb);
    }

    public static void drawTextWithShadow(class_332 c, class_327 tr, String text, int x, int y, int argb) {
        if ((text == null) || (text.isEmpty())) {
            return;
        }
        int a = ((argb >>> 24) & 255);
        if (a == 0) return;
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (target == null) return;
        float sizePx = resolveUiTextSize(tr);
        ExternalGlTextRenderer.drawText(target, text, x, y, argb, false, sizePx);
    }

    public static void drawTextWithShadow(class_332 c, String text, float x, float y, float size, int argb) {
        if ((text == null) || (text.isEmpty())) {
            return;
        }
        int a = ((argb >>> 24) & 255);
        if (a == 0) return;
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (target == null) return;
        ExternalGlTextRenderer.drawText(target, text, Math.round(x), Math.round(y), argb, false, size);
    }

    public static void drawTextWithShadow(String text, float x, float y, float size, int argb) {
        drawTextWithShadow(ctx, text, x, y, size, argb);
    }

    public static int measureTextWidth(class_327 tr, String text) {
        if ((text == null) || (text.isEmpty())) {
            return 0;
        }
        float sizePx = resolveUiTextSize(tr);
        int width = ExternalGlTextRenderer.measureTextWidth(text, sizePx);
        if (width > 0) return width;
        if (tr != null) {
        } else {
        }
        return 0;
    }

    public static int measureTextWidth(String text, float size) {
        if ((text == null) || (text.isEmpty())) {
            return 0;
        }
        int width = ExternalGlTextRenderer.measureTextWidth(text, size);
        if (width > 0) return width;
        class_310 mc = class_310.method_1551();
        if ((mc == null) || (mc.field_1772 == null)) {
            return 0;
        }
        return mc.field_1772.method_1727(text);
    }

    public static String trimToWidth(class_327 tr, String text, int maxWidth) {
        if ((text == null) || (text.isEmpty())) break label_1;
        if (maxWidth <= 0) {
            label_1:
            return "";
        }
        if (measureTextWidth(tr, text) <= maxWidth) return text;
        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = (((lo + hi) + 1) >>> 1);
            String candidate = text.substring(0, mid);
            if (measureTextWidth(tr, candidate) <= maxWidth) {
                lo = mid;
            } else {
                hi = (mid - 1);
            }
        }
        if (lo <= 0) {
        } else {
        }
        return text.substring(0, lo);
    }

    private static float resolveUiTextSize(class_327 tr) {
        if (tr != null) {
            Objects.requireNonNull(tr);
        } else {
        }
        float base = 9.0f;
        return Math.max(6.0f, (base * 1.0f));
    }

    private static void drawText0(class_332 target, class_327 tr, String text, int x, int y, int color, boolean shadow) {
        if ((((target == null) || (tr == null)) || (text == null)) || (text.isEmpty())) {
            return;
        }
        if (!(!shadow)) {
            target.method_51433(tr, text, x, y, color, true);
            return;
        }
        int a = ((color >>> 24) & 255);
        int r = ((color >>> 16) & 255);
        int g = ((color >>> 8) & 255);
        int b = (color & 255);
        target.method_51433(tr, text, x, y, ((((a << 24) | (r << 16)) | (g << 8)) | b), false);
    }

    public static void drawText(class_332 ctx, class_327 tr, String text, int x, int y, int color) {
        drawText0(ctx, tr, text, x, y, color, true);
    }

    public static void drawText(class_332 ctx, class_327 tr, String text, int x, int y, int color, boolean shadow) {
        drawText0(ctx, tr, text, x, y, color, shadow);
    }

    public static void fillLine(class_332 c, float x1, float y1, float x2, float y2, float thickness, int color) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (target == null) return;
        float dx = (x2 - x1);
        float dy = (y2 - y1);
        float len = ((float) Math.sqrt(((double) ((dx * dx) + (dy * dy)))));
        if (len < 1.0f) return;
        int steps = Math.max(1, ((int) len));
        int i = 0;
        while (i < steps) {
            float t = (((float) i) / ((float) steps));
            float px = (x1 + (dx * t));
            float py = (y1 + (dy * t));
            int ix = Math.round(px);
            int iy = Math.round(py);
            int thick = Math.max(1, ((int) thickness));
            fillRectSharp(target, ix, iy, thick, thick, color);
            i++;
        }
    }

    public static void fillRoundedRectRight(class_332 c, int x, int y, int w, int h, int radius, int argb) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (((target == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = Math.max(0, radius);
        if (r <= 0) {
            fillRect(target, x, y, (x + w), (y + h), argb);
            return;
        }
        fillRect(target, x, y, ((x + w) - r), (y + h), argb);
        fillRect(target, ((x + w) - r), y, (x + w), (y + r), argb);
        fillRect(target, ((x + w) - r), ((y + h) - r), (x + w), (y + h), argb);
    }

    public static void fillRoundedRectRight(int x, int y, int w, int h, int radius, int argb) {
        fillRoundedRectRight(ctx, x, y, w, h, radius, argb);
    }

    public static void fillTriangle(class_332 c, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        class_332 target = (c != null ? c : TessellatorRenderer.ctx);
        if (target == null) {
            return;
            try {
            }
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.field_1773 != null) {
                fillTriangleViaMesh(target, x1, y1, x2, y2, x3, y3, color);
            }
        } catch (Throwable ex8) {
            mc = ex8;
        }
    }

    private static void fillTriangleViaMesh(class_332 target, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        if ((target == null) || (((color >>> 24) & 255) == 0)) {
            return;
        }
        int sw = target.method_51421();
        int sh = target.method_51443();
        if (sw > 0) {
            if (sh <= 0) {
            }
            return;
            try {
            }
            Matrix3x2fStack matrices = target.method_51448();
            Vector2f p1 = matrices.transformPosition(x1, y1, new Vector2f());
            Vector2f p2 = matrices.transformPosition(x2, y2, new Vector2f());
            Vector2f p3 = matrices.transformPosition(x3, y3, new Vector2f());
            float nx1 = toNdcX(p1.x, sw);
            float ny1 = toNdcY(p1.y, sh);
            float nx2 = toNdcX(p2.x, sw);
            float ny2 = toNdcY(p2.y, sh);
            float nx3 = toNdcX(p3.x, sw);
            float ny3 = toNdcY(p3.y, sh);
            int r = ((color >> 16) & 255);
            int g = ((color >> 8) & 255);
            int b = (color & 255);
            int a = ((color >> 24) & 255);
            VieMeshBuilder mesh = new VieMeshBuilder(ViePipelines.HUD_COLOR);
            mesh.beginNoCamera();
            mesh.vec3(((double) nx1), ((double) ny1), 0.0).color(r, g, b, a).next();
            mesh.vec3(((double) nx2), ((double) ny2), 0.0).color(r, g, b, a).next();
            mesh.vec3(((double) nx3), ((double) ny3), 0.0).color(r, g, b, a).next();
            mesh.vec3(((double) nx3), ((double) ny3), 0.0).color(r, g, b, a).next();
            mesh.quad(0, 1, 2, 3);
            mesh.end();
            if (!(mesh.hasData())) {
                return;
                try {
                }
                class_310 mc = class_310.method_1551();
                if (mc != null) {
                    if (mc.method_1522() == null) {
                    }
                    return;
                    try {
                    }
                    RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie Triangle", mc.method_1522().method_71639(), OptionalInt.empty());
                    try {
                        pass.setPipeline(ViePipelines.HUD_COLOR);
                        pass.setVertexBuffer(0, mesh.getVertexBuffer());
                        pass.setIndexBuffer(mesh.getIndexBuffer(), VertexFormat.class_5595.field_27373);
                        pass.drawIndexed(0, 0, mesh.getIndicesCount(), 1);
                        if (pass == null) break label_45;
                        pass.close();
                    } catch (Throwable ex40) {
                        Object var27 = ex40;
                        if (pass == null) break label_44;
                        try {
                            pass.close();
                        } catch (Throwable ex43) {
                            Object var28 = ex43;
                            var27.addSuppressed(var28);
                        }
                        label_44:
                        throw var27;
                    }
                    label_45:
                } catch (Throwable ex46) {
                    matrices = ex46;
                }
            }
        }
    }

    public static void drawTexturedRect(class_332 c, class_1043 tex, int x, int y, int w, int h, int argb) {
        if ((((c == null) || (tex == null)) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int a = ((argb >>> 24) & 255);
        if (a == 0) return;
        int sw = c.method_51421();
        int sh = c.method_51443();
        if (sw > 0) {
            if (sh <= 0) {
            }
            return;
            try {
            }
            Matrix3x2fStack matrices = c.method_51448();
            Vector2f p0 = matrices.transformPosition(((float) x), ((float) y), new Vector2f());
            Vector2f p1 = matrices.transformPosition(((float) (x + w)), ((float) y), new Vector2f());
            Vector2f p2 = matrices.transformPosition(((float) (x + w)), ((float) (y + h)), new Vector2f());
            Vector2f p3 = matrices.transformPosition(((float) x), ((float) (y + h)), new Vector2f());
            int r = ((argb >> 16) & 255);
            int g = ((argb >> 8) & 255);
            int b = (argb & 255);
            VieMeshBuilder mesh = new VieMeshBuilder(ViePipelines.HUD_TEX);
            mesh.beginNoCamera();
            mesh.vec3(((double) toNdcX(p0.x, sw)), ((double) toNdcY(p0.y, sh)), 0.0).uv(0.0f, 0.0f).color(r, g, b, a).next();
            mesh.vec3(((double) toNdcX(p1.x, sw)), ((double) toNdcY(p1.y, sh)), 0.0).uv(1.0f, 0.0f).color(r, g, b, a).next();
            mesh.vec3(((double) toNdcX(p2.x, sw)), ((double) toNdcY(p2.y, sh)), 0.0).uv(1.0f, 1.0f).color(r, g, b, a).next();
            mesh.vec3(((double) toNdcX(p3.x, sw)), ((double) toNdcY(p3.y, sh)), 0.0).uv(0.0f, 1.0f).color(r, g, b, a).next();
            mesh.quad(0, 1, 2, 3);
            mesh.end();
            if (!(mesh.hasData())) {
                return;
                try {
                }
                class_310 mc = class_310.method_1551();
                if (mc != null) {
                    if (mc.method_1522() == null) {
                    }
                    return;
                    try {
                    }
                    RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie AlbumArt", mc.method_1522().method_71639(), OptionalInt.empty());
                    try {
                        pass.setPipeline(ViePipelines.HUD_TEX);
                        pass.bindTexture("Sampler0", tex.method_71659(), tex.method_75484());
                        pass.setVertexBuffer(0, mesh.getVertexBuffer());
                        pass.setIndexBuffer(mesh.getIndexBuffer(), VertexFormat.class_5595.field_27373);
                        pass.drawIndexed(0, 0, mesh.getIndicesCount(), 1);
                        if (pass == null) break label_42;
                        pass.close();
                    } catch (Throwable ex37) {
                        Object var21 = ex37;
                        if (pass == null) break label_41;
                        try {
                            pass.close();
                        } catch (Throwable ex40) {
                            Object var22 = ex40;
                            var21.addSuppressed(var22);
                        }
                        label_41:
                        throw var21;
                    }
                    label_42:
                } catch (Throwable ex43) {
                    matrices = ex43;
                }
            }
        }
    }

    private static float toNdcX(float x, int screenW) {
        return (((x / ((float) screenW)) * 2.0f) - 1.0f);
    }

    private static float toNdcY(float y, int screenH) {
        return (1.0f - ((y / ((float) screenH)) * 2.0f));
    }

}

package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_11280;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class SdfRoundedRectRenderer {
    private static final int ROUND_UBO_SIZE;
    private static final SdfRoundedRectRenderer.RoundData ROUND_DATA;
    private static class_11280 roundStorage;
    private static final VieMeshBuilder QUAD;
    private static boolean disabled;
    private static boolean warned;

    public static void flipFrame() {
        if (roundStorage != null) {
            roundStorage.method_71100();
        }
    }

    public static boolean draw(class_332 ctx, int x, int y, int w, int h, int radius, int color) {
        return drawInternal(ctx, x, y, w, h, ((float) radius), ((float) radius), ((float) radius), ((float) radius), color, 0.0f, 0.0f);
    }

    public static boolean draw(class_332 ctx, int x, int y, int w, int h, float radius, int color) {
        return drawInternal(ctx, x, y, w, h, radius, radius, radius, radius, color, 0.0f, 0.0f);
    }

    public static boolean drawVarying(class_332 ctx, int x, int y, int w, int h, float tl, float tr, float br, float bl, int color) {
        return drawInternal(ctx, x, y, w, h, tl, tr, br, bl, color, 0.0f, 0.0f);
    }

    public static boolean drawStroke(class_332 ctx, int x, int y, int w, int h, int radius, int color, int strokeWidth) {
        return drawInternal(ctx, x, y, w, h, ((float) radius), ((float) radius), ((float) radius), ((float) radius), color, Math.max(1.0f, ((float) strokeWidth)), 1.0f);
    }

    private static boolean drawInternal(class_332 ctx, int x, int y, int w, int h, float tlRadius, float trRadius, float brRadius, float blRadius, int color, float strokeWidth, float mode) {
        if (!(!disabled)) return false;
        if (w > 0) {
            if (h <= 0) {
            }
            return true;
            try {
            }
            class_310 mc = class_310.method_1551();
            if (mc == null) break label_5;
            if (mc.method_22683() != null) break label_7;
            label_5:
            return false;
            try {
                label_7:
                int sw = ctx.method_51421();
                int sh = ctx.method_51443();
                if (sw > 0) {
                    if (sh <= 0) {
                    }
                    return false;
                    try {
                    }
                    Matrix3x2fStack m = ctx.method_51448();
                    Vector2f p0 = m.transformPosition(((float) x), ((float) y), new Vector2f());
                    Vector2f p1 = m.transformPosition(((float) (x + w)), ((float) (y + h)), new Vector2f());
                    Vector2f dx = m.transformDirection(1.0f, 0.0f, new Vector2f());
                    Vector2f dy = m.transformDirection(0.0f, 1.0f, new Vector2f());
                    float scaleX = Math.max(1.0E-4f, dx.length());
                    float scaleY = Math.max(1.0E-4f, dy.length());
                    float sx0 = Math.min(p0.x, p1.x);
                    float sx1 = Math.max(p0.x, p1.x);
                    float sy0 = Math.min(p0.y, p1.y);
                    float sy1 = Math.max(p0.y, p1.y);
                    float ww = Math.max(1.0f, (sx1 - sx0));
                    float hh = Math.max(1.0f, (sy1 - sy0));
                    float pixelScale = Math.min(scaleX, scaleY);
                    float tl = (tlRadius * pixelScale);
                    float tr = (trRadius * pixelScale);
                    float br = (brRadius * pixelScale);
                    float bl = (blRadius * pixelScale);
                    float[] radii = normalizeCornerRadii(ww, hh, tl, tr, br, bl);
                    tl = radii[0];
                    tr = radii[1];
                    br = radii[2];
                    bl = radii[3];
                    float softness = Math.max(0.75f, (Math.min(scaleX, scaleY) * 1.1f));
                    float strokePx = Math.max(0.0f, (strokeWidth * Math.min(scaleX, scaleY)));
                    float x0 = toNdcX(sx0, sw);
                    float y0 = toNdcY(sy0, sh);
                    float x1 = toNdcX(sx1, sw);
                    float y1 = toNdcY(sy1, sh);
                    QUAD.beginNoCamera();
                    int i1 = QUAD.vec3(((double) x0), ((double) y0), 0.0).uv(0.0f, 0.0f).color(color).next();
                    int i2 = QUAD.vec3(((double) x1), ((double) y0), 0.0).uv(ww, 0.0f).color(color).next();
                    int i3 = QUAD.vec3(((double) x1), ((double) y1), 0.0).uv(ww, hh).color(color).next();
                    int i4 = QUAD.vec3(((double) x0), ((double) y1), 0.0).uv(0.0f, hh).color(color).next();
                    QUAD.quad(i1, i2, i3, i4);
                    QUAD.end();
                    if (!(QUAD.hasData())) {
                        return true;
                        try {
                        }
                        GpuBuffer vertexBuffer = QUAD.getVertexBuffer();
                        GpuBuffer indexBuffer = QUAD.getIndexBuffer();
                        GpuBufferSlice roundSlice = writeRoundData(ww, hh, softness, strokePx, mode, tl, tr, br, bl);
                        class_276 fb = mc.method_1522();
                        RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie HUD Rounded", fb.method_71639(), OptionalInt.empty());
                        pass.setPipeline(ViePipelines.HUD_ROUNDED);
                        pass.setUniform("RoundData", roundSlice);
                        pass.setVertexBuffer(0, vertexBuffer);
                        pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
                        pass.drawIndexed(0, 0, QUAD.getIndicesCount(), 1);
                        pass.close();
                        return true;
                    } catch (Throwable ex66) {
                        Throwable t = ex66;
                        disabled = true;
                        if (!warned) {
                            warned = true;
                            VieCheatsMod.LOGGER.warn("[VieCheat] SDF rounded renderer disabled, falling back to software corners", t);
                        }
                        return false;
                    }
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

    private static GpuBufferSlice writeRoundData(float w, float h, float softness, float stroke, float mode, float tl, float tr, float br, float bl) {
        ROUND_DATA.width = w;
        ROUND_DATA.height = h;
        ROUND_DATA.softness = softness;
        ROUND_DATA.stroke = stroke;
        ROUND_DATA.mode = mode;
        ROUND_DATA.tl = tl;
        ROUND_DATA.tr = tr;
        ROUND_DATA.br = br;
        ROUND_DATA.bl = bl;
        return getRoundStorage().method_71102(ROUND_DATA);
    }

    private static class_11280 getRoundStorage() {
        if (roundStorage == null) {
            roundStorage = new class_11280("Vie - HUD RoundData", ROUND_UBO_SIZE, 128);
        }
        return roundStorage;
    }

    private static float[] normalizeCornerRadii(float w, float h, float tl, float tr, float br, float bl) {
        float half = (Math.min(w, h) * 0.5f);
        tl = clamp(tl, 0.0f, half);
        tr = clamp(tr, 0.0f, half);
        br = clamp(br, 0.0f, half);
        bl = clamp(bl, 0.0f, half);
        float scale = 1.0f;
        float top = (tl + tr);
        if (top > w && top > 0.0f) {
            scale = Math.min(scale, (w / top));
        }
        float bottom = (bl + br);
        if (bottom > w && bottom > 0.0f) {
            scale = Math.min(scale, (w / bottom));
        }
        float left = (tl + bl);
        if (left > h && left > 0.0f) {
            scale = Math.min(scale, (h / left));
        }
        float right = (tr + br);
        if (right > h && right > 0.0f) {
            scale = Math.min(scale, (h / right));
        }
        if (scale < 1.0f) {
            tl = (tl * scale);
            tr = (tr * scale);
            br = (br * scale);
            bl = (bl * scale);
        }
        tl = Math.min(tl, Math.max(0.0f, (w - tr)));
        tr = Math.min(tr, Math.max(0.0f, (w - tl)));
        bl = Math.min(bl, Math.max(0.0f, (w - br)));
        br = Math.min(br, Math.max(0.0f, (w - bl)));
        tl = Math.min(tl, Math.max(0.0f, (h - bl)));
        bl = Math.min(bl, Math.max(0.0f, (h - tl)));
        tr = Math.min(tr, Math.max(0.0f, (h - br)));
        br = Math.min(br, Math.max(0.0f, (h - tr)));
        var tmp0 = new float[4];
        tmp0[0] = tl;
        tmp0[1] = tr;
        tmp0[2] = br;
        tmp0[3] = bl;
        return tmp0;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    static {
        ROUND_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().get();
        ROUND_DATA = new SdfRoundedRectRenderer.RoundData();
        QUAD = new VieMeshBuilder(ViePipelines.HUD_ROUNDED);
        disabled = false;
        warned = false;
    }


    final class RoundData implements class_11280.class_11281 {
        float width;
        float height;
        float softness;
        float stroke;
        float mode;
        float tl;
        float tr;
        float br;
        float bl;

        public void method_71104(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putVec4(this.width, this.height, this.softness, this.mode).putVec4(this.stroke, this.tl, this.tr, this.br).putVec4(this.bl, 0.0f, 0.0f, 0.0f);
        }

        public boolean equals(Object o) {
            return false;
        }

    }
}

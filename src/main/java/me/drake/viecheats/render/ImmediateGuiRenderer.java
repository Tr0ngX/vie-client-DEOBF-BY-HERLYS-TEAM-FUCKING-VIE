package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_1043;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class ImmediateGuiRenderer {
    private static final VieMeshBuilder QUAD;
    private static final VieMeshBuilder QUAD_TEX;
    private static boolean disabled;
    private static boolean warned;
    private static boolean warnedTex;

    public static void reset() {
        disabled = false;
        warned = false;
        warnedTex = false;
    }

    public static void fillRect(class_332 ctx, int x1, int y1, int x2, int y2, int argb) {
        if ((ctx == null) || (!(!disabled))) {
            return;
        }
        if (((argb >>> 24) & 255) == 0) return;
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        if ((maxX <= minX) || (maxY <= minY)) {
            return;
        }
        drawQuad(ctx, ((float) minX), ((float) minY), ((float) maxX), ((float) maxY), argb, argb, argb, argb);
    }

    public static void fillGradientRect(class_332 ctx, int x, int y, int w, int h, int startColor, int endColor, boolean vertical) {
        if ((ctx == null) || (!(!disabled))) {
            return;
        }
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int x2 = (x + w);
        int y2 = (y + h);
        if (!(!vertical)) {
            drawQuad(ctx, ((float) x), ((float) y), ((float) x2), ((float) y2), startColor, startColor, endColor, endColor);
        } else {
            drawQuad(ctx, ((float) x), ((float) y), ((float) x2), ((float) y2), startColor, endColor, endColor, startColor);
        }
    }

    public static void drawLine(class_332 ctx, float x1, float y1, float x2, float y2, float stroke, int argb) {
        if (ctx == null) break label_1;
        if (!(!disabled)) {
            label_1:
            return;
        }
        if (((argb >>> 24) & 255) == 0) return;
        float dx = (x2 - x1);
        float dy = (y2 - y1);
        float len = ((float) Math.sqrt(((double) ((dx * dx) + (dy * dy)))));
        float half = Math.max(0.5f, (stroke * 0.5f));
        if (len < 1.0E-4f) {
            fillRect(ctx, Math.round((x1 - half)), Math.round((y1 - half)), Math.round((x1 + half)), Math.round((y1 + half)), argb);
            return;
        }
        float nx = ((-(dy) / len) * half);
        float ny = ((dx / len) * half);
        float ax = (x1 + nx);
        float ay = (y1 + ny);
        float bx = (x2 + nx);
        float by = (y2 + ny);
        float cx = (x2 - nx);
        float cy = (y2 - ny);
        float dx2 = (x1 - nx);
        float dy2 = (y1 - ny);
        drawQuad(ctx, ax, ay, bx, by, cx, cy, dx2, dy2, argb, argb, argb, argb);
    }

    public static void strokeRect(class_332 ctx, int x, int y, int w, int h, int stroke, int argb) {
        if ((((ctx == null) || (disabled)) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int s = Math.max(1, stroke);
        fillRect(ctx, x, y, (x + w), (y + s), argb);
        fillRect(ctx, x, ((y + h) - s), (x + w), (y + h), argb);
        fillRect(ctx, x, (y + s), (x + s), ((y + h) - s), argb);
        fillRect(ctx, ((x + w) - s), (y + s), (x + w), ((y + h) - s), argb);
    }

    private static void drawQuad(class_332 ctx, float x1, float y1, float x2, float y2, int cTopLeft, int cTopRight, int cBottomRight, int cBottomLeft) {
        drawQuad(ctx, x1, y1, x2, y1, x2, y2, x1, y2, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    private static void drawQuad(class_332 ctx, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int c1, int c2, int c3, int c4) {
        try {
            int sw = ctx.method_51421();
            int sh = ctx.method_51443();
            if (sw <= 0) break label_3;
            if (sh <= 0) {
                label_3:
                return;
                try {
                }
                Matrix3x2fStack matrices = ctx.method_51448();
                Vector2f p1 = matrices.transformPosition(x1, y1, new Vector2f());
                Vector2f p2 = matrices.transformPosition(x2, y2, new Vector2f());
                Vector2f p3 = matrices.transformPosition(x3, y3, new Vector2f());
                Vector2f p4 = matrices.transformPosition(x4, y4, new Vector2f());
                QUAD.beginNoCamera();
                int i1 = QUAD.vec3(((double) toNdcX(p1.x, sw)), ((double) toNdcY(p1.y, sh)), 0.0).color(c1).next();
                int i2 = QUAD.vec3(((double) toNdcX(p2.x, sw)), ((double) toNdcY(p2.y, sh)), 0.0).color(c2).next();
                int i3 = QUAD.vec3(((double) toNdcX(p3.x, sw)), ((double) toNdcY(p3.y, sh)), 0.0).color(c3).next();
                int i4 = QUAD.vec3(((double) toNdcX(p4.x, sw)), ((double) toNdcY(p4.y, sh)), 0.0).color(c4).next();
                QUAD.quad(i1, i2, i3, i4);
                QUAD.end();
                if (!(QUAD.hasData())) {
                    return;
                    try {
                    }
                    GpuBuffer vertexBuffer = QUAD.getVertexBuffer();
                    GpuBuffer indexBuffer = QUAD.getIndexBuffer();
                    class_310 mc = class_310.method_1551();
                    if (mc == null) {
                        return;
                        try {
                        }
                        class_276 fb = mc.method_1522();
                        RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie HUD Color", fb.method_71639(), OptionalInt.empty());
                        pass.setPipeline(ViePipelines.HUD_COLOR);
                        pass.setVertexBuffer(0, vertexBuffer);
                        pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
                        pass.drawIndexed(0, 0, QUAD.getIndicesCount(), 1);
                        pass.close();
                    } catch (Throwable ex34) {
                        Throwable t = ex34;
                        disabled = true;
                        if (warned) break label_39;
                        warned = true;
                        VieCheatsMod.LOGGER.warn("[VieCheat] Immediate GUI color renderer disabled.", t);
                    }
                    label_39:
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

    public static void drawTriangle(class_332 ctx, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        if (ctx == null) break label_1;
        if (!disabled) break label_2;
        label_1:
        return;
        label_2:
        if (((color >>> 24) & 255) == 0) return;
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        if (sw > 0) {
            if (sh <= 0) {
            }
            return;
            try {
            }
            Matrix3x2fStack matrices = ctx.method_51448();
            Vector2f p1 = matrices.transformPosition(x1, y1, new Vector2f());
            Vector2f p2 = matrices.transformPosition(x2, y2, new Vector2f());
            Vector2f p3 = matrices.transformPosition(x3, y3, new Vector2f());
            QUAD.beginNoCamera();
            int i1 = QUAD.vec3(((double) toNdcX(p1.x, sw)), ((double) toNdcY(p1.y, sh)), 0.0).color(color).next();
            int i2 = QUAD.vec3(((double) toNdcX(p2.x, sw)), ((double) toNdcY(p2.y, sh)), 0.0).color(color).next();
            int i3 = QUAD.vec3(((double) toNdcX(p3.x, sw)), ((double) toNdcY(p3.y, sh)), 0.0).color(color).next();
            QUAD.quad(i1, i2, i3, i3);
            QUAD.end();
            if (!(QUAD.hasData())) {
                return;
                try {
                }
                class_310 mc = class_310.method_1551();
                if (mc == null) {
                    return;
                    try {
                    }
                    GpuBuffer vertexBuffer = QUAD.getVertexBuffer();
                    GpuBuffer indexBuffer = QUAD.getIndexBuffer();
                    RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie Triangle", mc.method_1522().method_71639(), OptionalInt.empty());
                    pass.setPipeline(ViePipelines.HUD_COLOR);
                    pass.setVertexBuffer(0, vertexBuffer);
                    pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
                    pass.drawIndexed(0, 0, QUAD.getIndicesCount(), 1);
                    pass.close();
                } catch (Throwable ex32) {
                    Throwable t = ex32;
                    disabled = true;
                    if (warned) break label_37;
                    warned = true;
                    VieCheatsMod.LOGGER.warn("[VieCheat] Immediate GUI renderer disabled.", t);
                }
                label_37:
            }
        }
    }

    public static void drawTexturedRect(class_332 ctx, class_1043 tex, int x, int y, int w, int h, int argb) {
        if (((((ctx == null) || (disabled)) || (tex == null)) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int a = ((argb >>> 24) & 255);
        if (a == 0) return;
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        if (sw > 0) {
            if (sh <= 0) {
            }
            return;
            try {
            }
            Matrix3x2fStack matrices = ctx.method_51448();
            Vector2f p0 = matrices.transformPosition(((float) x), ((float) y), new Vector2f());
            Vector2f p1 = matrices.transformPosition(((float) (x + w)), ((float) y), new Vector2f());
            Vector2f p2 = matrices.transformPosition(((float) (x + w)), ((float) (y + h)), new Vector2f());
            Vector2f p3 = matrices.transformPosition(((float) x), ((float) (y + h)), new Vector2f());
            int r = ((argb >> 16) & 255);
            int g = ((argb >> 8) & 255);
            int b = (argb & 255);
            QUAD_TEX.beginNoCamera();
            QUAD_TEX.vec3(((double) toNdcX(p0.x, sw)), ((double) toNdcY(p0.y, sh)), 0.0).uv(0.0f, 0.0f).color(r, g, b, a).next();
            QUAD_TEX.vec3(((double) toNdcX(p1.x, sw)), ((double) toNdcY(p1.y, sh)), 0.0).uv(1.0f, 0.0f).color(r, g, b, a).next();
            QUAD_TEX.vec3(((double) toNdcX(p2.x, sw)), ((double) toNdcY(p2.y, sh)), 0.0).uv(1.0f, 1.0f).color(r, g, b, a).next();
            QUAD_TEX.vec3(((double) toNdcX(p3.x, sw)), ((double) toNdcY(p3.y, sh)), 0.0).uv(0.0f, 1.0f).color(r, g, b, a).next();
            QUAD_TEX.quad(0, 1, 2, 3);
            QUAD_TEX.end();
            if (!(QUAD_TEX.hasData())) {
                return;
                try {
                }
                class_310 mc = class_310.method_1551();
                if (mc == null) {
                    return;
                    try {
                    }
                    GpuBuffer vertexBuffer = QUAD_TEX.getVertexBuffer();
                    GpuBuffer indexBuffer = QUAD_TEX.getIndexBuffer();
                    RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie Tex", mc.method_1522().method_71639(), OptionalInt.empty());
                    pass.setPipeline(ViePipelines.HUD_TEX);
                    pass.bindTexture("Sampler0", tex.method_71659(), tex.method_75484());
                    pass.setVertexBuffer(0, vertexBuffer);
                    pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
                    pass.drawIndexed(0, 0, QUAD_TEX.getIndicesCount(), 1);
                    pass.close();
                } catch (Throwable ex39) {
                    Throwable t = ex39;
                    disabled = true;
                    if (warnedTex) break label_44;
                    warnedTex = true;
                    VieCheatsMod.LOGGER.warn("[VieCheat] Immediate GUI tex renderer disabled.", t);
                }
                label_44:
            }
        }
    }

    static {
        QUAD = new VieMeshBuilder(ViePipelines.HUD_COLOR);
        QUAD_TEX = new VieMeshBuilder(ViePipelines.HUD_TEX);
    }

}

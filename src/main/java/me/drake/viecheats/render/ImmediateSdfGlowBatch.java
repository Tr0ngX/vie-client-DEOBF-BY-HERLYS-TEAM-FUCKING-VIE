package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import java.nio.ByteBuffer;
import net.minecraft.class_11280;

public final class ImmediateSdfGlowBatch {
    public static final int ROUND_GLOW_UBO_SIZE;
    private static final ImmediateSdfGlowBatch.RoundGlowUniformData TMP;

    public static ImmediateSdfGlowBatch.EmittedQuad emitRoundedGlowQuad(VieMeshBuilder mesh, float xPx, float yPx, float widthPx, float heightPx, float glowRadiusPx, float strokeThicknessPx, int argb, int screenW, int screenH) {
        float safeW = Math.max(1.0f, widthPx);
        float safeH = Math.max(1.0f, heightPx);
        float pad = ((Math.max(0.0f, glowRadiusPx) + Math.max(0.0f, strokeThicknessPx)) + 2.0f);
        float ex = (xPx - pad);
        float ey = (yPx - pad);
        float ew = ((safeW + pad) + pad);
        float eh = ((safeH + pad) + pad);
        float x0 = toNdcX(ex, screenW);
        float y0 = toNdcY(ey, screenH);
        float x1 = toNdcX((ex + ew), screenW);
        float y1 = toNdcY((ey + eh), screenH);
        mesh.ensureCapacity(4, 6);
        int i1 = mesh.vec3(((double) x0), ((double) y0), 0.0).uv(0.0f, 0.0f).color(argb).next();
        int i2 = mesh.vec3(((double) x1), ((double) y0), 0.0).uv(ew, 0.0f).color(argb).next();
        int i3 = mesh.vec3(((double) x1), ((double) y1), 0.0).uv(ew, eh).color(argb).next();
        int i4 = mesh.vec3(((double) x0), ((double) y1), 0.0).uv(0.0f, eh).color(argb).next();
        mesh.quad(i1, i2, i3, i4);
        return new ImmediateSdfGlowBatch.EmittedQuad(safeW, safeH, ew, eh, pad);
    }

    public static ImmediateSdfGlowBatch.EmittedQuad emitRoundedGlowQuad(VieMeshBuilder mesh, float xPx, float yPx, float widthPx, float heightPx, float glowRadiusPx, int argb, int screenW, int screenH) {
        return emitRoundedGlowQuad(mesh, xPx, yPx, widthPx, heightPx, glowRadiusPx, 0.0f, argb, screenW, screenH);
    }

    public static GpuBufferSlice writeRoundGlowData(class_11280 storage, ImmediateSdfGlowBatch.EmittedQuad quad, float radiusTl, float radiusTr, float radiusBr, float radiusBl, float strokeThicknessPx, float strokeAlign, float glowRadiusPx, float glowExponent, int fillArgb, int strokeArgb, int glowArgb, float minAaPx) {
        float[] r = normalizeCornerRadii(quad.rectWidth(), quad.rectHeight(), radiusTl, radiusTr, radiusBr, radiusBl);
        TMP.rectWidth = quad.rectWidth();
        TMP.rectHeight = quad.rectHeight();
        TMP.padPx = quad.padPx();
        TMP.minAaPx = Math.max(0.001f, minAaPx);
        TMP.radiusTl = r[0];
        TMP.radiusTr = r[1];
        TMP.radiusBr = r[2];
        TMP.radiusBl = r[3];
        TMP.strokeThicknessPx = Math.max(0.0f, strokeThicknessPx);
        TMP.strokeAlign = clamp(strokeAlign, -1.0f, 1.0f);
        TMP.glowRadiusPx = Math.max(0.0f, glowRadiusPx);
        TMP.glowExponent = Math.max(0.001f, glowExponent);
        unpackArgb(fillArgb, TMP.fillColor);
        unpackArgb(strokeArgb, TMP.strokeColor);
        unpackArgb(glowArgb, TMP.glowColor);
        return storage.method_71102(TMP);
    }

    private static float toNdcX(float x, int screenW) {
        float sw = Math.max(1.0f, ((float) screenW));
        return (((x / sw) * 2.0f) - 1.0f);
    }

    private static float toNdcY(float y, int screenH) {
        float sh = Math.max(1.0f, ((float) screenH));
        return (1.0f - ((y / sh) * 2.0f));
    }

    private static void unpackArgb(int argb, float[] out) {
        out[0] = (((float) ((argb >>> 16) & 255)) / 255.0f);
        out[1] = (((float) ((argb >>> 8) & 255)) / 255.0f);
        out[2] = (((float) (argb & 255)) / 255.0f);
        out[3] = (((float) ((argb >>> 24) & 255)) / 255.0f);
    }

    private static float[] normalizeCornerRadii(float w, float h, float tl, float tr, float br, float bl) {
        float half = Math.max(0.0f, (Math.min(w, h) * 0.5f));
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
        var tmp0 = new float[4];
        tmp0[0] = tl;
        tmp0[1] = tr;
        tmp0[2] = br;
        tmp0[3] = bl;
        return tmp0;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    static {
        ROUND_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
        TMP = new ImmediateSdfGlowBatch.RoundGlowUniformData();
    }


    public final class RoundGlowUniformData implements class_11280.class_11281 {
        float rectWidth;
        float rectHeight;
        float padPx;
        float minAaPx;
        float radiusTl;
        float radiusTr;
        float radiusBr;
        float radiusBl;
        float strokeThicknessPx;
        float strokeAlign;
        float glowRadiusPx;
        float glowExponent;
        final float[] fillColor;
        final float[] strokeColor;
        final float[] glowColor;

        public RoundGlowUniformData() {
            this.fillColor = new float[4];
            this.strokeColor = new float[4];
            this.glowColor = new float[4];
        }

        public void method_71104(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putVec4(this.rectWidth, this.rectHeight, this.padPx, this.minAaPx).putVec4(this.radiusTl, this.radiusTr, this.radiusBr, this.radiusBl).putVec4(this.strokeThicknessPx, this.strokeAlign, 0.0f, 0.0f).putVec4(this.glowRadiusPx, this.glowExponent, 0.0f, 0.0f).putVec4(this.fillColor[0], this.fillColor[1], this.fillColor[2], this.fillColor[3]).putVec4(this.strokeColor[0], this.strokeColor[1], this.strokeColor[2], this.strokeColor[3]).putVec4(this.glowColor[0], this.glowColor[1], this.glowColor[2], this.glowColor[3]);
        }

        public boolean equals(Object obj) {
            return false;
        }

    }

    public record EmittedQuad(float rectWidth, float rectHeight, float expandedWidth, float expandedHeight, float padPx) {

        public EmittedQuad(float rectWidth, float rectHeight, float expandedWidth, float expandedHeight, float padPx) {
            this.rectWidth = rectWidth;
            this.rectHeight = rectHeight;
            this.expandedWidth = expandedWidth;
            this.expandedHeight = expandedHeight;
            this.padPx = padPx;
        }

        public float rectWidth() {
            return this.rectWidth;
        }

        public float rectHeight() {
            return this.rectHeight;
        }

        public float expandedWidth() {
            return this.expandedWidth;
        }

        public float expandedHeight() {
            return this.expandedHeight;
        }

        public float padPx() {
            return this.padPx;
        }

    }
}

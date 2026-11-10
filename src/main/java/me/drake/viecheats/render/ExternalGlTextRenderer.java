package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

public final class ExternalGlTextRenderer {
    private static final class_2960 FONT_FILE;
    private static final class_2960 ATLAS_ID;
    private static final int ATLAS_W = 2048;
    private static final int ATLAS_H = 2048;
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 224;
    private static final int SDF_PADDING = 8;
    private static final int SDF_ON_EDGE_VALUE = 192;
    private static final float SDF_PIXEL_DIST_SCALE = 24.0f;
    private static final float FONT_PIXEL_HEIGHT = 32.0f;
    private static final float DEFAULT_TEXT_SIZE = 9.0f;
    private static final float CHAR_TRACKING = -0.02f;
    private static boolean triedInit;
    private static boolean available;
    private static long lastRecoverAttemptMs;
    private static final long RECOVER_COOLDOWN_MS = 750L;
    private static class_2960 atlasId;
    private static final ExternalGlTextRenderer.Glyph[] glyphs;
    private static final VieMeshBuilder TEXT_MESH;
    private static int ascentPx;
    private static int lineHeightPx;

    public static boolean isAvailable() {
        ensureInit();
        return available;
    }

    public static void drawText(class_332 ctx, String text, int x, int y, int argb, boolean shadow) {
        drawText(ctx, text, x, y, argb, shadow, 9.0f);
    }

    public static void drawText(class_332 ctx, String text, int x, int y, int argb, boolean shadow, float sizePx) {
        if ((ctx == null) || (text == null)) break label_1;
        if (text.isEmpty()) {
            label_1:
            return;
        }
        int alpha = ((argb >>> 24) & 255);
        if (alpha <= 0) return;
        if (!(ensureReady())) return;
        float renderSize = sanitizeSize(sizePx);
        float shadowOffset = Math.max(0.35f, (renderSize / 24.0f));
        if (!(!shadow)) {
            int sA = Math.max(1, ((int) (((float) alpha) * 0.12f)));
            int sRgb = ((((((argb >> 16) & 255) / 5) << 16) | ((((argb >> 8) & 255) / 5) << 8)) | ((argb & 255) / 5));
            int sCol = ((sA << 24) | sRgb);
            drawInternal(ctx, text, (((float) x) + shadowOffset), (((float) y) + shadowOffset), sCol, renderSize);
        }
        drawInternal(ctx, text, ((float) x), ((float) y), argb, renderSize);
    }

    public static int measureTextWidth(String text) {
        return measureTextWidth(text, 9.0f);
    }

    public static int measureTextWidth(String text, float sizePx) {
        if (text == null) break label_1;
        if (text.isEmpty()) {
            label_1:
            return 0;
        }
        if (!(ensureReady())) return 0;
        float scale = (sanitizeSize(sizePx) / 32.0f);
        float lineWidth = 0.0f;
        float maxLine = 0.0f;
        int i = 0;
        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == 10) {
                maxLine = Math.max(maxLine, lineWidth);
                lineWidth = 0.0f;
                break label_16;
            }
            ExternalGlTextRenderer.Glyph glyph = glyphForChar(ch);
            if (glyph == null) {
                break label_16;
            }
            lineWidth = (lineWidth + ((glyph.advance + -0.02f) * scale));
            label_16:
            i++;
        }
        maxLine = Math.max(maxLine, lineWidth);
        return Math.max(0, ((int) Math.ceil(((double) maxLine))));
    }

    public static int lineHeight() {
        return lineHeight(9.0f);
    }

    public static int lineHeight(float sizePx) {
        ensureInit();
        float scale = (sanitizeSize(sizePx) / 32.0f);
        return Math.max(1, ((int) Math.ceil(((double) (((float) lineHeightPx) * scale)))));
    }

    private static void drawInternal(class_332 ctx, String text, float startX, float startY, int argb, float sizePx) {
        if (((ctx == null) || (text == null)) || (text.isEmpty())) {
            return;
        }
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        if ((sw <= 0) || (sh <= 0)) {
            return;
        }
        float scale = (sanitizeSize(sizePx) / 32.0f);
        Matrix3x2fStack m = ctx.method_51448();
        float m00 = m.m00();
        float m01 = m.m01();
        float m10 = m.m10();
        float m11 = m.m11();
        float m20 = m.m20();
        float m21 = m.m21();
        float baseline = (((float) ascentPx) * scale);
        float originX = snapToPixel(startX);
        float originY = snapToPixel(startY);
        float penX = 0.0f;
        float penY = 0.0f;
        TEXT_MESH.beginNoCamera();
        int i = 0;
        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == 10) {
                penX = 0.0f;
                penY = (penY + (((float) lineHeightPx) * scale));
                break label_43;
            }
            ExternalGlTextRenderer.Glyph glyph = glyphForChar(ch);
            if (glyph == null) {
                break label_43;
            }
            float advance = ((glyph.advance + -0.02f) * scale);
            if (!(glyph.hasBitmap())) {
                penX = (penX + advance);
                break label_43;
            }
            float gx0 = ((originX + penX) + (((float) glyph.xOff) * scale));
            float gy0 = (((originY + penY) + baseline) + (((float) glyph.yOff) * scale));
            float gx1 = (gx0 + (((float) glyph.w) * scale));
            float gy1 = (gy0 + (((float) glyph.h) * scale));
            int i1 = pushVertex(gx0, gy0, glyph.u0, glyph.v0, argb, m00, m01, m10, m11, m20, m21, sw, sh);
            int i2 = pushVertex(gx1, gy0, glyph.u1, glyph.v0, argb, m00, m01, m10, m11, m20, m21, sw, sh);
            int i3 = pushVertex(gx1, gy1, glyph.u1, glyph.v1, argb, m00, m01, m10, m11, m20, m21, sw, sh);
            int i4 = pushVertex(gx0, gy1, glyph.u0, glyph.v1, argb, m00, m01, m10, m11, m20, m21, sw, sh);
            TEXT_MESH.quad(i1, i2, i3, i4);
            penX = (penX + advance);
            label_43:
            i++;
        }
        TEXT_MESH.end();
        if (!(TEXT_MESH.hasData())) return;
        renderImmediateTextPass();
    }

    private static int pushVertex(float x, float y, float u, float v, int argb, float m00, float m01, float m10, float m11, float m20, float m21, int sw, int sh) {
        float sx = (((m00 * x) + (m10 * y)) + m20);
        float sy = (((m01 * x) + (m11 * y)) + m21);
        float ndcX = toNdcX(sx, sw);
        float ndcY = toNdcY(sy, sh);
        return TEXT_MESH.vec3(((double) ndcX), ((double) ndcY), 0.0).uv(u, v).color(argb).next();
    }

    private static float toNdcX(float x, int screenW) {
        return (((x / ((float) screenW)) * 2.0f) - 1.0f);
    }

    private static float toNdcY(float y, int screenH) {
        return (1.0f - ((y / ((float) screenH)) * 2.0f));
    }

    private static void renderImmediateTextPass() {
        class_310 mc = class_310.method_1551();
        if (((mc == null) || (mc.method_1531() == null)) || (atlasId == null)) {
            return;
        }
        class_1044 texture = mc.method_1531().method_4619(atlasId);
        if ((texture == null) || (texture.method_71659() == null)) {
            return;
        }
        class_12137 sampler = texture.method_75484();
        if (RenderSystem.getSamplerCache() == null) break label_12;
        class_12137 linear = RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, false);
        if (linear == null) {
            linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        }
        if (linear == null) break label_12;
        sampler = linear;
        label_12:
        if (sampler == null) return;
        class_276 fb = mc.method_1522();
        if ((fb == null) || (fb.method_71639() == null)) {
            return;
        }
        GpuBuffer vertexBuffer = TEXT_MESH.getVertexBuffer();
        GpuBuffer indexBuffer = TEXT_MESH.getIndexBuffer();
        RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie HUD Text SDF", fb.method_71639(), OptionalInt.empty());
        pass.setPipeline(ViePipelines.HUD_TEXT_SDF);
        pass.bindTexture("Sampler0", texture.method_71659(), sampler);
        pass.setVertexBuffer(0, vertexBuffer);
        pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
        pass.drawIndexed(0, 0, TEXT_MESH.getIndicesCount(), 1);
        pass.close();
    }

    private static int mapChar(char ch) {
        int code = ch;
        if (code >= 32) {
            if (code >= 256) {
            }
            code = 63;
        }
        return (code - 32);
    }

    private static ExternalGlTextRenderer.Glyph glyphForChar(char ch) {
        int index = mapChar(ch);
        ExternalGlTextRenderer.Glyph glyph = glyphs[index];
        if (glyph != null) return glyph;
        return glyphs[mapChar(63)];
    }

    private static float sanitizeSize(float sizePx) {
        if ((Float.isNaN(sizePx)) || (Float.isInfinite(sizePx))) {
            return 9.0f;
        }
        float clamped = Math.max(6.0f, Math.min(24.0f, sizePx));
        return (((float) Math.round((clamped * 2.0f))) * 0.5f);
    }

    private static float snapToPixel(float value) {
        return ((float) Math.floor(((double) (value + 0.5f))));
    }

    private static void ensureInit() {
        if (!(!triedInit)) return;
        triedInit = true;
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
            try {
            }
            ByteBuffer ttfBuffer = readFontBuffer(mc);
            if (ttfBuffer != null) {
                if (!(ttfBuffer.hasRemaining())) {
                }
                VieCheatsMod.LOGGER.warn("[VieCheat] External font resource is empty: {}", FONT_FILE);
                return;
                try {
                }
                STBTTFontinfo info = STBTTFontinfo.create();
                if (!(STBTruetype.stbtt_InitFont(info, ttfBuffer))) {
                    VieCheatsMod.LOGGER.warn("[VieCheat] Failed parsing external font: {}", FONT_FILE);
                    return;
                    try {
                    }
                    float fontScale = computeMetrics(info);
                    class_1011 image = buildSdfAtlas(info, fontScale);
                    atlasId = uploadAtlas(mc, image);
                    available = atlasId != null;
                } catch (Throwable ex19) {
                    Throwable t = ex19;
                    VieCheatsMod.LOGGER.warn("[VieCheat] Failed to initialize external GL font renderer", t);
                    available = false;
                }
            }
        }
    }

    private static boolean ensureReady() {
        ensureInit();
        if (!available) return false;
        return ensureAtlasTextureReady();
    }

    private static boolean ensureAtlasTextureReady() {
        class_310 mc = class_310.method_1551();
        if (((mc == null) || (mc.method_1531() == null)) || (atlasId == null)) {
            return false;
        }
        class_1044 texture = mc.method_1531().method_4619(atlasId);
        if (texture != null && texture.method_71659() != null) return true;
        long now = System.currentTimeMillis();
        if ((now - lastRecoverAttemptMs) < 750L) return false;
        lastRecoverAttemptMs = now;
        triedInit = false;
        available = false;
        atlasId = null;
        Arrays.fill(glyphs, null);
        ensureInit();
        if ((!available) || (atlasId == null)) {
            return false;
        }
        class_1044 rebuilt = mc.method_1531().method_4619(atlasId);
        if (rebuilt == null) break label_17;
        return rebuilt.method_71659() != null;
    }

    private static ByteBuffer readFontBuffer(class_310 mc) throws IOException {
        InputStream in = openFontStream(mc);
        try {
            if (in == null) {
                byte[] bytes = null;
                if (in != null) {
                    in.close();
                }
                return bytes;
                try {
                }
                bytes = in.readAllBytes();
                ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                Object var4 = buffer;
                if (in != null) {
                    in.close();
                }
                return var4;
            } catch (Throwable ex11) {
                bytes = ex11;
                if (in == null) break label_15;
                try {
                    in.close();
                } catch (Throwable ex14) {
                    buffer = ex14;
                    bytes.addSuppressed(buffer);
                }
                label_15:
                throw bytes;
            }
        }
    }

    private static InputStream openFontStream(class_310 mc) throws IOException {
        Optional opt = mc.method_1478().method_14486(FONT_FILE);
        if (opt.isEmpty()) return null;
        class_3298 resource = ((class_3298) opt.get());
        return resource.method_14482();
    }

    private static float computeMetrics(STBTTFontinfo info) {
        float scale = STBTruetype.stbtt_ScaleForPixelHeight(info, 32.0f);
        int[] asc = new int[1];
        int[] des = new int[1];
        int[] gap = new int[1];
        STBTruetype.stbtt_GetFontVMetrics(info, asc, des, gap);
        ascentPx = Math.max(1, Math.round((((float) asc[0]) * scale)));
        int descentPx = Math.round((((float) des[0]) * scale));
        int gapPx = Math.round((((float) gap[0]) * scale));
        lineHeightPx = Math.max(1, ((ascentPx - descentPx) + gapPx));
        return scale;
    }

    private static class_1011 buildSdfAtlas(STBTTFontinfo info, float fontScale) {
        class_1011 image = new class_1011(class_1011.class_1012.field_4997, 2048, 2048, false);
        int penX = 1;
        int penY = 1;
        int rowH = 0;
        int[] advance = new int[1];
        int[] lsb = new int[1];
        int[] bw = new int[1];
        int[] bh = new int[1];
        int[] xOff = new int[1];
        int[] yOff = new int[1];
        int i = 0;
        while (i < 224) {
            int codepoint = (32 + i);
            ExternalGlTextRenderer.Glyph glyph = new ExternalGlTextRenderer.Glyph();
            STBTruetype.stbtt_GetCodepointHMetrics(info, codepoint, advance, lsb);
            glyph.advance = (((float) advance[0]) * fontScale);
            ByteBuffer sdf = STBTruetype.stbtt_GetCodepointSDF(info, fontScale, codepoint, 8, -64, 24.0f, bw, bh, xOff, yOff);
            if (sdf != null) {
                int gw = bw[0];
                int gh = bh[0];
                if ((gw <= 0) || (gh <= 0)) break label_47;
                if (((penX + gw) + 1) > 2048) {
                    penX = 1;
                    penY = (penY + (rowH + 1));
                    rowH = 0;
                }
                if (((penY + gh) + 1) > 2048) break label_47;
                int dstX = penX;
                int dstY = penY;
                glyph.w = gw;
                glyph.h = gh;
                glyph.xOff = xOff[0];
                glyph.yOff = yOff[0];
                glyph.u0 = (((float) dstX) / 2048.0f);
                glyph.v0 = (((float) dstY) / 2048.0f);
                glyph.u1 = (((float) (dstX + gw)) / 2048.0f);
                glyph.v1 = (((float) (dstY + gh)) / 2048.0f);
                int src = 0;
                int y = 0;
                while (y < gh) {
                    int x = 0;
                    while (x < gw) {
                        src++;
                        int a = (sdf.get(src) & 255);
                        image.method_61941((dstX + x), (dstY + y), ((a << 24) | 16777215));
                        x++;
                    }
                    y++;
                }
                penX = (penX + (gw + 1));
                rowH = Math.max(rowH, gh);
                label_47:
                STBTruetype.stbtt_FreeSDF(sdf);
            }
            glyphs[i] = glyph;
            i++;
        }
        return image;
    }

    private static class_2960 uploadAtlas(class_310 mc, class_1011 image) {
        class_1043 texture = new class_1043(() -> "vie_external_gl_font", image);
        mc.method_1531().method_4616(ATLAS_ID, texture);
        return ATLAS_ID;
    }

    static {
        FONT_FILE = class_2960.method_60655("vie_cheats", "font/vie_external.ttf");
        ATLAS_ID = class_2960.method_60655("vie_cheats", "font/external_gl_atlas");
        triedInit = false;
        available = false;
        lastRecoverAttemptMs = 0L;
        glyphs = new ExternalGlTextRenderer.Glyph[224];
        TEXT_MESH = new VieMeshBuilder(ViePipelines.HUD_TEXT_SDF);
        ascentPx = 12;
        lineHeightPx = 16;
    }


    final class Glyph {
        float u0;
        float v0;
        float u1;
        float v1;
        int w;
        int h;
        int xOff;
        int yOff;
        float advance;

        boolean hasBitmap() {
            if (((this.w <= 0) || (this.h <= 0)) || (this.u1 <= this.u0)) break label_1;
            return this.v1 > this.v0;
        }

    }
}

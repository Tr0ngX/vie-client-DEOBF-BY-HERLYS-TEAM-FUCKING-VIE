package me.drake.viecheats.modules.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.render.ExternalGlTextRenderer;
import me.drake.viecheats.render.ImmediateGuiRenderer;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class SpotiPlayRenderer {
    private static final int ACCENT = -14829228;
    private static final int BG_COLOR = -586347251;
    private static final int BG_BORDER = -15066598;
    private static final int BG_ART = -15723234;
    private static final int TEXT_MAIN = -1;
    private static final int TEXT_DIM = -5000269;
    private static final boolean DEBUG = true;
    private static volatile boolean editorActive;
    private static final class_2960 ALBUM_TEX_ID;
    private static class_1043 albumTex;
    private static long lastCoverArtCrc;
    private static long lastPosUpdateMs;
    private static int lastPosMs;
    private static boolean lastWasPlaying;
    private static String lastTrackTitle;
    private static int lastDurationMs;
    private static boolean stablePlayingState;
    private static boolean pendingPlayingState;
    private static long pendingPlayingSinceMs;
    private static final long PLAYBACK_STATE_DEBOUNCE_MS = 280L;
    private static volatile boolean initStarted;

    public static void setEditorActive(boolean active) {
        editorActive = active;
    }

    public static boolean isEditorActive() {
        return editorActive;
    }

    private static void ensureInitialized() {
        if (!(!initStarted)) return;
        initStarted = true;
        SpotifyAccessor.initialize();
    }

    public static void render(class_332 ctx, int nativeW, int nativeH, int scaledW, int scaledH, SpotifyHUD hud) {
        class_310 client = class_310.method_1551();
        if (client == null) return;
        TessellatorRenderer.setContext(ctx);
        ensureInitialized();
        updateAlbumArt();
        float sc = hud.getScale();
        int accent = hud.getAccentColor();
        int posX = hud.getPosX();
        int posY = hud.getPosY();
        int artSize = ((int) (54.0f * sc));
        int pad = ((int) (10.0f * sc));
        int gap = ((int) (12.0f * sc));
        int textH = ((int) (9.0f * sc));
        int lineH = ExternalGlTextRenderer.lineHeight(((float) textH));
        int barH = ((int) (4.0f * sc));
        int timeGap = ((int) (5.0f * sc));
        int radius = ((int) (12.0f * sc));
        int infoW = ((int) (175.0f * sc));
        int titleYoffset = pad;
        int artistYoffset = (titleYoffset + lineH);
        int barYoffset = (((artistYoffset + lineH) + timeGap) + ((int) (8.0f * sc)));
        int timeYoffset = ((barYoffset + barH) + timeGap);
        int btnYoffset = ((timeYoffset + lineH) + ((int) (8.0f * sc)));
        int btnSize = ((int) (8.0f * sc));
        int contentBottom = (btnYoffset + (btnSize / 2));
        int panelH = (contentBottom + pad);
        int panelW = (((artSize + gap) + infoW) + (pad * 2));
        int panelX = posX;
        int panelY = posY;
        int artX = (panelX + pad);
        int titleY = (panelY + titleYoffset);
        int artistY = (panelY + artistYoffset);
        int barY = (panelY + barYoffset);
        int timeY = (panelY + timeYoffset);
        int btnY = (panelY + btnYoffset);
        int infoX = ((artX + artSize) + gap);
        int infoRightEdge = ((panelX + panelW) - pad);
        int panelStroke = withAlpha(blendColor(-15066598, accent, 0.3f), 0.95f);
        int panelGlow = withAlpha(accent, 0.14f);
        drawRoundedCardWithStroke(ctx, panelX, panelY, panelW, panelH, radius, -586347251, panelStroke, panelGlow);
        int artY = (panelY + pad);
        int artRadius = Math.max(4, Math.round((6.0f * sc)));
        int artStroke = withAlpha(blendColor(-14076600, accent, 0.26f), 0.88f);
        int artGlow = withAlpha(accent, 0.1f);
        drawRoundedCardWithStroke(ctx, artX, artY, artSize, artSize, artRadius, -15723234, artStroke, artGlow);
        int artInset = Math.max(1, Math.round((2.0f * sc)));
        int artContentX = (artX + artInset);
        int artContentY = (artY + artInset);
        int artContentSize = Math.max(2, (artSize - (artInset * 2)));
        if (albumTex != null) {
            ImmediateGuiRenderer.drawTexturedRect(ctx, albumTex, artContentX, artContentY, artContentSize, artContentSize, -1);
        } else {
            String musicNote = "\u266b";
            int noteW = client.field_1772.method_1727(musicNote);
            TessellatorRenderer.drawTextWithShadow(ctx, client.field_1772, musicNote, (artContentX + ((artContentSize - noteW) / 2)), (artContentY + ((artContentSize - 9) / 2)), accent);
        }
        String title = SpotifyAccessor.getTitle();
        String artist = SpotifyAccessor.getArtist();
        if (title.isBlank()) {
            if (!(SpotifyAccessor.isConnected())) {
                break label_60;
            }
            break label_60;
        }
        label_60:
        String displayTitle = title;
        TessellatorRenderer.drawTextWithShadow(ctx, client.field_1772, trimToWidth(client.field_1772, displayTitle, infoW), infoX, titleY, -1);
        TessellatorRenderer.drawTextWithShadow(ctx, client.field_1772, trimToWidth(client.field_1772, (artist.isBlank() ? "\u2022" : artist), infoW), infoX, artistY, -5000269);
        int posMs = interpolatePosition();
        int durMs = SpotifyAccessor.getDurationMs();
        int barW = (infoRightEdge - infoX);
        int barStroke = withAlpha(blendColor(-14013910, accent, 0.22f), 0.9f);
        TessellatorRenderer.fillRoundedRect(ctx, (infoX - 1), (barY - 1), (barW + 2), (barH + 2), ((barH + 2) / 2), barStroke);
        TessellatorRenderer.fillRoundedRect(ctx, infoX, barY, barW, barH, (barH / 2), withAlpha(3815994, 0.78f));
        if (durMs > 0 && posMs >= 0) {
            float progress = Math.max(0.0f, Math.min(1.0f, (((float) posMs) / ((float) durMs))));
            int fillW = Math.max(barH, ((int) (((float) barW) * progress)));
            TessellatorRenderer.fillRoundedRect(ctx, (infoX - 1), (barY - 1), (fillW + 2), (barH + 2), ((barH + 2) / 2), withAlpha(accent, 0.24f));
            TessellatorRenderer.fillRoundedRect(ctx, infoX, barY, fillW, barH, (barH / 2), accent);
        }
        TessellatorRenderer.drawTextWithShadow(ctx, client.field_1772, formatTime(posMs), infoX, timeY, -5000269);
        String totalStr = formatTime((durMs > 0 ? durMs : 180000));
        int totalW = TessellatorRenderer.measureTextWidth(client.field_1772, totalStr);
        TessellatorRenderer.drawTextWithShadow(ctx, client.field_1772, totalStr, (infoRightEdge - totalW), timeY, -5000269);
        drawPlaybackControls(ctx, infoX, btnY, sc, accent);
    }

    private static int interpolatePosition() {
        long now = System.currentTimeMillis();
        int rawPos = SpotifyAccessor.getPositionMs();
        int duration = SpotifyAccessor.getDurationMs();
        boolean playing = SpotifyAccessor.isPlaying();
        String title = SpotifyAccessor.getTitle();
        if (lastPosUpdateMs != 0L) {
            if (!(title.equals(lastTrackTitle))) {
            }
            lastPosUpdateMs = now;
            lastPosMs = rawPos;
            lastWasPlaying = playing;
            lastTrackTitle = title;
            lastDurationMs = duration;
            return rawPos;
        }
        long elapsed = (now - lastPosUpdateMs);
        if (!(!lastWasPlaying)) {
            lastPosMs = (lastPosMs + ((int) elapsed));
        }
        if (!playing) {
            lastPosMs = rawPos;
        }
        if (duration != lastDurationMs) {
            lastDurationMs = duration;
            lastPosMs = rawPos;
        }
        if (duration > 0 && lastPosMs > duration) {
            lastPosMs = duration;
        }
        if (lastPosMs < 0) {
            lastPosMs = 0;
        }
        lastPosUpdateMs = now;
        lastPosMs = Math.max(lastPosMs, rawPos);
        lastWasPlaying = playing;
        return lastPosMs;
    }

    private static void updateAlbumArt() {
        byte[] data = SpotifyAccessor.getCoverArt();
        if (data == null) break label_2;
        if (data.length != 0) break label_3;
        label_2:
        return;
        label_3:
        CRC32 crc = new CRC32();
        crc.update(data, 0, Math.min(data.length, 65536));
        long crcVal = crc.getValue();
        if (crcVal == lastCoverArtCrc) return;
        lastCoverArtCrc = crcVal;
        try {
            byte[] normalized = normalizeImagePayload(data);
            class_1011 img = decodeImage(normalized);
            if (img == null && normalized != data) {
                img = decodeImage(data);
            }
            if (img == null) {
                return;
                try {
                }
                if (img.method_4307() > 0) {
                    if (img.method_4323() <= 0) {
                    }
                    img.close();
                    return;
                    try {
                    }
                    class_310 mc = class_310.method_1551();
                    if (mc == null) {
                        img.close();
                        return;
                        try {
                        }
                        class_1043 newTex = new class_1043(() -> "spotify_album", img);
                        mc.method_1531().method_4615(ALBUM_TEX_ID);
                        mc.method_1531().method_4616(ALBUM_TEX_ID, newTex);
                        if ((albumTex == null) || (albumTex == newTex)) break label_27;
                        try {
                            albumTex.close();
                        } catch (Throwable ex26) {
                            Object var8 = ex26;
                        }
                        label_27:
                        albumTex = newTex;
                        log(("Album art: " + data.length + " bytes, " + img.method_4307() + "x" + img.method_4323()));
                    } catch (Throwable ex30) {
                        Throwable t = ex30;
                        log(("Album art error: " + t.getMessage()));
                    }
                }
            }
        }
    }

    private static byte[] normalizeImagePayload(byte[] data) {
        if ((data == null) || (data.length < 4)) {
            return data;
        }
        int start = findImageStartOffset(data);
        if ((start <= 0) || (start >= data.length)) {
            return data;
        }
        return Arrays.copyOfRange(data, start, data.length);
    }

    private static int findImageStartOffset(byte[] data) {
        int i = 0;
        while (i <= (data.length - 12)) {
            if ((data[i] & 255) == 137 && data[(i + 1)] == 80 && data[(i + 2)] == 78 && data[(i + 3)] == 71) return i;
            if ((data[i] & 255) == 255 && (data[(i + 1)] & 255) == 216 && (data[(i + 2)] & 255) == 255) return i;
            if (data[i] == 82 && data[(i + 1)] == 73 && data[(i + 2)] == 70 && data[(i + 3)] == 70 && data[(i + 8)] == 87 && data[(i + 9)] == 69 && data[(i + 10)] == 66 && data[(i + 11)] == 80) return i;
            i++;
        }
        return -1;
    }

    private static class_1011 decodeImage(byte[] data) {
        if (data == null) break label_1;
        if (data.length >= 4) break label_2;
        label_1:
        return null;
        label_2:
        if ((((data[0] != -119) || (data[1] != 80)) || (data[2] != 78)) || (data[3] != 71)) break label_7;
        try {
            return class_1011.method_4309(new ByteArrayInputStream(data));
        } catch (Throwable ex5) {
            Throwable t = ex5;
            log(("PNG decode failed: " + t.getMessage()));
            label_7:
            if ((data[0] != -1) || (data[1] != -40)) break label_12;
            try {
                return class_1011.method_4309(new ByteArrayInputStream(data));
            }
        } catch (Throwable ex10) {
            t = ex10;
            log(("JPEG decode failed: " + t.getMessage()));
            label_12:
            if (((((((((data.length <= 12) || (data[0] != 82)) || (data[1] != 73)) || (data[2] != 70)) || (data[3] != 70)) || (data[8] != 87)) || (data[9] != 69)) || (data[10] != 66)) || (data[11] != 80)) break label_17;
            try {
                return class_1011.method_4309(new ByteArrayInputStream(data));
            }
        } catch (Throwable ex15) {
            t = ex15;
            log(("WebP decode failed: " + t.getMessage()));
            try {
                label_17:
                return class_1011.method_4309(new ByteArrayInputStream(data));
            }
        } catch (Throwable ex19) {
            t = ex19;
            log(("NativeImage auto-decode failed: " + t.getMessage()));
            try {
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(data));
                if (bi == null) {
                    return null;
                }
                try {
                }
                int w = bi.getWidth();
                int h = bi.getHeight();
                class_1011 img = new class_1011(class_1011.class_1012.field_4997, w, h, false);
                int y = 0;
                while (y < h) {
                    int x = 0;
                    while (x < w) {
                        int argb = bi.getRGB(x, y);
                        int a = ((argb >> 24) & 255);
                        int r = ((argb >> 16) & 255);
                        int g = ((argb >> 8) & 255);
                        int b = (argb & 255);
                        img.method_61941(x, y, ((((a << 24) | (b << 16)) | (g << 8)) | r));
                        x++;
                    }
                    y++;
                }
                return img;
            } catch (Throwable ex41) {
                t = ex41;
                log(("BufferedImage fallback failed: " + t.getMessage()));
                return null;
            }
        }
    }

    private static void drawPlaybackControls(class_332 ctx, int x, int y, float sc, int accent) {
        int playSize = Math.max(7, Math.min(10, Math.round((7.2f * sc))));
        int sideSize = Math.max(5, Math.round((((float) playSize) * 0.68f)));
        int btnGap = Math.max(9, Math.min(13, Math.round((10.5f * sc))));
        int sideColor = -1710619;
        int prevX = x;
        int playX = ((prevX + sideSize) + btnGap);
        int nextX = ((playX + playSize) + btnGap);
        int sideY = (y + Math.max(0, ((playSize - sideSize) / 2)));
        drawPrevIcon(ctx, prevX, sideY, sideSize, sideColor);
        if (isPlaybackActuallyPlaying()) {
            drawPauseIcon(ctx, playX, y, playSize, accent);
        } else {
            drawPlayIcon(ctx, playX, y, playSize, accent);
        }
        drawNextIcon(ctx, nextX, sideY, sideSize, sideColor);
    }

    private static boolean isPlaybackActuallyPlaying() {
        boolean apiPlaying = SpotifyAccessor.isPlaying();
        long now = System.currentTimeMillis();
        if (pendingPlayingSinceMs == 0L) {
            stablePlayingState = apiPlaying;
            pendingPlayingState = apiPlaying;
            pendingPlayingSinceMs = now;
            return apiPlaying;
        }
        if (apiPlaying != pendingPlayingState) {
            pendingPlayingState = apiPlaying;
            pendingPlayingSinceMs = now;
        }
        if (stablePlayingState != pendingPlayingState && (now - pendingPlayingSinceMs) >= 280L) {
            stablePlayingState = pendingPlayingState;
        }
        return stablePlayingState;
    }

    private static void drawPlayIcon(class_332 ctx, int x, int y, int size, int color) {
        drawTriangleRightAA(ctx, (((float) x) + 1.0f), (((float) y) + 0.0f), Math.max(5.0f, (((float) size) - 2.0f)), Math.max(7.0f, ((float) size)), color);
    }

    private static void drawPrevIcon(class_332 ctx, int x, int y, int size, int color) {
        int barW = Math.max(1, Math.round((((float) size) * 0.18f)));
        int triW = Math.max(3, ((size - barW) - 1));
        drawVerticalBarAA(ctx, x, y, barW, size, color);
        drawTriangleLeftAA(ctx, (((float) (x + barW)) + 1.0f), ((float) y), ((float) triW), ((float) size), color);
    }

    private static void drawNextIcon(class_332 ctx, int x, int y, int size, int color) {
        int barW = Math.max(1, Math.round((((float) size) * 0.18f)));
        int triW = Math.max(3, ((size - barW) - 1));
        drawTriangleRightAA(ctx, ((float) x), ((float) y), ((float) triW), ((float) size), color);
        drawVerticalBarAA(ctx, ((x + size) - barW), y, barW, size, color);
    }

    private static void drawTriangleRightAA(class_332 ctx, float x, float y, float w, float h, int color) {
        if ((w <= 0.0f) || (h <= 0.0f)) {
            return;
        }
        float xLeft = (x + 0.25f);
        float xRight = ((x + w) - 0.25f);
        float yTop = (y + 0.35f);
        float yBottom = ((y + h) - 0.35f);
        float yMid = (y + (h * 0.5f));
        int fringe = withAlpha(color, 0.34f);
        ImmediateGuiRenderer.drawTriangle(ctx, (xLeft - 0.75f), (yTop - 0.7f), (xRight + 0.75f), yMid, (xLeft - 0.75f), (yBottom + 0.7f), fringe);
        ImmediateGuiRenderer.drawTriangle(ctx, xLeft, yTop, xRight, yMid, xLeft, yBottom, color);
    }

    private static void drawTriangleLeftAA(class_332 ctx, float x, float y, float w, float h, int color) {
        if ((w <= 0.0f) || (h <= 0.0f)) {
            return;
        }
        float xRight = ((x + w) - 0.25f);
        float xLeft = (x + 0.25f);
        float yTop = (y + 0.35f);
        float yBottom = ((y + h) - 0.35f);
        float yMid = (y + (h * 0.5f));
        int fringe = withAlpha(color, 0.34f);
        ImmediateGuiRenderer.drawTriangle(ctx, (xRight + 0.75f), (yTop - 0.7f), (xLeft - 0.75f), yMid, (xRight + 0.75f), (yBottom + 0.7f), fringe);
        ImmediateGuiRenderer.drawTriangle(ctx, xRight, yTop, xLeft, yMid, xRight, yBottom, color);
    }

    private static void drawVerticalBarAA(class_332 ctx, int x, int y, int w, int h, int color) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        int radius = Math.max(1, (w / 2));
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, radius, color);
        int fringe = withAlpha(color, 0.28f);
        if (h > 2) {
            TessellatorRenderer.fillRect(ctx, (x - 1), (y + 1), x, ((y + h) - 1), fringe);
            TessellatorRenderer.fillRect(ctx, (x + w), (y + 1), ((x + w) + 1), ((y + h) - 1), fringe);
        }
    }

    private static void drawPauseIcon(class_332 ctx, int x, int y, int size, int color) {
        int barW = Math.max(1, Math.round((((float) size) * 0.24f)));
        int gap = Math.max(1, Math.round((((float) size) * 0.22f)));
        int barsH = Math.max(6, (size - 1));
        int yStart = (y + Math.max(0, ((size - barsH) / 2)));
        drawVerticalBarAA(ctx, x, yStart, barW, barsH, color);
        drawVerticalBarAA(ctx, ((x + barW) + gap), yStart, barW, barsH, color);
    }

    private static String trimToWidth(class_327 tr, String text, int maxWidth) {
        if (((text == null) || (text.isEmpty())) || (maxWidth <= 0)) {
            return "";
        }
        return TessellatorRenderer.trimToWidth(tr, text, maxWidth);
    }

    private static String formatTime(int ms) {
        if (ms <= 0) return "0:00";
        int s = (ms / 1000);
        var tmp0 = new Object[1];
        tmp0[0] = (s % 60);
        return ((s / 60) + ":" + String.format(Locale.ROOT, "%02d", tmp0));
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round((alpha * 255.0f))));
        return ((a << 24) | (rgb & 16777215));
    }

    private static int blendColor(int a, int b, float t) {
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

    private static void drawRoundedCardWithStroke(class_332 ctx, int x, int y, int w, int h, int radius, int fillColor, int strokeColor, int glowColor) {
        if (((ctx == null) || (w <= 0)) || (h <= 0)) {
            return;
        }
        int r = Math.max(2, Math.min((Math.min(w, h) / 2), radius));
        int outerW = (w + 4);
        int outerH = (h + 4);
        int outerR = Math.max(2, Math.min((Math.min(outerW, outerH) / 2), (r + 2)));
        TessellatorRenderer.fillRoundedRect(ctx, (x - 2), (y - 2), outerW, outerH, outerR, glowColor);
        int strokeW = (w + 2);
        int strokeH = (h + 2);
        int strokeR = Math.max(2, Math.min((Math.min(strokeW, strokeH) / 2), (r + 1)));
        TessellatorRenderer.fillRoundedRect(ctx, (x - 1), (y - 1), strokeW, strokeH, strokeR, strokeColor);
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, r, fillColor);
    }

    private static void log(String msg) {
        VieCheatsMod.LOGGER.info(("[SpotiPlay] " + msg));
    }

    static {
        editorActive = false;
        ALBUM_TEX_ID = class_2960.method_60655("viecheats", "spotify_album");
        lastCoverArtCrc = -1L;
        lastPosUpdateMs = 0L;
        lastPosMs = 0;
        lastWasPlaying = false;
        lastTrackTitle = "";
        lastDurationMs = 0;
        stablePlayingState = false;
        pendingPlayingState = false;
        pendingPlayingSinceMs = 0L;
        initStarted = false;
    }

}

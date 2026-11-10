package me.drake.viecheats.util;

public final class RainbowUtil {
    public static int rainbow(double speed, double saturation, long offset, int alpha) {
        float hue = (((float) ((((double) ((System.currentTimeMillis() + (offset * 200L)) % 3600L)) / 3600.0) * speed)) % 1.0f);
        int rgb = hsbToRgb(hue, ((float) saturation), 1.0f);
        return (((alpha & 255) << 24) | (rgb & 16777215));
    }

    public static int rainbow(double speed, long offset) {
        return rainbow(speed, 1.0, offset, 255);
    }

    public static int lerpColor(int colorA, int colorB, double factor) {
        factor = Math.max(0.0, Math.min(1.0, factor));
        int aA = ((colorA >> 24) & 255);
        int rA = ((colorA >> 16) & 255);
        int gA = ((colorA >> 8) & 255);
        int bA = (colorA & 255);
        int aB = ((colorB >> 24) & 255);
        int rB = ((colorB >> 16) & 255);
        int gB = ((colorB >> 8) & 255);
        int bB = (colorB & 255);
        int a = ((int) (((double) aA) + (((double) (aB - aA)) * factor)));
        int r = ((int) (((double) rA) + (((double) (rB - rA)) * factor)));
        int g = ((int) (((double) gA) + (((double) (gB - gA)) * factor)));
        int b = ((int) (((double) bA) + (((double) (bB - bA)) * factor)));
        return ((((a << 24) | (r << 16)) | (g << 8)) | b);
    }

    public static int distanceColor(double distSq, double maxDistSq, int nearColor, int farColor) {
        double factor = Math.min(1.0, (distSq / maxDistSq));
        return lerpColor(nearColor, farColor, factor);
    }

    public static int pulse(int baseColor, double speed, int minAlpha, int maxAlpha) {
        double time = ((((double) System.currentTimeMillis()) / 1000.0) * speed);
        double factor = ((Math.sin(((time * 3.141592653589793) * 2.0)) + 1.0) / 2.0);
        int alpha = ((int) (((double) minAlpha) + (((double) (maxAlpha - minAlpha)) * factor)));
        return ((alpha << 24) | (baseColor & 16777215));
    }

    public static int withAlpha(int color, int alpha) {
        return (((alpha & 255) << 24) | (color & 16777215));
    }

    public static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0.0f) {
            var tmp0 = ((int) ((brightness * 255.0f) + 0.5f));
            b = tmp0;
            g = tmp0;
            r = tmp0;
            break label_17;
        }
        float h = ((hue - ((float) Math.floor(((double) hue)))) * 6.0f);
        float f = (h - ((float) Math.floor(((double) h))));
        float p = (brightness * (1.0f - saturation));
        float q = (brightness * (1.0f - (saturation * f)));
        float t = (brightness * (1.0f - (saturation * (1.0f - f))));
        switch (((int) h)) {
            case 0 -> { break label_11; }
            case 1 -> { break label_12; }
            case 2 -> { break label_13; }
            case 3 -> { break label_14; }
            case 4 -> { break label_15; }
            case 5 -> { break label_16; }
            default -> { break label_17; }
        }
        label_11:
        r = ((int) ((brightness * 255.0f) + 0.5f));
        g = ((int) ((t * 255.0f) + 0.5f));
        b = ((int) ((p * 255.0f) + 0.5f));
        break label_17;
        label_12:
        r = ((int) ((q * 255.0f) + 0.5f));
        g = ((int) ((brightness * 255.0f) + 0.5f));
        b = ((int) ((p * 255.0f) + 0.5f));
        break label_17;
        label_13:
        r = ((int) ((p * 255.0f) + 0.5f));
        g = ((int) ((brightness * 255.0f) + 0.5f));
        b = ((int) ((t * 255.0f) + 0.5f));
        break label_17;
        label_14:
        r = ((int) ((p * 255.0f) + 0.5f));
        g = ((int) ((q * 255.0f) + 0.5f));
        b = ((int) ((brightness * 255.0f) + 0.5f));
        break label_17;
        label_15:
        r = ((int) ((t * 255.0f) + 0.5f));
        g = ((int) ((p * 255.0f) + 0.5f));
        b = ((int) ((brightness * 255.0f) + 0.5f));
        break label_17;
        label_16:
        r = ((int) ((brightness * 255.0f) + 0.5f));
        g = ((int) ((p * 255.0f) + 0.5f));
        b = ((int) ((q * 255.0f) + 0.5f));
        label_17:
        return (((r << 16) | (g << 8)) | b);
    }

}

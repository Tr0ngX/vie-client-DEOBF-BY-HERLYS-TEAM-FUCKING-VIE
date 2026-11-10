package me.drake.viecheats.modules.donut;

import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_238;

public final class ActiveChunkRenderer {
    public static void drawSheet(double x1, double z1, double x2, double z2, int y, int fillColor, int outlineColor) {
        double minY = ((double) y);
        double maxY = (((double) y) + 0.02);
        class_238 sheet = new class_238(x1, minY, z1, x2, maxY, z2);
        RenderUtil.drawFilledBox(sheet, fillColor);
        RenderUtil.drawBox(sheet, outlineColor);
    }

    public static void drawPillar(double x1, double z1, double x2, double z2, int worldMinY, int worldMaxY, int fillColor, int outlineColor) {
        class_238 pillar = new class_238(x1, ((double) worldMinY), z1, x2, (((double) worldMaxY) + 1.0), z2);
        RenderUtil.drawFilledBox(pillar, fillColor);
        RenderUtil.drawBox(pillar, outlineColor);
    }

    public static void drawTracer(double px, double py, double pz, double tx, double ty, double tz, int color, float tickDelta) {
        RenderUtil.drawTracerToPos(tx, ty, tz, color, tickDelta);
    }

}

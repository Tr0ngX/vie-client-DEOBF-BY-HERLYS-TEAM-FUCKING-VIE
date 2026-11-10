package me.drake.viecheats.util;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.render.VieMeshBuilder;
import me.drake.viecheats.render.VieMeshRenderer;
import me.drake.viecheats.render.ViePipelines;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

public final class RenderUtil {
    private static final VieMeshBuilder linesMesh;
    private static final VieMeshBuilder trianglesMesh;
    private static final double TRACER_NEAR_OFFSET = 0.1;
    private static boolean allowOutlineThisFrame;
    private static boolean allowFillThisFrame;
    private static boolean allowTracerThisFrame;
    private static boolean cachedCrosshairOriginValid;
    private static double cachedCrosshairOriginX;
    private static double cachedCrosshairOriginY;
    private static double cachedCrosshairOriginZ;

    public static void begin() {
        linesMesh.begin();
        trianglesMesh.begin();
        updateCrosshairOriginCache();
        updateAdaptiveDrawFlags();
    }

    public static void render(class_4587 matrices) {
        try {
            if (!(linesMesh.hasData())) break label_2;
            VieMeshRenderer.render(ViePipelines.WORLD_LINES, linesMesh, matrices);
            break label_3;
            label_2:
            if (!(linesMesh.isBuilding())) break label_3;
            linesMesh.end();
            label_3:
        } catch (Exception ex4) {
            Exception e = ex4;
            VieCheatsMod.LOGGER.error("[VieCheat] Error rendering lines mesh", e);
            if (!(linesMesh.isBuilding())) break label_10;
            try {
                linesMesh.end();
            } catch (Exception ex9) {
                Object var2 = ex9;
                try {
                }
            }
            label_10:
            if (trianglesMesh.hasData()) {
                VieMeshRenderer.render(ViePipelines.WORLD_TRIANGLES, trianglesMesh, matrices);
                break label_13;
            }
            if (!(trianglesMesh.isBuilding())) break label_13;
            trianglesMesh.end();
            label_13:
        } catch (Exception ex14) {
            e = ex14;
            VieCheatsMod.LOGGER.error("[VieCheat] Error rendering triangles mesh", e);
            if (!(trianglesMesh.isBuilding())) break label_20;
            try {
                trianglesMesh.end();
            } catch (Exception ex19) {
                var2 = ex19;
            }
        }
        label_20:
    }

    public static class_238 getInterpolatedBox(class_1297 entity, float tickDelta) {
        double x = class_3532.method_16436(((double) tickDelta), entity.field_6038, entity.method_23317());
        double y = class_3532.method_16436(((double) tickDelta), entity.field_5971, entity.method_23318());
        double z = class_3532.method_16436(((double) tickDelta), entity.field_5989, entity.method_23321());
        class_238 base = entity.method_5829();
        double hw = ((base.field_1320 - base.field_1323) / 2.0);
        double hd = ((base.field_1324 - base.field_1321) / 2.0);
        double h = (base.field_1325 - base.field_1322);
        return new class_238((x - hw), y, (z - hd), (x + hw), (y + h), (z + hd));
    }

    public static void drawBox(class_238 box, int color) {
        if (!allowOutlineThisFrame) return;
        float r = ((float) ((color >> 16) & 255));
        float g = ((float) ((color >> 8) & 255));
        float b = ((float) (color & 255));
        float a = ((float) ((color >> 24) & 255));
        if (a <= 0.0f) return;
        double x1 = box.field_1323;
        double y1 = box.field_1322;
        double z1 = box.field_1321;
        double x2 = box.field_1320;
        double y2 = box.field_1325;
        double z2 = box.field_1324;
        linesMesh.ensureCapacity(8, 24);
        int blb = linesMesh.vec3(x1, y1, z1).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int blf = linesMesh.vec3(x1, y1, z2).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int brb = linesMesh.vec3(x2, y1, z1).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int brf = linesMesh.vec3(x2, y1, z2).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int tlb = linesMesh.vec3(x1, y2, z1).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int tlf = linesMesh.vec3(x1, y2, z2).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int trb = linesMesh.vec3(x2, y2, z1).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int trf = linesMesh.vec3(x2, y2, z2).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        linesMesh.line(blb, blf);
        linesMesh.line(brb, brf);
        linesMesh.line(blb, brb);
        linesMesh.line(blf, brf);
        linesMesh.line(tlb, tlf);
        linesMesh.line(trb, trf);
        linesMesh.line(tlb, trb);
        linesMesh.line(tlf, trf);
        linesMesh.line(blb, tlb);
        linesMesh.line(blf, tlf);
        linesMesh.line(brb, trb);
        linesMesh.line(brf, trf);
    }

    public static void drawFilledBox(class_238 box, int color) {
        if (!allowFillThisFrame) return;
        float r = ((float) ((color >> 16) & 255));
        float g = ((float) ((color >> 8) & 255));
        float b = ((float) (color & 255));
        int a = ((color >> 24) & 255);
        if (a <= 0) return;
        double x1 = box.field_1323;
        double y1 = box.field_1322;
        double z1 = box.field_1321;
        double x2 = box.field_1320;
        double y2 = box.field_1325;
        double z2 = box.field_1324;
        trianglesMesh.ensureCapacity(8, 36);
        int blb = trianglesMesh.vec3(x1, y1, z1).color(((int) r), ((int) g), ((int) b), a).next();
        int blf = trianglesMesh.vec3(x1, y1, z2).color(((int) r), ((int) g), ((int) b), a).next();
        int brb = trianglesMesh.vec3(x2, y1, z1).color(((int) r), ((int) g), ((int) b), a).next();
        int brf = trianglesMesh.vec3(x2, y1, z2).color(((int) r), ((int) g), ((int) b), a).next();
        int tlb = trianglesMesh.vec3(x1, y2, z1).color(((int) r), ((int) g), ((int) b), a).next();
        int tlf = trianglesMesh.vec3(x1, y2, z2).color(((int) r), ((int) g), ((int) b), a).next();
        int trb = trianglesMesh.vec3(x2, y2, z1).color(((int) r), ((int) g), ((int) b), a).next();
        int trf = trianglesMesh.vec3(x2, y2, z2).color(((int) r), ((int) g), ((int) b), a).next();
        trianglesMesh.quad(blb, blf, tlf, tlb);
        trianglesMesh.quad(brb, trb, trf, brf);
        trianglesMesh.quad(blb, tlb, trb, brb);
        trianglesMesh.quad(blf, brf, trf, tlf);
        trianglesMesh.quad(blb, brb, brf, blf);
        trianglesMesh.quad(tlb, tlf, trf, trb);
    }

    public static void drawTracer(class_1297 target, int color, float tickDelta) {
        if (target == null) return;
        class_238 box = getInterpolatedBox(target, tickDelta);
        double tx = ((box.field_1323 + box.field_1320) * 0.5);
        double ty = ((box.field_1322 + box.field_1325) * 0.5);
        double tz = ((box.field_1321 + box.field_1324) * 0.5);
        drawTracerToPos(tx, ty, tz, color, tickDelta);
    }

    @Deprecated
    public static void drawTracer(class_1297 target, int color) {
        drawTracer(target, color, 1.0f);
    }

    public static void drawTracerQuad(class_1297 target, int color, float tickDelta) {
        class_310 mc = class_310.method_1551();
        if ((mc.field_1724 == null) || (mc.field_1773 == null)) {
            return;
        }
        double tx = class_3532.method_16436(((double) tickDelta), target.field_6038, target.method_23317());
        double ty = (class_3532.method_16436(((double) tickDelta), target.field_5971, target.method_23318()) + (((double) target.method_17682()) / 2.0));
        double tz = class_3532.method_16436(((double) tickDelta), target.field_5989, target.method_23321());
        drawTracerToPos(tx, ty, tz, color, tickDelta);
    }

    public static void drawTracerToPos(double tx, double ty, double tz, int color) {
        drawTracerToPos(tx, ty, tz, color, 1.0f);
    }

    public static void drawTracerToPos(double tx, double ty, double tz, int color, float tickDelta) {
        if (!allowTracerThisFrame) return;
        if (!(!cachedCrosshairOriginValid)) {
            double sx = cachedCrosshairOriginX;
            double sy = cachedCrosshairOriginY;
            double sz = cachedCrosshairOriginZ;
        } else {
            class_243 start = computeCrosshairOrigin(tickDelta);
            if (start == null) return;
            sx = start.field_1352;
            sy = start.field_1351;
            sz = start.field_1350;
        }
        drawLine(sx, sy, sz, tx, ty, tz, color);
    }

    private static class_243 computeCrosshairOrigin(float tickDelta) {
        class_310 mc = class_310.method_1551();
        if (mc == null) return null;
        if (mc.field_1773 != null && mc.field_1773.method_19418() != null) {
            class_4184 camera = mc.field_1773.method_19418();
            class_243 eye = camera.method_71156();
            class_243 look = class_243.method_1030(camera.method_19329(), camera.method_19330());
            if (look.method_1027() > 1.0E-6) {
                eye = eye.method_1019(look.method_1029().method_1021(0.1));
            }
            return eye;
        }
        if (mc.field_1724 != null) {
            eye = mc.field_1724.method_5836(tickDelta);
            look = class_243.method_1030(mc.field_1724.method_36455(), mc.field_1724.method_36454());
            if (look.method_1027() > 1.0E-6) {
                eye = eye.method_1019(look.method_1029().method_1021(0.1));
            }
            return eye;
        }
        return null;
    }

    private static void updateCrosshairOriginCache() {
        class_310 mc = class_310.method_1551();
        if (mc != null) {
            if (mc.method_61966() == null) {
            }
            cachedCrosshairOriginValid = false;
            return;
        }
        class_243 origin = computeCrosshairOrigin(mc.method_61966().method_60637(true));
        if (origin == null) {
            cachedCrosshairOriginValid = false;
            return;
        }
        cachedCrosshairOriginValid = true;
        cachedCrosshairOriginX = origin.field_1352;
        cachedCrosshairOriginY = origin.field_1351;
        cachedCrosshairOriginZ = origin.field_1350;
    }

    private static void updateAdaptiveDrawFlags() {
        allowOutlineThisFrame = true;
        allowFillThisFrame = true;
        allowTracerThisFrame = true;
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        float r = ((float) ((color >> 16) & 255));
        float g = ((float) ((color >> 8) & 255));
        float b = ((float) (color & 255));
        float a = ((float) ((color >> 24) & 255));
        if (a <= 0.0f) return;
        linesMesh.ensureCapacity(2, 2);
        int i1 = linesMesh.vec3(x1, y1, z1).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        int i2 = linesMesh.vec3(x2, y2, z2).color(((int) r), ((int) g), ((int) b), ((int) a)).next();
        linesMesh.line(i1, i2);
    }

    public static void drawFlatPlane(double x1, double z1, double x2, double z2, double y, int color) {
        int r = ((color >> 16) & 255);
        int g = ((color >> 8) & 255);
        int b = (color & 255);
        int a = ((color >> 24) & 255);
        if (a <= 0) return;
        trianglesMesh.ensureCapacity(4, 12);
        int v0 = trianglesMesh.vec3(x1, y, z1).color(r, g, b, a).next();
        int v1 = trianglesMesh.vec3(x1, y, z2).color(r, g, b, a).next();
        int v2 = trianglesMesh.vec3(x2, y, z2).color(r, g, b, a).next();
        int v3 = trianglesMesh.vec3(x2, y, z1).color(r, g, b, a).next();
        trianglesMesh.quad(v0, v1, v2, v3);
        trianglesMesh.quad(v3, v2, v1, v0);
    }

    public static void drawFlatPlaneBorder(double x1, double z1, double x2, double z2, double y, int color) {
        int r = ((color >> 16) & 255);
        int g = ((color >> 8) & 255);
        int b = (color & 255);
        int a = ((color >> 24) & 255);
        if (a <= 0) return;
        linesMesh.ensureCapacity(4, 8);
        int v0 = linesMesh.vec3(x1, y, z1).color(r, g, b, a).next();
        int v1 = linesMesh.vec3(x1, y, z2).color(r, g, b, a).next();
        int v2 = linesMesh.vec3(x2, y, z2).color(r, g, b, a).next();
        int v3 = linesMesh.vec3(x2, y, z1).color(r, g, b, a).next();
        linesMesh.line(v0, v1);
        linesMesh.line(v1, v2);
        linesMesh.line(v2, v3);
        linesMesh.line(v3, v0);
    }

    static {
        linesMesh = new VieMeshBuilder(ViePipelines.WORLD_LINES);
        trianglesMesh = new VieMeshBuilder(ViePipelines.WORLD_TRIANGLES);
        allowOutlineThisFrame = true;
        allowFillThisFrame = true;
        allowTracerThisFrame = true;
    }

}

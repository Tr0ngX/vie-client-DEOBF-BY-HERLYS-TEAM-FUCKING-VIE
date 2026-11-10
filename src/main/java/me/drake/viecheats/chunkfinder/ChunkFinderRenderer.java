package me.drake.viecheats.chunkfinder;

import java.util.Set;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_12249;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_9974;

public final class ChunkFinderRenderer {
    private static final int FLAG_PLATE_Y = 63;
    private static final class_265 CHUNK_PLATE_SHAPE;
    private static final int FLAG_COLOR = -1593890262;
    private static final int CORE_TRACER_COLOR = -922801622;
    private static final float OUTLINE_WIDTH = 1.6f;
    private static final int MAX_RENDERED_CHUNKS = 4096;
    private static final float TRACER_WIDTH = 2.0f;
    private final ChunkFinderScanner scanner;
    private final BooleanSupplier enabledSupplier;

    public ChunkFinderRenderer(ChunkFinderScanner scanner, BooleanSupplier enabledSupplier) {
        this.scanner = scanner;
        this.enabledSupplier = enabledSupplier;
    }

    public void onWorldRender(WorldRenderContext context) {
        if (context == null) break label_1;
        if (this.enabledSupplier.getAsBoolean()) break label_2;
        label_1:
        return;
        label_2:
        Set flaggedChunks = this.scanner.getFlaggedChunksSnapshot();
        if (flaggedChunks.isEmpty()) return;
        Set coreChunks = this.scanner.getCoreChunksSnapshot();
        class_4587 matrices = context.matrices();
        if (matrices == null) return;
        class_310 mc = class_310.method_1551();
        if (((mc == null) || (mc.field_1773 == null)) || (mc.field_1773.method_19418() == null)) {
            return;
        }
        class_243 cameraPos = mc.field_1773.method_19418().method_71156();
        class_4588 lines = context.consumers().method_73477(class_12249.method_76015());
        matrices.method_22903();
        matrices.method_22904(-(cameraPos.field_1352), -(cameraPos.field_1351), -(cameraPos.field_1350));
        int rendered = 0;
        Object var9 = flaggedChunks.iterator();
        while (var9.hasNext()) {
            class_1923 cp = ((class_1923) var9.next());
            class_9974.method_62296(matrices, lines, CHUNK_PLATE_SHAPE, ((double) cp.method_8326()), 63.0, ((double) cp.method_8328()), -1593890262, 1.6f);
            rendered++;
            if (rendered >= 4096) {
                break label_24;
            }
        }
        label_24:
        var9 = coreChunks.iterator();
        while (var9.hasNext()) {
            cp = ((class_1923) var9.next());
            double tx = (((double) cp.method_8326()) + 8.0);
            double ty = 63.5;
            double tz = (((double) cp.method_8328()) + 8.0);
            drawTracerLine(matrices, lines, cameraPos, tx, ty, tz, -922801622);
        }
        matrices.method_22909();
    }

    private static void drawTracerLine(class_4587 matrices, class_4588 lines, class_243 cameraPos, double tx, double ty, double tz, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        int r = ((color >> 16) & 255);
        int g = ((color >> 8) & 255);
        int b = (color & 255);
        int a = ((color >> 24) & 255);
        lines.method_56824(entry, ((float) cameraPos.field_1352), ((float) cameraPos.field_1351), ((float) cameraPos.field_1350)).method_1336(r, g, b, a).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        lines.method_56824(entry, ((float) tx), ((float) ty), ((float) tz)).method_1336(r, g, b, a).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
    }

    static {
        CHUNK_PLATE_SHAPE = class_259.method_1081(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    }

}

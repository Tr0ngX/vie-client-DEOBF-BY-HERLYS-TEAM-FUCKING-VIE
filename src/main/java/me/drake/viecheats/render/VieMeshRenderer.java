package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_11280;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import org.joml.Matrix4f;

public class VieMeshRenderer {
    private static final int UBO_SIZE;
    private static final VieMeshRenderer.MeshData MESH_DATA;
    private static class_11280 uboStorage;
    public static Matrix4f projection;
    public static Matrix4f lastProjectionMatrix;
    public static Matrix4f lastPositionMatrix;
    public static boolean hasCapturedWorldMatrices;

    public static void flipFrame() {
        if (uboStorage != null) {
            uboStorage.method_71100();
        }
    }

    public static void render(RenderPipeline pipeline, VieMeshBuilder mesh, class_4587 matrices) {
        if (!(mesh.isBuilding())) break label_1;
        mesh.end();
        label_1:
        int indexCount = mesh.getIndicesCount();
        if (indexCount <= 0) return;
        class_310 mc = class_310.method_1551();
        class_276 fb = mc.method_1522();
        RenderSystem.getModelViewStack().pushMatrix();
        if (matrices != null) {
            RenderSystem.getModelViewStack().mul(matrices.method_23760().method_23761());
        }
        GpuBuffer vertexBuffer = mesh.getVertexBuffer();
        GpuBuffer indexBuffer = mesh.getIndexBuffer();
        GpuBufferSlice meshData = writeMeshData(projection, RenderSystem.getModelViewStack());
        if (pipeline.wantsDepthTexture()) {
            RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie ESP", fb.method_71639(), OptionalInt.empty(), fb.method_71640(), OptionalDouble.empty());
        } else {
            pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Vie ESP", fb.method_71639(), OptionalInt.empty());
        }
        pass.setPipeline(pipeline);
        pass.setUniform("MeshData", meshData);
        pass.setVertexBuffer(0, vertexBuffer);
        pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
        pass.drawIndexed(0, 0, indexCount, 1);
        pass.close();
        RenderSystem.getModelViewStack().popMatrix();
    }

    private static GpuBufferSlice writeMeshData(Matrix4f proj, Matrix4f modelView) {
        MESH_DATA.proj = proj;
        MESH_DATA.modelView = new Matrix4f(modelView);
        return getUboStorage().method_71102(MESH_DATA);
    }

    private static class_11280 getUboStorage() {
        if (uboStorage == null) {
            uboStorage = new class_11280("Vie - Mesh UBO", UBO_SIZE, 64);
        }
        return uboStorage;
    }

    static {
        UBO_SIZE = new Std140SizeCalculator().putMat4f().putMat4f().get();
        MESH_DATA = new VieMeshRenderer.MeshData();
        projection = new Matrix4f();
        lastProjectionMatrix = new Matrix4f();
        lastPositionMatrix = new Matrix4f();
        hasCapturedWorldMatrices = false;
    }


    final class MeshData implements class_11280.class_11281 {
        Matrix4f proj;
        Matrix4f modelView;

        public void method_71104(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putMat4f(this.proj).putMat4f(this.modelView);
        }

        public boolean equals(Object o) {
            return false;
        }

    }
}

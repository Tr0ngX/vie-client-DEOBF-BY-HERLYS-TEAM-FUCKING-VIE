package me.drake.viecheats.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

public class VieMeshBuilder {
    private final VertexFormat format;
    private final int vertexSize;
    private ByteBuffer vertices;
    private long verticesStart;
    private long verticesPtr;
    private ByteBuffer indices;
    private long indicesPtr;
    private int vertexI;
    private int indicesCount;
    private boolean building;
    private double cameraX;
    private double cameraY;
    private double cameraZ;

    public VieMeshBuilder(RenderPipeline pipeline) {
        this.format = pipeline.getVertexFormat();
        this.vertexSize = this.format.getVertexSize();
    }

    public void begin() {
        if (!(!this.building)) throw new IllegalStateException("VieMeshBuilder.begin() called while building");
        if (this.vertices == null) {
            allocate(1024, 2048);
        }
        this.verticesPtr = this.verticesStart;
        this.vertexI = 0;
        this.indicesCount = 0;
        this.building = true;
        class_310 mc = class_310.method_1551();
        if (mc.field_1773 != null && mc.field_1773.method_19418() != null) {
            class_243 cam = mc.field_1773.method_19418().method_71156();
            this.cameraX = cam.field_1352;
            this.cameraY = cam.field_1351;
            this.cameraZ = cam.field_1350;
        } else {
            this.cameraX = 0.0;
            this.cameraY = 0.0;
            this.cameraZ = 0.0;
        }
    }

    public void beginNoCamera() {
        if (!(!this.building)) throw new IllegalStateException("VieMeshBuilder.beginNoCamera() called while building");
        if (this.vertices == null) {
            allocate(1024, 2048);
        }
        this.verticesPtr = this.verticesStart;
        this.vertexI = 0;
        this.indicesCount = 0;
        this.building = true;
        this.cameraX = 0.0;
        this.cameraY = 0.0;
        this.cameraZ = 0.0;
    }

    public VieMeshBuilder vec3(double x, double y, double z) {
        ensureVertexCapacity(1);
        long p = this.verticesPtr;
        MemoryUtil.memPutFloat(p, ((float) (x - this.cameraX)));
        MemoryUtil.memPutFloat((p + 4L), ((float) (y - this.cameraY)));
        MemoryUtil.memPutFloat((p + 8L), ((float) (z - this.cameraZ)));
        this.verticesPtr = (this.verticesPtr + 12L);
        return this;
    }

    public VieMeshBuilder color(int r, int g, int b, int a) {
        long p = this.verticesPtr;
        MemoryUtil.memPutByte(p, ((byte) r));
        MemoryUtil.memPutByte((p + 1L), ((byte) g));
        MemoryUtil.memPutByte((p + 2L), ((byte) b));
        MemoryUtil.memPutByte((p + 3L), ((byte) a));
        this.verticesPtr = (this.verticesPtr + 4L);
        return this;
    }

    public VieMeshBuilder color(int argb) {
        return color(((argb >> 16) & 255), ((argb >> 8) & 255), (argb & 255), ((argb >> 24) & 255));
    }

    public VieMeshBuilder uv(float u, float v) {
        long p = this.verticesPtr;
        MemoryUtil.memPutFloat(p, u);
        MemoryUtil.memPutFloat((p + 4L), v);
        this.verticesPtr = (this.verticesPtr + 8L);
        return this;
    }

    public int next() {
        this.vertexI = (this.vertexI + 1);
        return this.vertexI;
    }

    public void line(int i1, int i2) {
        ensureIndexCapacity(2);
        long p = (this.indicesPtr + (((long) this.indicesCount) * 4L));
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt((p + 4L), i2);
        this.indicesCount = (this.indicesCount + 2);
    }

    public void quad(int i1, int i2, int i3, int i4) {
        ensureIndexCapacity(6);
        long p = (this.indicesPtr + (((long) this.indicesCount) * 4L));
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt((p + 4L), i2);
        MemoryUtil.memPutInt((p + 8L), i3);
        MemoryUtil.memPutInt((p + 12L), i3);
        MemoryUtil.memPutInt((p + 16L), i4);
        MemoryUtil.memPutInt((p + 20L), i1);
        this.indicesCount = (this.indicesCount + 6);
    }

    public void ensureCapacity(int vertexCount, int indexCount) {
        ensureVertexCapacity(vertexCount);
        ensureIndexCapacity(indexCount);
    }

    private void ensureVertexCapacity(int count) {
        if (this.vertices == null) {
            allocate(1024, 2048);
            return;
        }
        if (((this.vertexI + count) * this.vertexSize) >= this.vertices.capacity()) {
            int offset = getVertexOffset();
            int newSize = Math.max((this.vertices.capacity() * 2), (this.vertices.capacity() + (count * this.vertexSize)));
            ByteBuffer newVerts = BufferUtils.createByteBuffer(newSize);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(this.vertices), MemoryUtil.memAddress0(newVerts), ((long) offset));
            this.vertices = newVerts;
            this.verticesStart = MemoryUtil.memAddress0(this.vertices);
            this.verticesPtr = (this.verticesStart + ((long) offset));
        }
    }

    private void ensureIndexCapacity(int count) {
        if (this.indices == null) {
            allocate(1024, 2048);
            return;
        }
        if (((this.indicesCount + count) * 4) >= this.indices.capacity()) {
            int newSize = Math.max((this.indices.capacity() * 2), (this.indices.capacity() + (count * 4)));
            ByteBuffer newIdx = BufferUtils.createByteBuffer(newSize);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(this.indices), MemoryUtil.memAddress0(newIdx), (((long) this.indicesCount) * 4L));
            this.indices = newIdx;
            this.indicesPtr = MemoryUtil.memAddress0(this.indices);
        }
    }

    private void allocate(int vertexCount, int indexCount) {
        this.vertices = BufferUtils.createByteBuffer((this.vertexSize * vertexCount));
        this.verticesStart = MemoryUtil.memAddress0(this.vertices);
        null.verticesPtr = this;
        this.indices = BufferUtils.createByteBuffer((indexCount * 4));
        this.indicesPtr = MemoryUtil.memAddress0(this.indices);
    }

    public void end() {
        if (!this.building) throw new IllegalStateException("VieMeshBuilder.end() called while not building");
        this.building = false;
    }

    public boolean isBuilding() {
        return this.building;
    }

    public boolean hasData() {
        return this.indicesCount > 0;
    }

    public GpuBuffer getVertexBuffer() {
        this.vertices.limit(getVertexOffset());
        return this.format.uploadImmediateVertexBuffer(this.vertices);
    }

    public GpuBuffer getIndexBuffer() {
        this.indices.limit((this.indicesCount * 4));
        return this.format.uploadImmediateIndexBuffer(this.indices);
    }

    public int getIndicesCount() {
        return this.indicesCount;
    }

    private int getVertexOffset() {
        return ((int) (this.verticesPtr - this.verticesStart));
    }

}

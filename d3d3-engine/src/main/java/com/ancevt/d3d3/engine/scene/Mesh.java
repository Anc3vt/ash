package com.ancevt.d3d3.engine.scene;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private final int vaoId;
    private final int vboId;
    private final int vertexCount;
    private final int stride = 8; // xyz, uv, normal

    // Оригинальные вершины (храним для пересчёта UV без накопления ошибок)
    private final float[] originalVertices;

    public Mesh(float[] vertices, int stride) {
        this.vertexCount = vertices.length / stride;
        this.originalVertices = vertices.clone();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        // position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride * Float.BYTES, 0);

        // texCoord
        if (stride >= 5) {
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, stride * Float.BYTES, 3 * Float.BYTES);
        }

        // normal
        if (stride >= 8) {
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, stride * Float.BYTES, 5 * Float.BYTES);
        }

        MemoryUtil.memFree(buffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }

    /** Возвращает список треугольников (для коллизий и пр.) */
    public List<Vector3f[]> getTriangles() {
        List<Vector3f[]> tris = new ArrayList<>();

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertexCount * stride);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffer);

        float[] verts = new float[vertexCount * stride];
        buffer.get(verts);
        MemoryUtil.memFree(buffer);

        for (int i = 0; i < verts.length; i += stride * 3) {
            Vector3f v1 = new Vector3f(verts[i],     verts[i + 1],  verts[i + 2]);
            Vector3f v2 = new Vector3f(verts[i + 8], verts[i + 9],  verts[i + 10]);
            Vector3f v3 = new Vector3f(verts[i + 16],verts[i + 17], verts[i + 18]);
            tris.add(new Vector3f[]{v1, v2, v3});
        }
        return tris;
    }

    /** ✅ Масштабирует UV относительно исходных координат */
    public void scaleUV(float uScale, float vScale) {
        float[] verts = originalVertices.clone();

        for (int i = 0; i < verts.length; i += stride) {
            verts[i + 3] *= uScale; // U
            verts[i + 4] *= vScale; // V
        }

        uploadToGPU(verts);
    }

    /** Сбрасывает UV в исходное состояние */
    public void resetUV() {
        uploadToGPU(originalVertices);
    }

    /** Вспомогательный метод: загружает массив вершин в GPU */
    private void uploadToGPU(float[] verts) {
        FloatBuffer newBuffer = MemoryUtil.memAllocFloat(verts.length);
        newBuffer.put(verts).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, newBuffer);

        MemoryUtil.memFree(newBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public float[] getVertices() {
        return originalVertices;
    }

    public int getStride() {
        return stride;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}

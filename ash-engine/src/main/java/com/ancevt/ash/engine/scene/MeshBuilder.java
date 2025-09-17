package com.ancevt.ash.engine.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final int stride;

    public MeshBuilder(int stride) {
        this.stride = stride;
    }

    public MeshBuilder addMesh(Mesh mesh, float offsetX, float offsetY, float offsetZ) {
        float[] verts = mesh.getVertices();
        int stride = mesh.getStride();

        for (int i = 0; i < verts.length; i += stride) {
            // позиция
            vertices.add(verts[i]     + offsetX);
            vertices.add(verts[i + 1] + offsetY);
            vertices.add(verts[i + 2] + offsetZ);

            // UV
            vertices.add(verts[i + 3]);
            vertices.add(verts[i + 4]);

            // нормали
            vertices.add(verts[i + 5]);
            vertices.add(verts[i + 6]);
            vertices.add(verts[i + 7]);
        }
        return this;
    }

    public MeshBuilder addMeshTransformed(Mesh mesh, Matrix4f transform) {
        float[] verts = mesh.getVertices();
        int stride = mesh.getStride();

        for (int i = 0; i < verts.length; i += stride) {
            // Позиция вершины
            Vector3f pos = new Vector3f(verts[i], verts[i + 1], verts[i + 2]);
            pos.mulPosition(transform);

            // Нормаль
            Vector3f normal = new Vector3f(verts[i + 5], verts[i + 6], verts[i + 7]);
            normal.mulDirection(transform).normalize();

            // Добавляем в массив
            vertices.add(pos.x);
            vertices.add(pos.y);
            vertices.add(pos.z);

            vertices.add(verts[i + 3]); // UV
            vertices.add(verts[i + 4]);

            vertices.add(normal.x);
            vertices.add(normal.y);
            vertices.add(normal.z);
        }
        return this;
    }



    public Mesh build() {
        float[] arr = new float[vertices.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = vertices.get(i);
        return new Mesh(arr, stride);
    }
}

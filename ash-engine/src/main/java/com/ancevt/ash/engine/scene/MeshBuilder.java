package com.ancevt.ash.engine.scene;

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


    public Mesh build() {
        float[] arr = new float[vertices.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = vertices.get(i);
        return new Mesh(arr, stride);
    }
}

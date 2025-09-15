package com.ancevt.ash.engine.scene;

public class MeshFactory {

    public static Mesh createTexturedCubeMesh(float size) {
        float hs = size / 2.0f; // half size

        float[] vertices = {
                // позиции          // UV      // нормали
                // Front
                -hs, -hs,  hs, 0, 0, 0, 0, 1,
                hs, -hs,  hs, 1, 0, 0, 0, 1,
                hs,  hs,  hs, 1, 1, 0, 0, 1,
                hs,  hs,  hs, 1, 1, 0, 0, 1,
                -hs,  hs,  hs, 0, 1, 0, 0, 1,
                -hs, -hs,  hs, 0, 0, 0, 0, 1,

                // Back
                -hs, -hs, -hs, 0, 0, 0, 0, -1,
                -hs,  hs, -hs, 0, 1, 0, 0, -1,
                hs,  hs, -hs, 1, 1, 0, 0, -1,
                hs,  hs, -hs, 1, 1, 0, 0, -1,
                hs, -hs, -hs, 1, 0, 0, 0, -1,
                -hs, -hs, -hs, 0, 0, 0, 0, -1,

                // Left
                -hs,  hs,  hs, 1, 0, -1, 0, 0,
                -hs,  hs, -hs, 1, 1, -1, 0, 0,
                -hs, -hs, -hs, 0, 1, -1, 0, 0,
                -hs, -hs, -hs, 0, 1, -1, 0, 0,
                -hs, -hs,  hs, 0, 0, -1, 0, 0,
                -hs,  hs,  hs, 1, 0, -1, 0, 0,

                // Right
                hs,  hs,  hs, 1, 0, 1, 0, 0,
                hs, -hs, -hs, 0, 1, 1, 0, 0,
                hs,  hs, -hs, 1, 1, 1, 0, 0,
                hs, -hs, -hs, 0, 1, 1, 0, 0,
                hs,  hs,  hs, 1, 0, 1, 0, 0,
                hs, -hs,  hs, 0, 0, 1, 0, 0,

                // Top
                -hs,  hs, -hs, 0, 1, 0, 1, 0,
                -hs,  hs,  hs, 0, 0, 0, 1, 0,
                hs,  hs,  hs, 1, 0, 0, 1, 0,
                hs,  hs,  hs, 1, 0, 0, 1, 0,
                hs,  hs, -hs, 1, 1, 0, 1, 0,
                -hs,  hs, -hs, 0, 1, 0, 1, 0,

                // Bottom
                -hs, -hs, -hs, 0, 1, 0, -1, 0,
                hs, -hs,  hs, 1, 0, 0, -1, 0,
                -hs, -hs,  hs, 0, 0, 0, -1, 0,
                hs, -hs,  hs, 1, 0, 0, -1, 0,
                -hs, -hs, -hs, 0, 1, 0, -1, 0,
                hs, -hs, -hs, 1, 1, 0, -1, 0
        };

        return new Mesh(vertices, 8);
    }

    public static Mesh createSkyboxCube() {
        float[] vertices = {
                // positions
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };
        return new Mesh(vertices, 3);
    }

    public static Mesh createFloorTileMesh(float size, float thickness) {
        float hs = size / 2.0f;
        float ht = thickness / 2.0f; // половина толщины

        float[] vertices = {
                // Верх
                -hs,  ht, -hs, 0, 0, 0, 1, 0,
                hs,  ht, -hs, 1, 0, 0, 1, 0,
                hs,  ht,  hs, 1, 1, 0, 1, 0,
                hs,  ht,  hs, 1, 1, 0, 1, 0,
                -hs,  ht,  hs, 0, 1, 0, 1, 0,
                -hs,  ht, -hs, 0, 0, 0, 1, 0,

                // Низ
                -hs, -ht, -hs, 0, 0, 0, -1, 0,
                hs, -ht, -hs, 1, 0, 0, -1, 0,
                hs, -ht,  hs, 1, 1, 0, -1, 0,
                hs, -ht,  hs, 1, 1, 0, -1, 0,
                -hs, -ht,  hs, 0, 1, 0, -1, 0,
                -hs, -ht, -hs, 0, 0, 0, -1, 0,

                // Передняя грань
                -hs, -ht,  hs, 0, 0, 0, 0, 1,
                hs, -ht,  hs, 1, 0, 0, 0, 1,
                hs,  ht,  hs, 1, 1, 0, 0, 1,
                hs,  ht,  hs, 1, 1, 0, 0, 1,
                -hs,  ht,  hs, 0, 1, 0, 0, 1,
                -hs, -ht,  hs, 0, 0, 0, 0, 1,

                // Задняя грань
                -hs, -ht, -hs, 0, 0, 0, 0, -1,
                hs, -ht, -hs, 1, 0, 0, 0, -1,
                hs,  ht, -hs, 1, 1, 0, 0, -1,
                hs,  ht, -hs, 1, 1, 0, 0, -1,
                -hs,  ht, -hs, 0, 1, 0, 0, -1,
                -hs, -ht, -hs, 0, 0, 0, 0, -1,

                // Левая грань
                -hs,  ht,  hs, 1, 0, -1, 0, 0,
                -hs,  ht, -hs, 1, 1, -1, 0, 0,
                -hs, -ht, -hs, 0, 1, -1, 0, 0,
                -hs, -ht, -hs, 0, 1, -1, 0, 0,
                -hs, -ht,  hs, 0, 0, -1, 0, 0,
                -hs,  ht,  hs, 1, 0, -1, 0, 0,

                // Правая грань
                hs,  ht,  hs, 1, 0, 1, 0, 0,
                hs,  ht, -hs, 1, 1, 1, 0, 0,
                hs, -ht, -hs, 0, 1, 1, 0, 0,
                hs, -ht, -hs, 0, 1, 1, 0, 0,
                hs, -ht,  hs, 0, 0, 1, 0, 0,
                hs,  ht,  hs, 1, 0, 1, 0, 0
        };

        return new Mesh(vertices, 8);
    }


}

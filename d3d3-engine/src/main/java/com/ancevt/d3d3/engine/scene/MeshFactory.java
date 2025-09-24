package com.ancevt.d3d3.engine.scene;

import com.ancevt.d3d3.engine.asset.UVRect;
import org.joml.Vector3f;

public class MeshFactory {

    public static float[] createTexturedCube(float size, UVRect uv) {
        float h = size / 2f;

        // 36 вершин (6 граней * 2 треугольника * 3 вершины)
        float[] verts = new float[36 * 8]; // xyz uv normal

        // нормали по граням
        float[][] normals = {
                { 0,  0, -1}, // back
                { 0,  0,  1}, // front
                {-1,  0,  0}, // left
                { 1,  0,  0}, // right
                { 0,  1,  0}, // top
                { 0, -1,  0}  // bottom
        };

        // базовые UV (0..1)
        float[][] uvBase = {
                {0, 0}, {1, 0}, {1, 1}, {0, 1}
        };

        int vi = 0;

        // каждая грань
        for (int f = 0; f < 6; f++) {
            float[] n = normals[f];

            // выбрать вершины для грани
            Vector3f[] faceVerts = getFaceVertices(f, h);

            // два треугольника: (0,1,2) и (2,3,0)
            int[][] tris = {{0,1,2},{2,3,0}};

            for (int[] tri : tris) {
                for (int idx : tri) {
                    Vector3f v = faceVerts[idx];
                    float[] uvRaw = uvBase[idx];

                    verts[vi++] = v.x;
                    verts[vi++] = v.y;
                    verts[vi++] = v.z;

                    // мапим UV в uvRect
                    verts[vi++] = uv.u() + uvRaw[0] * uv.width();
                    verts[vi++] = uv.v() + uvRaw[1] * uv.height();

                    verts[vi++] = n[0];
                    verts[vi++] = n[1];
                    verts[vi++] = n[2];
                }
            }
        }

        return verts;
    }

    private static Vector3f[] getFaceVertices(int face, float h) {
        return switch (face) {
            case 0 -> new Vector3f[]{ // back (-Z)
                    new Vector3f(-h, -h, -h),
                    new Vector3f( h, -h, -h),
                    new Vector3f( h,  h, -h),
                    new Vector3f(-h,  h, -h)
            };
            case 1 -> new Vector3f[]{ // front (+Z)
                    new Vector3f(-h, -h,  h),
                    new Vector3f( h, -h,  h),
                    new Vector3f( h,  h,  h),
                    new Vector3f(-h,  h,  h)
            };
            case 2 -> new Vector3f[]{ // left (-X)
                    new Vector3f(-h, -h, -h),
                    new Vector3f(-h, -h,  h),
                    new Vector3f(-h,  h,  h),
                    new Vector3f(-h,  h, -h)
            };
            case 3 -> new Vector3f[]{ // right (+X)
                    new Vector3f( h, -h, -h),
                    new Vector3f( h, -h,  h),
                    new Vector3f( h,  h,  h),
                    new Vector3f( h,  h, -h)
            };
            case 4 -> new Vector3f[]{ // top (+Y)
                    new Vector3f(-h,  h, -h),
                    new Vector3f( h,  h, -h),
                    new Vector3f( h,  h,  h),
                    new Vector3f(-h,  h,  h)
            };
            case 5 -> new Vector3f[]{ // bottom (-Y)
                    new Vector3f(-h, -h, -h),
                    new Vector3f( h, -h, -h),
                    new Vector3f( h, -h,  h),
                    new Vector3f(-h, -h,  h)
            };
            default -> throw new IllegalArgumentException("Invalid face: " + face);
        };
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

    public static float[] createFloorTile(float size, float thickness, UVRect uv) {
        float hs = size / 2.0f;     // половина стороны
        float ht = thickness / 2.0f; // половина толщины

        // исправленные UV (переворот по V)
        float u1 = uv.u();
        float u2 = uv.u() + uv.width();
        float v1 = uv.v();
        float v2 = uv.v() + uv.height();

        return new float[]{
                // === Верхняя грань ===
                -hs,  ht, -hs,  u1, v2,   0,  1, 0,
                hs,  ht, -hs,  u2, v2,   0,  1, 0,
                hs,  ht,  hs,  u2, v1,   0,  1, 0,
                hs,  ht,  hs,  u2, v1,   0,  1, 0,
                -hs,  ht,  hs,  u1, v1,   0,  1, 0,
                -hs,  ht, -hs,  u1, v2,   0,  1, 0,

                // === Нижняя грань ===
                -hs, -ht, -hs,  u1, v2,   0, -1, 0,
                hs, -ht, -hs,  u2, v2,   0, -1, 0,
                hs, -ht,  hs,  u2, v1,   0, -1, 0,
                hs, -ht,  hs,  u2, v1,   0, -1, 0,
                -hs, -ht,  hs,  u1, v1,   0, -1, 0,
                -hs, -ht, -hs,  u1, v2,   0, -1, 0,

                // === Передняя грань ===
                -hs, -ht,  hs,  u1, v2,   0,  0, 1,
                hs, -ht,  hs,  u2, v2,   0,  0, 1,
                hs,  ht,  hs,  u2, v1,   0,  0, 1,
                hs,  ht,  hs,  u2, v1,   0,  0, 1,
                -hs,  ht,  hs,  u1, v1,   0,  0, 1,
                -hs, -ht,  hs,  u1, v2,   0,  0, 1,

                // === Задняя грань ===
                -hs, -ht, -hs,  u1, v2,   0,  0, -1,
                hs, -ht, -hs,  u2, v2,   0,  0, -1,
                hs,  ht, -hs,  u2, v1,   0,  0, -1,
                hs,  ht, -hs,  u2, v1,   0,  0, -1,
                -hs,  ht, -hs,  u1, v1,   0,  0, -1,
                -hs, -ht, -hs,  u1, v2,   0,  0, -1,

                // === Левая грань ===
                -hs, -ht, -hs,  u1, v2,  -1,  0, 0,
                -hs, -ht,  hs,  u2, v2,  -1,  0, 0,
                -hs,  ht,  hs,  u2, v1,  -1,  0, 0,
                -hs,  ht,  hs,  u2, v1,  -1,  0, 0,
                -hs,  ht, -hs,  u1, v1,  -1,  0, 0,
                -hs, -ht, -hs,  u1, v2,  -1,  0, 0,

                // === Правая грань ===
                hs, -ht, -hs,  u1, v2,   1,  0, 0,
                hs, -ht,  hs,  u2, v2,   1,  0, 0,
                hs,  ht,  hs,  u2, v1,   1,  0, 0,
                hs,  ht,  hs,  u2, v1,   1,  0, 0,
                hs,  ht, -hs,  u1, v1,   1,  0, 0,
                hs, -ht, -hs,  u1, v2,   1,  0, 0,
        };
    }





}

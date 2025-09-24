package com.ancevt.d3d3.engine.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformUtil {
    public static float[] transformVertices(float[] verts, float dx, float dy, float dz) {
        float[] res = verts.clone();
        int stride = 8; // xyz uv normal

        for (int i = 0; i < res.length; i += stride) {
            res[i] += dx;
            res[i + 1] += dy;
            res[i + 2] += dz;
        }
        return res;
    }

    public static float[] transformVertices(float[] verts, Matrix4f transform) {
        float[] res = verts.clone();
        int stride = 8; // xyz uv normal

        for (int i = 0; i < res.length; i += stride) {
            Vector3f pos = new Vector3f(res[i], res[i + 1], res[i + 2]);
            pos.mulPosition(transform);
            res[i] = pos.x;
            res[i + 1] = pos.y;
            res[i + 2] = pos.z;

            Vector3f norm = new Vector3f(res[i + 5], res[i + 6], res[i + 7]);
            norm.mulDirection(transform).normalize();
            res[i + 5] = norm.x;
            res[i + 6] = norm.y;
            res[i + 7] = norm.z;
        }

        return res;
    }

}


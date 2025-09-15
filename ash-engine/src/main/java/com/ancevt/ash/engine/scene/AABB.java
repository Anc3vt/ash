package com.ancevt.ash.engine.scene;

import org.joml.Vector3f;

public class AABB {
    public Vector3f min;
    public Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }

    public boolean intersects(Vector3f point) {
        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y &&
                point.z >= min.z && point.z <= max.z;
    }
}

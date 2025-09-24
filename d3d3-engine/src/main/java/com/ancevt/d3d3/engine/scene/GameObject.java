package com.ancevt.d3d3.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GameObject {

    @Getter
    private Mesh mesh;
    @Getter
    private int textureId;
    @Getter
    private Vector3f position;
    @Getter
    private Vector3f rotation;
    @Getter
    private Vector3f scale;

    private AABB boundingBox;


    public GameObject(Mesh mesh, int textureId) {
        this.mesh = mesh;
        this.textureId = textureId;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);

        updateBoundingBox();
    }

    @Getter
    private Vector3f color = new Vector3f(1, 1, 1); // белый по умолчанию

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
    }

    public void update() {
        // можно делать анимацию
    }

    public void update(float time) {}


    public void updateBoundingBox() {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        for (Vector3f[] tri : mesh.getTriangles()) {
            for (Vector3f v : tri) {
                Vector3f worldPos = new Vector3f(v)
                        .mul(scale) // масштаб
                        .add(position); // перенос
                min.min(worldPos);
                max.max(worldPos);
            }
        }

        boundingBox = new AABB(min, max);
    }


    public AABB getBoundingBox() {
        return boundingBox;
    }

    // Удобные сеттеры
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateBoundingBox();
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        updateBoundingBox();
    }


}

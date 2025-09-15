package com.ancevt.ash.engine.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private float pitch; // X rotation
    private float yaw;   // Y rotation

    public Camera() {
        position = new Vector3f(0, 3, 3);
        pitch = 0;
        yaw = -90; // Смотрим вдоль -Z
    }

    public Matrix4f getViewMatrix() {
        Vector3f front = getFront();
        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, new Vector3f(0, 1, 0));
    }

    public Vector3f getFront() {
        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return front.normalize();
    }

    public void move(Vector3f offset) {
        position.add(offset);
    }

    public void addRotation(float dx, float dy) {
        yaw += dx;
        pitch += dy;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    public Vector3f getPosition() {
        return position;
    }
}

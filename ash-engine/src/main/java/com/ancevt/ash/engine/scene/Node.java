package com.ancevt.ash.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Node {
    protected Vector3f position = new Vector3f();
    protected Vector3f rotation = new Vector3f();
    protected Vector3f scale = new Vector3f(1,1,1);

    @Getter
    protected Node parent;

    @Getter
    protected List<Node> children = new ArrayList<>();

    public void addChild(Node child) {
        children.add(child);
        child.parent = this;
    }

    public void removeChild(Node child) {
        children.remove(child);
        child.parent = null;
    }

    public Matrix4f getLocalTransform() {
        return new Matrix4f()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .scale(scale);
    }

    public Matrix4f getWorldTransform() {
        if (parent == null) {
            return getLocalTransform();
        } else {
            return new Matrix4f(parent.getWorldTransform()).mul(getLocalTransform());
        }
    }

    public void render(RenderContext ctx) {
        for (Node child : children) {
            child.render(ctx);
        }
    }

    public void update(float time) {
        for (Node child : children) {
            child.update(time);
        }
    }

    // getters/setters
    public Vector3f getPosition() { return position; }
    public Vector3f getRotation() { return rotation; }
    public Vector3f getScale() { return scale; }

    public void setPosition(float x, float y, float z) { position.set(x,y,z); }
    public void setRotation(float x, float y, float z) { rotation.set(x,y,z); }
    public void setScale(float x, float y, float z) { scale.set(x,y,z); }
}

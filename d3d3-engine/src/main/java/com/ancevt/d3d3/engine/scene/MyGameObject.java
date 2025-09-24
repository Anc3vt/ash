package com.ancevt.d3d3.engine.scene;

public class MyGameObject extends GameObject {

    private float r;

    public MyGameObject(Mesh mesh, int textureId) {
        super(mesh, textureId);

        getRotation().y = (float) (Math.random() * 360);
    }

    @Override
    public void update() {
    }

    @Override
    public void update(float time) {

        getRotation().y += 1;


        updateBoundingBox();
    }
}

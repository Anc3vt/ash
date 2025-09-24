package com.ancevt.d3d3.engine.scene;

import lombok.Getter;

import java.util.List;

public class MazeNode extends GameObjectNode {

    @Getter
    private final List<AABB> colliders;

    public MazeNode(Mesh mesh, int textureId, List<AABB> colliders) {
        super(mesh, textureId);
        this.colliders = colliders;
    }

    public boolean checkCollision(AABB player) {
        for (AABB box : colliders) {
            float epsilon = 0.001f;
            if (player.min.x <= box.max.x - epsilon && player.max.x >= box.min.x + epsilon &&
                    player.min.y <= box.max.y - epsilon && player.max.y >= box.min.y + epsilon &&
                    player.min.z <= box.max.z - epsilon && player.max.z >= box.min.z + epsilon) {
                return true;
            }

        }
        return false;
    }
}

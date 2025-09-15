package com.ancevt.ash.engine.asset;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private final Map<String, Integer> textures = new HashMap<>();
    private final Map<String, OBJModel> objs = new HashMap<>();

    public int loadTexture(String path, boolean repeat) {
        return textures.computeIfAbsent(path,
                p -> TextureLoader.loadTextureFromResources(p, repeat));
    }

    public OBJModel loadObj(String path) {
        return objs.computeIfAbsent(path, OBJLoader::load);
    }
}

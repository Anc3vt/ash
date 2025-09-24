package com.ancevt.d3d3.engine.core;


import com.ancevt.d3d3.engine.asset.AssetManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EngineContext {

    private final Engine engine;
    private final LaunchConfig launchConfig;
    private final AssetManager assetManager;
}

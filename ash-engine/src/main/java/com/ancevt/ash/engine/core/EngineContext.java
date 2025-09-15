package com.ancevt.ash.engine.core;


import com.ancevt.ash.engine.asset.AssetManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EngineContext {

    private final Engine engine;
    private final LaunchConfig launchConfig;
    private final AssetManager assetManager;
}

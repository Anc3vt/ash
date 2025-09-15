package com.ancevt.ash.engine.core;

public interface Application {

    void init(EngineContext ctx);

    void update();

    void shutdown();
}

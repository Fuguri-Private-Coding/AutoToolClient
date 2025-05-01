package me.hackclient.deeplearn;

import ai.djl.engine.Engine;
import me.hackclient.Client;

public class DeepLearningEngine {
    public void init() {
        System.setProperty("DJL_CACHE_DIR", Client.INSTANCE.getModelsDirectory().getAbsolutePath());
        System.setProperty("ENGINE_CACHE_DIR", Client.INSTANCE.getModelsDirectory().getAbsolutePath());

        // Disable tracking of DJL
        System.setProperty("OPT_OUT_TRACKING", "true");
        Engine.getInstance();
    }
}

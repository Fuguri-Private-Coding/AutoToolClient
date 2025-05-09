package me.hackclient.deeplearn;

import ai.djl.engine.Engine;
import me.hackclient.Client;

import java.io.File;

public class DeepLearningEngine {

    public void init() {
        File cacheDir = new File(Client.INSTANCE.getClientDirectory().getName() + "/djlCache");
        System.setProperty("DJL_CACHE_DIR", cacheDir.getAbsolutePath());
        System.setProperty("ENGINE_CACHE_DIR", cacheDir.getAbsolutePath());

        // Disable tracking of DJL
        System.setProperty("OPT_OUT_TRACKING", "true");
        Engine.getInstance();
    }
}
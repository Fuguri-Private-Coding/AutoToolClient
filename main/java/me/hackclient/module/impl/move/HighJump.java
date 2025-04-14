package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;

@ModuleInfo(name = "HighJump", category = Category.MOVE)
public class HighJump extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "FunnyMcSkyPvp",
            new String[] {
                    "FunnyMcSkyPvp"
            }
    );

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        switch (mode.getMode()) {
            case "FunnyMcSkyPvp" -> {
                if (event instanceof MotionEvent) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.02;
                    }
                }
            }
        }
    }
}

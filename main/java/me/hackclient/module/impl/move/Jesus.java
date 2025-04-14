package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;

@ModuleInfo(
        name = "Jesus",
        category = Category.MOVE
)
public class Jesus extends Module {

    ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "45DegFunnyMcSkyPvpree",
            new String[] {
                    "FunnyMcSkyPvp"
            }
    );

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

        if (!mc.thePlayer.isInWater()) { return; }

        switch (mode.getMode()) {
            case "45DegFunnyMcSkyPvpree" -> {
                if (event instanceof MotionEvent) {
                    if (!mc.thePlayer.movementInput.jump && !mc.thePlayer.movementInput.sneak) {
                        mc.thePlayer.motionY = 0;
                    }
                }
            }
        }
    }
}

package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.MultiMode;
import me.hackclient.utils.move.MoveUtils;

@ModuleInfo(name = "Fixes", category = Category.MISC)
public class Fixes extends Module {

    MultiMode fixes = new MultiMode("Fixes", this)
            .add("ClickDelay", true)
            .add("SaveMoveKeys", true);

    boolean wasInGui = false;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MotionEvent) {
            if (mc.thePlayer != null && mc.theWorld != null && fixes.get("ClickDelay")) mc.leftClickCounter = -1;
            if (fixes.get("SaveMoveKeys")) {
                if (mc.currentScreen == null) {
                    if (wasInGui) MoveUtils.updateControls();
                    wasInGui = false;
                } else {
                    wasInGui = true;
                }
            }
        }
    }
}

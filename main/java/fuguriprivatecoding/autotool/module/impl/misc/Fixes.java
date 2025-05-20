package fuguriprivatecoding.autotool.module.impl.misc;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.MotionEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.MultiMode;
import fuguriprivatecoding.autotool.utils.move.MoveUtils;

@ModuleInfo(name = "Fixes", category = Category.MISC)
public class Fixes extends Module {

    MultiMode fixes = new MultiMode("Fixes", this)
            .add("ClickDelay", true)
            .add("SaveMoveKeys", true)
            .add("JumpDelay", true);

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
            if (fixes.get("JumpDelay")) mc.thePlayer.jumpTicks = 0;
        }
    }
}

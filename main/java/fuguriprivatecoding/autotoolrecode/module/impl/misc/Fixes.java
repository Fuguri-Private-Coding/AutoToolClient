package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;

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

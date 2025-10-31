package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;

@ModuleInfo(name = "Fixes", category = Category.MISC, description = "Убирает задержки механики майнкрафта.")
public class Fixes extends Module {

    MultiMode fixes = new MultiMode("Fixes", this)
            .add("ClickDelay", true)
            .add("SaveMoveKeys", true)
            .add("JumpDelay", true);

    boolean wasInGui = false;

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null && mc.theWorld == null) return;
        if (event instanceof MotionEvent) {
            if (fixes.get("ClickDelay")) mc.leftClickCounter = -1;
            if (fixes.get("SaveMoveKeys")) {
                if (mc.currentScreen == null) {
                    if (wasInGui) MoveUtils.updateControls();
                    wasInGui = false;
                } else {
                    wasInGui = true;
                }
            }

            if (fixes.get("JumpDelay") && !Client.INST.getModules().getModule(Scaffold.class).isToggled()) {
                mc.thePlayer.jumpTicks = 0;
            }
        }
    }
}

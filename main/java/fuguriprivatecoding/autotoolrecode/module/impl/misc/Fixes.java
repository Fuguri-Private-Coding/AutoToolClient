package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;

@ModuleInfo(name = "Fixes", category = Category.MISC, description = "Убирает задержки механики майнкрафта.")
public class Fixes extends Module {

    MultiMode fixes = new MultiMode("Fixes", this)
            .add("ClickDelay", true)
            .add("SaveMoveKeys", true)
            .add("JumpDelay", true)
            .add("Memory", true);

    boolean wasInGui = false;

    @Override
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

            if (fixes.get("JumpDelay") && !Modules.getModule(Scaffold.class).isToggled()) {
                mc.thePlayer.jumpTicks = 0;
            }
        }
        if (event instanceof WorldChangeEvent && fixes.get("Memory")) {
            System.gc();
        }
    }
}

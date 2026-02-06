package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;

@ModuleInfo(name = "Fixes", category = Category.MISC, description = "Убирает задержки механики майнкрафта.")
public class Fixes extends Module {

    public MultiMode fixes = new MultiMode("Fixes", this)
            .add("ClickDelay", true)
            .add("SaveMoveKeys", true)
            .add("JumpDelay", true)
            .add("FastWorldLoading", true);

    IntegerSetting jumpChance = new IntegerSetting("JumpChance", this, 0, 100, 100);

    boolean wasInGui = false;

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null && mc.theWorld == null) return;
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE) {
            if (fixes.get("ClickDelay")) mc.leftClickCounter = -1;
            if (fixes.get("SaveMoveKeys")) {
                if (wasInGui && mc.currentScreen == null) MoveUtils.updateControls();
                wasInGui = mc.currentScreen == null;
            }

            if (fixes.get("JumpDelay") && Math.random() <= jumpChance.getValue() / 100f) {
                mc.thePlayer.jumpTicks = 0;
            }
        }
    }
}

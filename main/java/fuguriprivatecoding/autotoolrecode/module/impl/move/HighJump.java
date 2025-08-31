package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;

@ModuleInfo(name = "HighJump", category = Category.MOVE)
public class HighJump extends Module {

    int stage = 0;

    @Override
    public void onDisable() {
        stage = 0;
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MotionEvent e) {
            if(stage == 0) e.setOnGround(false);
        }
        if (event instanceof UpdateEvent) {
            if(stage == 0) {
                mc.thePlayer.motionY = 1;
                stage++;
            }
        }
    }
}

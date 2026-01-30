package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "FastLadder", category = Category.MOVE, description = "Позволяет быстро подниматся по лестнице.")
public class FastLadder extends Module {

    final FloatSetting motion = new FloatSetting("Motion", this, 0, 5, 0.6f, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE && mc.thePlayer.isOnLadder() && mc.gameSettings.keyBindJump.isKeyDown()) {
            e.setOnGround(true);
            mc.thePlayer.motionY = motion.getValue();
        }
    }
}

package fuguriprivatecoding.autotoolrecode.module.impl.move.longjump;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.move.LongJump;

public interface LongJumpMode {
    void onEnable(LongJump longJump);
    void onDisable(LongJump longJump);
    void handleEvent(Event event, LongJump longJump);
    String getName();
}
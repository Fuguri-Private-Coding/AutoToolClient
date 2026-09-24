package fuguriprivatecoding.autotoolrecode.module.impl.move.longjump;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.move.LongJump;

public interface LongJumpMode {
    void tick(LongJump longJump, boolean toggled);
    void handleEvent(Event event, LongJump longJump);
    String getName();
}
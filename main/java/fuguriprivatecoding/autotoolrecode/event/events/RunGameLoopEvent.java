package fuguriprivatecoding.autotoolrecode.event.events;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class RunGameLoopEvent extends Event {
    @Getter private static final RunGameLoopEvent instance = new RunGameLoopEvent();
    private RunGameLoopEvent() {}
}

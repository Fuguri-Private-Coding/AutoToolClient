package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class BestClickTimingEvent extends Event {
    @Getter
    private static final BestClickTimingEvent instance = new BestClickTimingEvent();
    private BestClickTimingEvent() {}
}

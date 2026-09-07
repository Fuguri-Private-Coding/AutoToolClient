package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class LegitClickTimingEvent extends Event {
    @Getter
    private static final LegitClickTimingEvent instance = new LegitClickTimingEvent();
    private LegitClickTimingEvent() {}
}

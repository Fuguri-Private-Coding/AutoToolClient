package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class TickEvent extends Event {
    @Getter
    private static final TickEvent instance = new TickEvent();
    private TickEvent() {}
}

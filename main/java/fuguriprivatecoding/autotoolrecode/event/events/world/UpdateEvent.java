package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class UpdateEvent extends Event {
    @Getter private static final UpdateEvent instance = new UpdateEvent();
    private UpdateEvent() {}
}

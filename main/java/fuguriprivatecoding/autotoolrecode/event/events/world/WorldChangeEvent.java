package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WorldChangeEvent extends Event {
    @Getter private static final WorldChangeEvent instance = new WorldChangeEvent();
    private WorldChangeEvent() {}
}

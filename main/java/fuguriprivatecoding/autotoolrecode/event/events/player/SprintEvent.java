package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SprintEvent extends Event {
    @Getter
    private static final SprintEvent instance = new SprintEvent();

    private SprintEvent() {}

    boolean sprinting;
}
package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlowDownEvent extends Event {
    @Getter
    private static final SlowDownEvent instance = new SlowDownEvent();
    private SlowDownEvent() {}

    private float strafe, forward;
    private boolean sprinting;
}

package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SlowDownEvent extends Event {
    private float strafe;
    private float forward;
    private boolean sprinting;
}

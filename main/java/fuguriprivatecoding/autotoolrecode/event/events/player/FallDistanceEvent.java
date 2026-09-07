package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FallDistanceEvent extends Event {
    @Getter
    private static final FallDistanceEvent instance = new FallDistanceEvent();
    private FallDistanceEvent() {}

    float fallDistance;
    float damageMultiplier;
}

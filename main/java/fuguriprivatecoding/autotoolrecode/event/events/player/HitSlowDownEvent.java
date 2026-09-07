package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HitSlowDownEvent extends Event {
    @Getter
    private static final HitSlowDownEvent instance = new HitSlowDownEvent();
    private HitSlowDownEvent() {}

    double slowDown;
    boolean sprint;
}

package fuguriprivatecoding.autotoolrecode.event.events;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HitSlowDownEvent extends Event {
    public double slowDown;
    public boolean sprint;
}

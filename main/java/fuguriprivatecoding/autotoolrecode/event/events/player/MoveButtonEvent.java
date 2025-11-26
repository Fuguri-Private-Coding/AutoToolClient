package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class MoveButtonEvent extends Event {
	boolean forward, back, left, right, jump, sneak;
}

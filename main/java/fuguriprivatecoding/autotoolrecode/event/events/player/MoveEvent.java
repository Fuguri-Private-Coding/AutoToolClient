package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class MoveEvent extends Event {
	float forward, strafe;
	boolean jump, sneak;
	float sneakSlowDown;
}

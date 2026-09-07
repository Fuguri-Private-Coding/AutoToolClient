package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MoveFlyingEvent extends Event {
	@Getter
	private static final MoveFlyingEvent instance = new MoveFlyingEvent();
	private MoveFlyingEvent() {}

	float yaw, strafe, forward, friction;
}

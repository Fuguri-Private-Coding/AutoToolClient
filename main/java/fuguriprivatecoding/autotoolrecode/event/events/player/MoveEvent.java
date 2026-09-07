package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MoveEvent extends Event {
	@Getter
	private static final MoveEvent instance = new MoveEvent();
	private MoveEvent() {}

	float forward, strafe;
	float sneakSlowDown;
}

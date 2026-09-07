package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
public class MoveButtonEvent extends Event {
	@Getter
	private static final MoveButtonEvent instance = new MoveButtonEvent();
	private MoveButtonEvent() {}

	boolean forward, back, left, right, jump, sneak;
}

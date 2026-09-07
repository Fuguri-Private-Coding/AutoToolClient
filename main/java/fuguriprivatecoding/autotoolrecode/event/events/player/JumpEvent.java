package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JumpEvent extends Event {
	@Getter
	private static final JumpEvent instance = new JumpEvent();
	private JumpEvent() {}

	float yaw, height;
}

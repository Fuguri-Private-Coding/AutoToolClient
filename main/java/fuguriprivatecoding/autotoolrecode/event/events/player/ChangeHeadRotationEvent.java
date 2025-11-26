package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class ChangeHeadRotationEvent extends Event {
	float yaw, pitch;
}

package fuguriprivatecoding.autotool.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class ChangeHeadRotationEvent extends Event {
	float yaw, pitch;
}

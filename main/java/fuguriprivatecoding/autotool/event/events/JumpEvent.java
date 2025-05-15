package fuguriprivatecoding.autotool.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.event.CancelableEvent;

@Setter
@Getter
@AllArgsConstructor
public class JumpEvent extends CancelableEvent {
	float yaw, height;
}

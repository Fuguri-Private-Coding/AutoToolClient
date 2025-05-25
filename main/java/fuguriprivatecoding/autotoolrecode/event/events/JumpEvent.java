package fuguriprivatecoding.autotoolrecode.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;

@Setter
@Getter
@AllArgsConstructor
public class JumpEvent extends CancelableEvent {
	float yaw, height;
}

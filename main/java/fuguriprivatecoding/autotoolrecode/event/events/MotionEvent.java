package fuguriprivatecoding.autotoolrecode.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class MotionEvent extends Event {
	double x, y, z;
	float yaw, pitch;
	boolean onGround;
}

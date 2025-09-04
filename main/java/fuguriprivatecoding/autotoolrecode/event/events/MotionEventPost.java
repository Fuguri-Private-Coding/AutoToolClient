package fuguriprivatecoding.autotoolrecode.event.events;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MotionEventPost extends Event {
    double x, y, z;
    float yaw, pitch;
    boolean onGround;
}

package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
public class MotionEvent extends Event {
    @Getter
    private static final MotionEvent instance = new MotionEvent();
    private MotionEvent() {}

	double x, y, z;
	boolean onGround;
    Type type;

    public enum Type {
        PRE, POST
    }
}
